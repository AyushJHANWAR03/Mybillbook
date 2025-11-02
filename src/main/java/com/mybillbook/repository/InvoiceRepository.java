package com.mybillbook.repository;

import com.mybillbook.enums.InvoiceStatus;
import com.mybillbook.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByUserIdAndStatus(Long userId, InvoiceStatus status);

    List<Invoice> findByUserIdAndStatusIn(Long userId, List<InvoiceStatus> statuses);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
