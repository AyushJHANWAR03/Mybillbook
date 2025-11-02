package com.mybillbook.repository;

import com.mybillbook.enums.InvoiceStatus;
import com.mybillbook.model.Invoice;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InvoiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InvoiceRepository invoiceRepository;

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
    void shouldSaveAndRetrieveInvoice() {
        // Given
        Invoice invoice = new Invoice();
        invoice.setUser(testUser);
        invoice.setInvoiceNumber("INV001");
        invoice.setCustomerName("Suresh Traders");
        invoice.setTotalAmount(new BigDecimal("10000.00"));
        invoice.setPendingAmount(new BigDecimal("10000.00"));
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setInvoiceDate(LocalDate.of(2025, 1, 15));

        // When
        Invoice savedInvoice = invoiceRepository.save(invoice);
        Invoice foundInvoice = entityManager.find(Invoice.class, savedInvoice.getId());

        // Then
        assertThat(foundInvoice).isNotNull();
        assertThat(foundInvoice.getInvoiceNumber()).isEqualTo("INV001");
        assertThat(foundInvoice.getCustomerName()).isEqualTo("Suresh Traders");
        assertThat(foundInvoice.getTotalAmount()).isEqualByComparingTo("10000.00");
        assertThat(foundInvoice.getPendingAmount()).isEqualByComparingTo("10000.00");
        assertThat(foundInvoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        assertThat(foundInvoice.getCreatedAt()).isNotNull();
        assertThat(foundInvoice.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindInvoicesByUserIdAndStatus() {
        // Given
        Invoice invoice1 = createInvoice("INV001", "Customer1", "10000", "10000", InvoiceStatus.UNPAID);
        Invoice invoice2 = createInvoice("INV002", "Customer2", "20000", "10000", InvoiceStatus.PARTIALLY_PAID);
        Invoice invoice3 = createInvoice("INV003", "Customer3", "15000", "0", InvoiceStatus.FULLY_PAID);

        entityManager.persistAndFlush(invoice1);
        entityManager.persistAndFlush(invoice2);
        entityManager.persistAndFlush(invoice3);

        // When
        List<Invoice> unpaidInvoices = invoiceRepository.findByUserIdAndStatus(testUser.getId(), InvoiceStatus.UNPAID);
        List<Invoice> partiallyPaidInvoices = invoiceRepository.findByUserIdAndStatus(testUser.getId(), InvoiceStatus.PARTIALLY_PAID);

        // Then
        assertThat(unpaidInvoices).hasSize(1);
        assertThat(unpaidInvoices.get(0).getInvoiceNumber()).isEqualTo("INV001");

        assertThat(partiallyPaidInvoices).hasSize(1);
        assertThat(partiallyPaidInvoices.get(0).getInvoiceNumber()).isEqualTo("INV002");
    }

    @Test
    void shouldFindInvoicesByUserIdAndStatusIn() {
        // Given
        Invoice invoice1 = createInvoice("INV001", "Customer1", "10000", "10000", InvoiceStatus.UNPAID);
        Invoice invoice2 = createInvoice("INV002", "Customer2", "20000", "10000", InvoiceStatus.PARTIALLY_PAID);
        Invoice invoice3 = createInvoice("INV003", "Customer3", "15000", "0", InvoiceStatus.FULLY_PAID);

        entityManager.persistAndFlush(invoice1);
        entityManager.persistAndFlush(invoice2);
        entityManager.persistAndFlush(invoice3);

        // When
        List<Invoice> pendingInvoices = invoiceRepository.findByUserIdAndStatusIn(
            testUser.getId(),
            List.of(InvoiceStatus.UNPAID, InvoiceStatus.PARTIALLY_PAID)
        );

        // Then
        assertThat(pendingInvoices).hasSize(2);
        assertThat(pendingInvoices)
            .extracting(Invoice::getInvoiceNumber)
            .containsExactlyInAnyOrder("INV001", "INV002");
    }

    @Test
    void shouldFindInvoiceByInvoiceNumber() {
        // Given
        Invoice invoice = createInvoice("INV123", "Customer XYZ", "5000", "5000", InvoiceStatus.UNPAID);
        entityManager.persistAndFlush(invoice);

        // When
        Optional<Invoice> foundInvoice = invoiceRepository.findByInvoiceNumber("INV123");

        // Then
        assertThat(foundInvoice).isPresent();
        assertThat(foundInvoice.get().getCustomerName()).isEqualTo("Customer XYZ");
    }

    @Test
    void shouldReturnEmptyWhenInvoiceNumberNotFound() {
        // When
        Optional<Invoice> foundInvoice = invoiceRepository.findByInvoiceNumber("NONEXISTENT");

        // Then
        assertThat(foundInvoice).isEmpty();
    }

    @Test
    void shouldUpdateInvoiceStatus() {
        // Given
        Invoice invoice = createInvoice("INV001", "Customer1", "10000", "10000", InvoiceStatus.UNPAID);
        entityManager.persistAndFlush(invoice);

        // When
        invoice.setPendingAmount(new BigDecimal("5000.00"));
        invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoice);
        entityManager.flush();

        // Then
        Invoice updatedInvoice = entityManager.find(Invoice.class, invoice.getId());
        assertThat(updatedInvoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        assertThat(updatedInvoice.getPendingAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void shouldNotAllowDuplicateInvoiceNumbers() {
        // Given
        Invoice invoice1 = createInvoice("INV001", "Customer1", "10000", "10000", InvoiceStatus.UNPAID);
        entityManager.persistAndFlush(invoice1);

        Invoice invoice2 = createInvoice("INV001", "Customer2", "20000", "20000", InvoiceStatus.UNPAID);

        // When & Then
        try {
            invoiceRepository.save(invoice2);
            entityManager.flush();
            assertThat(false).as("Should have thrown constraint violation").isTrue();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    private Invoice createInvoice(String invoiceNumber, String customerName,
                                  String totalAmount, String pendingAmount, InvoiceStatus status) {
        Invoice invoice = new Invoice();
        invoice.setUser(testUser);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setCustomerName(customerName);
        invoice.setTotalAmount(new BigDecimal(totalAmount));
        invoice.setPendingAmount(new BigDecimal(pendingAmount));
        invoice.setStatus(status);
        invoice.setInvoiceDate(LocalDate.now());
        return invoice;
    }
}
