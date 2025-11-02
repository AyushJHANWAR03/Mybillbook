package com.mybillbook.repository;

import com.mybillbook.enums.PaymentMode;
import com.mybillbook.enums.PaymentStatus;
import com.mybillbook.model.Payment;
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
class PaymentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PaymentRepository paymentRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setMobileNumber("9876543210");
        testUser.setName("Ramesh Kumar");
        testUser.setBusinessName("Ramesh Traders");
        entityManager.persistAndFlush(testUser);
    }

    @Test
    void shouldSaveAndRetrievePayment() {
        // Given
        Payment payment = new Payment();
        payment.setUser(testUser);
        payment.setAmount(new BigDecimal("5000.00"));
        payment.setPaymentDate(LocalDate.of(2025, 1, 20));
        payment.setPaymentMode(PaymentMode.UPI);
        payment.setRemark("INV001 partial payment");
        payment.setStatus(PaymentStatus.UNRECONCILED);

        // When
        Payment savedPayment = paymentRepository.save(payment);
        Payment foundPayment = entityManager.find(Payment.class, savedPayment.getId());

        // Then
        assertThat(foundPayment).isNotNull();
        assertThat(foundPayment.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(foundPayment.getPaymentDate()).isEqualTo(LocalDate.of(2025, 1, 20));
        assertThat(foundPayment.getPaymentMode()).isEqualTo(PaymentMode.UPI);
        assertThat(foundPayment.getRemark()).isEqualTo("INV001 partial payment");
        assertThat(foundPayment.getStatus()).isEqualTo(PaymentStatus.UNRECONCILED);
        assertThat(foundPayment.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindPaymentsByUserIdAndStatus() {
        // Given
        Payment payment1 = createPayment("5000", PaymentMode.UPI, "Payment 1", PaymentStatus.UNRECONCILED);
        Payment payment2 = createPayment("10000", PaymentMode.CASH, "Payment 2", PaymentStatus.UNRECONCILED);
        Payment payment3 = createPayment("7000", PaymentMode.CARD, "Payment 3", PaymentStatus.RECONCILED);

        entityManager.persistAndFlush(payment1);
        entityManager.persistAndFlush(payment2);
        entityManager.persistAndFlush(payment3);

        // When
        List<Payment> unreconciledPayments = paymentRepository.findByUserIdAndStatus(
            testUser.getId(),
            PaymentStatus.UNRECONCILED
        );
        List<Payment> reconciledPayments = paymentRepository.findByUserIdAndStatus(
            testUser.getId(),
            PaymentStatus.RECONCILED
        );

        // Then
        assertThat(unreconciledPayments).hasSize(2);
        assertThat(unreconciledPayments)
            .extracting(Payment::getRemark)
            .containsExactlyInAnyOrder("Payment 1", "Payment 2");

        assertThat(reconciledPayments).hasSize(1);
        assertThat(reconciledPayments.get(0).getRemark()).isEqualTo("Payment 3");
    }

    @Test
    void shouldFindPaymentsByUserId() {
        // Given
        Payment payment1 = createPayment("5000", PaymentMode.UPI, "Payment 1", PaymentStatus.UNRECONCILED);
        Payment payment2 = createPayment("10000", PaymentMode.CASH, "Payment 2", PaymentStatus.RECONCILED);

        entityManager.persistAndFlush(payment1);
        entityManager.persistAndFlush(payment2);

        // When
        List<Payment> allPayments = paymentRepository.findByUserId(testUser.getId());

        // Then
        assertThat(allPayments).hasSize(2);
    }

    @Test
    void shouldUpdatePaymentStatus() {
        // Given
        Payment payment = createPayment("5000", PaymentMode.UPI, "Test payment", PaymentStatus.UNRECONCILED);
        entityManager.persistAndFlush(payment);

        // When
        payment.setStatus(PaymentStatus.RECONCILED);
        paymentRepository.save(payment);
        entityManager.flush();

        // Then
        Payment updatedPayment = entityManager.find(Payment.class, payment.getId());
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.RECONCILED);
    }

    @Test
    void shouldSavePaymentWithoutRemark() {
        // Given
        Payment payment = new Payment();
        payment.setUser(testUser);
        payment.setAmount(new BigDecimal("3000.00"));
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMode(PaymentMode.CASH);
        payment.setRemark(null);
        payment.setStatus(PaymentStatus.UNRECONCILED);

        // When
        Payment savedPayment = paymentRepository.save(payment);

        // Then
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getRemark()).isNull();
    }

    @Test
    void shouldHandleDifferentPaymentModes() {
        // Given
        Payment upiPayment = createPayment("1000", PaymentMode.UPI, "UPI", PaymentStatus.UNRECONCILED);
        Payment cashPayment = createPayment("2000", PaymentMode.CASH, "Cash", PaymentStatus.UNRECONCILED);
        Payment cardPayment = createPayment("3000", PaymentMode.CARD, "Card", PaymentStatus.UNRECONCILED);
        Payment bankPayment = createPayment("4000", PaymentMode.BANK_TRANSFER, "Bank", PaymentStatus.UNRECONCILED);

        // When
        paymentRepository.save(upiPayment);
        paymentRepository.save(cashPayment);
        paymentRepository.save(cardPayment);
        paymentRepository.save(bankPayment);
        entityManager.flush();

        // Then
        List<Payment> allPayments = paymentRepository.findByUserId(testUser.getId());
        assertThat(allPayments).hasSize(4);
        assertThat(allPayments)
            .extracting(Payment::getPaymentMode)
            .containsExactlyInAnyOrder(
                PaymentMode.UPI,
                PaymentMode.CASH,
                PaymentMode.CARD,
                PaymentMode.BANK_TRANSFER
            );
    }

    private Payment createPayment(String amount, PaymentMode mode, String remark, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setUser(testUser);
        payment.setAmount(new BigDecimal(amount));
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMode(mode);
        payment.setRemark(remark);
        payment.setStatus(status);
        return payment;
    }
}
