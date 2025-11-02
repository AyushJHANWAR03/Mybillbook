package com.mybillbook.service;

import com.mybillbook.enums.InvoiceStatus;
import com.mybillbook.enums.PaymentStatus;
import com.mybillbook.enums.SuggestionStatus;
import com.mybillbook.model.Invoice;
import com.mybillbook.model.Payment;
import com.mybillbook.model.ReconciliationSuggestion;
import com.mybillbook.repository.InvoiceRepository;
import com.mybillbook.repository.PaymentRepository;
import com.mybillbook.repository.ReconciliationSuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ReconciliationSuggestionRepository suggestionRepository;

    public Map<String, Object> getSummaryReport(Long userId) {
        Map<String, Object> report = new HashMap<>();

        // Invoice stats
        List<Invoice> allInvoices = invoiceRepository.findByUserIdAndStatusIn(
            userId, List.of(InvoiceStatus.values())
        );
        long reconciledInvoices = allInvoices.stream()
            .filter(inv -> inv.getStatus() == InvoiceStatus.FULLY_PAID)
            .count();
        long pendingInvoices = allInvoices.size() - reconciledInvoices;

        // Payment stats
        List<Payment> allPayments = paymentRepository.findByUserId(userId);
        long reconciledPayments = allPayments.stream()
            .filter(p -> p.getStatus() == PaymentStatus.RECONCILED)
            .count();
        long unreconciledPayments = allPayments.size() - reconciledPayments;

        // AI accuracy
        List<ReconciliationSuggestion> allSuggestions = suggestionRepository.findByStatus(SuggestionStatus.CONFIRMED);
        allSuggestions.addAll(suggestionRepository.findByStatus(SuggestionStatus.REJECTED));

        double aiAccuracy = 0.0;
        if (!allSuggestions.isEmpty()) {
            long confirmed = allSuggestions.stream()
                .filter(s -> s.getStatus() == SuggestionStatus.CONFIRMED)
                .count();
            aiAccuracy = (double) confirmed / allSuggestions.size();
        }

        // Revenue stats
        BigDecimal totalRevenue = allInvoices.stream()
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRevenue = allInvoices.stream()
            .map(Invoice::getPendingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("totalInvoices", allInvoices.size());
        report.put("reconciledInvoices", reconciledInvoices);
        report.put("pendingInvoices", pendingInvoices);

        report.put("totalPayments", allPayments.size());
        report.put("reconciledPayments", reconciledPayments);
        report.put("unreconciledPayments", unreconciledPayments);

        report.put("aiAccuracy", BigDecimal.valueOf(aiAccuracy).setScale(2, RoundingMode.HALF_UP));
        report.put("totalRevenue", totalRevenue);
        report.put("pendingRevenue", pendingRevenue);

        return report;
    }
}
