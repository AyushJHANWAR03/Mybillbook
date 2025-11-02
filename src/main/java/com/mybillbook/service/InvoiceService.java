package com.mybillbook.service;

import com.mybillbook.enums.InvoiceStatus;
import com.mybillbook.model.Invoice;
import com.mybillbook.model.User;
import com.mybillbook.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public List<Invoice> uploadInvoices(List<Invoice> invoices, User user) {
        invoices.forEach(invoice -> {
            invoice.setUser(user);
            if (invoice.getStatus() == null) {
                invoice.setStatus(InvoiceStatus.UNPAID);
            }
            if (invoice.getPendingAmount() == null) {
                invoice.setPendingAmount(invoice.getTotalAmount());
            }
        });

        List<Invoice> saved = invoiceRepository.saveAll(invoices);
        log.info("Uploaded {} invoices for user {}", saved.size(), user.getId());
        return saved;
    }

    public List<Invoice> getInvoicesByUserIdAndStatus(Long userId, InvoiceStatus status) {
        if (status != null) {
            return invoiceRepository.findByUserIdAndStatus(userId, status);
        }
        return invoiceRepository.findByUserIdAndStatusIn(userId, List.of(InvoiceStatus.values()));
    }

    public List<Invoice> getAllInvoices(Long userId) {
        return invoiceRepository.findByUserIdAndStatusIn(userId, List.of(InvoiceStatus.values()));
    }
}
