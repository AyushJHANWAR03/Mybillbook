package com.mybillbook.service;

import com.mybillbook.dto.OpenAIMatchResponse;
import com.mybillbook.enums.InvoiceStatus;
import com.mybillbook.enums.PaymentStatus;
import com.mybillbook.enums.SuggestionStatus;
import com.mybillbook.exception.ResourceNotFoundException;
import com.mybillbook.model.Invoice;
import com.mybillbook.model.Payment;
import com.mybillbook.model.ReconciliationSuggestion;
import com.mybillbook.model.User;
import com.mybillbook.repository.InvoiceRepository;
import com.mybillbook.repository.PaymentRepository;
import com.mybillbook.repository.ReconciliationSuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReconciliationService {

    private final OpenAIService openAIService;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReconciliationSuggestionRepository suggestionRepository;

    @Value("${openai.model:gpt-4o-mini}")
    private String aiModel;

    @Transactional
    public int runReconciliation(Long userId) {
        log.info("Starting AI reconciliation for user: {}", userId);

        // Fetch all unreconciled payments for this user
        List<Payment> unreconciledPayments = paymentRepository.findByUserIdAndStatus(userId, PaymentStatus.UNRECONCILED);

        // Fetch all pending invoices (UNPAID or PARTIALLY_PAID)
        List<Invoice> pendingInvoices = invoiceRepository.findByUserIdAndStatusIn(
            userId,
            List.of(InvoiceStatus.UNPAID, InvoiceStatus.PARTIALLY_PAID)
        );

        if (unreconciledPayments.isEmpty()) {
            log.info("No unreconciled payments found for user: {}", userId);
            return 0;
        }

        if (pendingInvoices.isEmpty()) {
            log.info("No pending invoices found for user: {}", userId);
            return 0;
        }

        int suggestionsGenerated = 0;

        // Process each unreconciled payment
        for (Payment payment : unreconciledPayments) {
            try {
                // Skip if this payment already has pending suggestions
                boolean hasPendingSuggestion = suggestionRepository.existsByPaymentIdAndStatus(
                    payment.getId(), SuggestionStatus.PENDING);

                if (hasPendingSuggestion) {
                    log.info("Skipping payment {} - already has pending suggestions", payment.getId());
                    continue;
                }

                OpenAIMatchResponse aiResponse = openAIService.findMatchingInvoices(payment, pendingInvoices);

                if (aiResponse.getMatches() != null && !aiResponse.getMatches().isEmpty()) {
                    for (OpenAIMatchResponse.Match match : aiResponse.getMatches()) {
                        // Find the invoice by invoice number
                        Invoice matchedInvoice = findInvoiceByNumber(pendingInvoices, match.getInvoiceNumber());

                        if (matchedInvoice != null) {
                            // Create suggestion
                            ReconciliationSuggestion suggestion = new ReconciliationSuggestion();
                            suggestion.setPayment(payment);
                            suggestion.setInvoice(matchedInvoice);
                            suggestion.setConfidence(match.getConfidence());
                            suggestion.setReasoning(match.getReason());
                            suggestion.setStatus(SuggestionStatus.PENDING);
                            suggestion.setAiModel(aiModel);

                            suggestionRepository.save(suggestion);
                            suggestionsGenerated++;
                            log.info("Created suggestion: Payment {} -> Invoice {} (confidence: {})",
                                payment.getId(), matchedInvoice.getInvoiceNumber(), match.getConfidence());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process payment {}: {}", payment.getId(), e.getMessage());
                // Continue processing other payments
            }
        }

        log.info("Reconciliation completed. Generated {} suggestions for user {}", suggestionsGenerated, userId);
        return suggestionsGenerated;
    }

    @Transactional
    public void confirmSuggestion(Long suggestionId, Long userId) {
        ReconciliationSuggestion suggestion = suggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found with ID: " + suggestionId));

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Only pending suggestions can be confirmed");
        }

        // Update suggestion status
        suggestion.setStatus(SuggestionStatus.CONFIRMED);
        User user = new User();
        user.setId(userId);
        suggestion.setConfirmedBy(user);
        suggestionRepository.save(suggestion);

        // Update payment status
        Payment payment = suggestion.getPayment();
        payment.setStatus(PaymentStatus.RECONCILED);
        paymentRepository.save(payment);

        // Update invoice pending amount and status
        Invoice invoice = suggestion.getInvoice();
        BigDecimal newPendingAmount = invoice.getPendingAmount().subtract(payment.getAmount());

        if (newPendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setPendingAmount(BigDecimal.ZERO);
            invoice.setStatus(InvoiceStatus.FULLY_PAID);
        } else {
            invoice.setPendingAmount(newPendingAmount);
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }

        invoiceRepository.save(invoice);

        log.info("Confirmed suggestion {}: Payment {} -> Invoice {}, New pending: ₹{}",
            suggestionId, payment.getId(), invoice.getInvoiceNumber(), invoice.getPendingAmount());
    }

    @Transactional
    public void rejectSuggestion(Long suggestionId) {
        ReconciliationSuggestion suggestion = suggestionRepository.findById(suggestionId)
            .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found with ID: " + suggestionId));

        if (suggestion.getStatus() != SuggestionStatus.PENDING) {
            throw new IllegalStateException("Only pending suggestions can be rejected");
        }

        suggestion.setStatus(SuggestionStatus.REJECTED);
        suggestionRepository.save(suggestion);

        log.info("Rejected suggestion {}", suggestionId);
    }

    @Transactional
    public int bulkConfirm(List<Long> suggestionIds, Long userId) {
        int confirmed = 0;

        for (Long suggestionId : suggestionIds) {
            try {
                confirmSuggestion(suggestionId, userId);
                confirmed++;
            } catch (Exception e) {
                log.error("Failed to confirm suggestion {}: {}", suggestionId, e.getMessage());
            }
        }

        log.info("Bulk confirmed {} out of {} suggestions", confirmed, suggestionIds.size());
        return confirmed;
    }

    @Transactional
    public int bulkConfirmHighConfidence(BigDecimal minConfidence, Long userId) {
        List<ReconciliationSuggestion> highConfidenceSuggestions =
            suggestionRepository.findByConfidenceGreaterThanEqualAndStatus(minConfidence, SuggestionStatus.PENDING);

        List<Long> suggestionIds = highConfidenceSuggestions.stream()
            .map(ReconciliationSuggestion::getId)
            .toList();

        return bulkConfirm(suggestionIds, userId);
    }

    public List<ReconciliationSuggestion> getPendingSuggestions(Long userId) {
        // Get all pending suggestions
        List<ReconciliationSuggestion> allPending = suggestionRepository.findByStatus(SuggestionStatus.PENDING);

        // Filter by userId (check if payment belongs to user)
        return allPending.stream()
            .filter(s -> s.getPayment().getUser().getId().equals(userId))
            .toList();
    }

    private Invoice findInvoiceByNumber(List<Invoice> invoices, String invoiceNumber) {
        return invoices.stream()
            .filter(inv -> inv.getInvoiceNumber().equalsIgnoreCase(invoiceNumber))
            .findFirst()
            .orElse(null);
    }
}
