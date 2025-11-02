package com.mybillbook.controller;

import com.mybillbook.enums.InvoiceStatus;
import com.mybillbook.model.Invoice;
import com.mybillbook.model.User;
import com.mybillbook.service.AuthService;
import com.mybillbook.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final AuthService authService;

    @PostMapping("/upload")
    @Operation(summary = "Upload invoices", description = "Bulk upload invoices for a user")
    public ResponseEntity<Map<String, Object>> uploadInvoices(
            @RequestParam Long userId,
            @RequestBody List<Invoice> invoices) {

        User user = authService.getUserById(userId);
        List<Invoice> savedInvoices = invoiceService.uploadInvoices(invoices, user);

        Map<String, Object> response = new HashMap<>();
        response.put("uploaded", savedInvoices.size());
        response.put("failed", 0);
        response.put("message", "Invoices uploaded successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get invoices", description = "Get invoices by user and optional status filter")
    public ResponseEntity<List<Invoice>> getInvoices(
            @RequestParam Long userId,
            @RequestParam(required = false) InvoiceStatus status) {

        List<Invoice> invoices = invoiceService.getInvoicesByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all invoices", description = "Get all invoices for a user")
    public ResponseEntity<List<Invoice>> getAllInvoices(@RequestParam Long userId) {
        List<Invoice> invoices = invoiceService.getAllInvoices(userId);
        return ResponseEntity.ok(invoices);
    }
}
