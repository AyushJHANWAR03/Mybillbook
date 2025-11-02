package com.mybillbook.repository;

import com.mybillbook.enums.InvoiceStatus;
import com.mybillbook.enums.PaymentMode;
import com.mybillbook.enums.PaymentStatus;
import com.mybillbook.enums.SuggestionStatus;
import com.mybillbook.model.Invoice;
import com.mybillbook.model.Payment;
import com.mybillbook.model.ReconciliationSuggestion;
import com.mybillbook.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReconciliationSuggestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReconciliationSuggestionRepository suggestionRepository;

    private User testUser;
    private Invoice testInvoice;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        // Create user
        testUser = new User();
        testUser.setMobileNumber("9876543210");
        testUser.setName("Ramesh Kumar");
        testUser.setBusinessName("Ramesh Traders");
        entityManager.persistAndFlush(testUser);

        // Create invoice
        testInvoice = new Invoice();
        testInvoice.setUser(testUser);
        testInvoice.setInvoiceNumber("INV001");
        testInvoice.setCustomerName("Suresh Traders");
        testInvoice.setTotalAmount(new BigDecimal("10000.00"));
        testInvoice.setPendingAmount(new BigDecimal("10000.00"));
        testInvoice.setStatus(InvoiceStatus.UNPAID);
        testInvoice.setInvoiceDate(LocalDate.now());
        entityManager.persistAndFlush(testInvoice);

        // Create payment
        testPayment = new Payment();
        testPayment.setUser(testUser);
        testPayment.setAmount(new BigDecimal("5000.00"));
        testPayment.setPaymentDate(LocalDate.now());
        testPayment.setPaymentMode(PaymentMode.UPI);
        testPayment.setRemark("INV001 partial");
        testPayment.setStatus(PaymentStatus.UNRECONCILED);
        entityManager.persistAndFlush(testPayment);
    }

    @Test
    void shouldSaveAndRetrieveReconciliationSuggestion() {
        // Given
        ReconciliationSuggestion suggestion = new ReconciliationSuggestion();
        suggestion.setPayment(testPayment);
        suggestion.setInvoice(testInvoice);
        suggestion.setConfidence(new BigDecimal("0.92"));
        suggestion.setReasoning("Remark mentions INV001 and amount is half of pending");
        suggestion.setStatus(SuggestionStatus.PENDING);
        suggestion.setAiModel("gpt-4o-mini");

        // When
        ReconciliationSuggestion saved = suggestionRepository.save(suggestion);
        ReconciliationSuggestion found = entityManager.find(ReconciliationSuggestion.class, saved.getId());

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getPayment().getId()).isEqualTo(testPayment.getId());
        assertThat(found.getInvoice().getId()).isEqualTo(testInvoice.getId());
        assertThat(found.getConfidence()).isEqualByComparingTo("0.92");
        assertThat(found.getReasoning()).isEqualTo("Remark mentions INV001 and amount is half of pending");
        assertThat(found.getStatus()).isEqualTo(SuggestionStatus.PENDING);
        assertThat(found.getAiModel()).isEqualTo("gpt-4o-mini");
        assertThat(found.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindSuggestionsByPaymentIdAndStatus() {
        // Given
        ReconciliationSuggestion suggestion1 = createSuggestion(testPayment, testInvoice, "0.90", SuggestionStatus.PENDING);
        ReconciliationSuggestion suggestion2 = createSuggestion(testPayment, testInvoice, "0.85", SuggestionStatus.CONFIRMED);

        entityManager.persistAndFlush(suggestion1);
        entityManager.persistAndFlush(suggestion2);

        // When
        List<ReconciliationSuggestion> pendingSuggestions = suggestionRepository.findByPaymentIdAndStatus(
            testPayment.getId(),
            SuggestionStatus.PENDING
        );

        // Then
        assertThat(pendingSuggestions).hasSize(1);
        assertThat(pendingSuggestions.get(0).getStatus()).isEqualTo(SuggestionStatus.PENDING);
    }

    @Test
    void shouldFindSuggestionsByPaymentId() {
        // Given
        ReconciliationSuggestion suggestion1 = createSuggestion(testPayment, testInvoice, "0.90", SuggestionStatus.PENDING);
        ReconciliationSuggestion suggestion2 = createSuggestion(testPayment, testInvoice, "0.85", SuggestionStatus.CONFIRMED);

        entityManager.persistAndFlush(suggestion1);
        entityManager.persistAndFlush(suggestion2);

        // When
        List<ReconciliationSuggestion> allSuggestions = suggestionRepository.findByPaymentId(testPayment.getId());

        // Then
        assertThat(allSuggestions).hasSize(2);
    }

    @Test
    void shouldFindSuggestionsByStatus() {
        // Given
        ReconciliationSuggestion suggestion1 = createSuggestion(testPayment, testInvoice, "0.90", SuggestionStatus.PENDING);
        ReconciliationSuggestion suggestion2 = createSuggestion(testPayment, testInvoice, "0.85", SuggestionStatus.PENDING);

        // Create another payment and suggestion with CONFIRMED status
        Payment payment2 = createPayment("3000", "Payment 2");
        entityManager.persistAndFlush(payment2);
        ReconciliationSuggestion suggestion3 = createSuggestion(payment2, testInvoice, "0.95", SuggestionStatus.CONFIRMED);

        entityManager.persistAndFlush(suggestion1);
        entityManager.persistAndFlush(suggestion2);
        entityManager.persistAndFlush(suggestion3);

        // When
        List<ReconciliationSuggestion> pendingSuggestions = suggestionRepository.findByStatus(SuggestionStatus.PENDING);
        List<ReconciliationSuggestion> confirmedSuggestions = suggestionRepository.findByStatus(SuggestionStatus.CONFIRMED);

        // Then
        assertThat(pendingSuggestions).hasSize(2);
        assertThat(confirmedSuggestions).hasSize(1);
    }

    @Test
    void shouldFindHighConfidenceSuggestions() {
        // Given
        ReconciliationSuggestion highConf1 = createSuggestion(testPayment, testInvoice, "0.95", SuggestionStatus.PENDING);
        ReconciliationSuggestion highConf2 = createSuggestion(testPayment, testInvoice, "0.91", SuggestionStatus.PENDING);
        ReconciliationSuggestion lowConf = createSuggestion(testPayment, testInvoice, "0.70", SuggestionStatus.PENDING);

        entityManager.persistAndFlush(highConf1);
        entityManager.persistAndFlush(highConf2);
        entityManager.persistAndFlush(lowConf);

        // When
        List<ReconciliationSuggestion> highConfSuggestions =
            suggestionRepository.findByConfidenceGreaterThanEqualAndStatus(new BigDecimal("0.90"), SuggestionStatus.PENDING);

        // Then
        assertThat(highConfSuggestions).hasSize(2);
        assertThat(highConfSuggestions)
            .allMatch(s -> s.getConfidence().compareTo(new BigDecimal("0.90")) >= 0);
    }

    @Test
    void shouldUpdateSuggestionStatus() {
        // Given
        ReconciliationSuggestion suggestion = createSuggestion(testPayment, testInvoice, "0.92", SuggestionStatus.PENDING);
        entityManager.persistAndFlush(suggestion);

        // When
        suggestion.setStatus(SuggestionStatus.CONFIRMED);
        suggestion.setConfirmedBy(testUser);
        suggestionRepository.save(suggestion);
        entityManager.flush();

        // Then
        ReconciliationSuggestion updated = entityManager.find(ReconciliationSuggestion.class, suggestion.getId());
        assertThat(updated.getStatus()).isEqualTo(SuggestionStatus.CONFIRMED);
        assertThat(updated.getConfirmedBy().getId()).isEqualTo(testUser.getId());
        assertThat(updated.getConfirmedAt()).isNotNull();
    }

    private ReconciliationSuggestion createSuggestion(Payment payment, Invoice invoice,
                                                     String confidence, SuggestionStatus status) {
        ReconciliationSuggestion suggestion = new ReconciliationSuggestion();
        suggestion.setPayment(payment);
        suggestion.setInvoice(invoice);
        suggestion.setConfidence(new BigDecimal(confidence));
        suggestion.setReasoning("AI reasoning for confidence " + confidence);
        suggestion.setStatus(status);
        suggestion.setAiModel("gpt-4o-mini");
        return suggestion;
    }

    private Payment createPayment(String amount, String remark) {
        Payment payment = new Payment();
        payment.setUser(testUser);
        payment.setAmount(new BigDecimal(amount));
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMode(PaymentMode.CASH);
        payment.setRemark(remark);
        payment.setStatus(PaymentStatus.UNRECONCILED);
        return payment;
    }
}
