package com.mybillbook.controller;

import com.mybillbook.enums.PaymentStatus;
import com.mybillbook.model.Payment;
import com.mybillbook.model.User;
import com.mybillbook.service.AuthService;
import com.mybillbook.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final AuthService authService;

    @PostMapping("/upload")
    @Operation(summary = "Upload payments", description = "Bulk upload payments for a user")
    public ResponseEntity<Map<String, Object>> uploadPayments(
            @RequestParam Long userId,
            @RequestBody List<Payment> payments) {

        User user = authService.getUserById(userId);
        List<Payment> savedPayments = paymentService.uploadPayments(payments, user);

        Map<String, Object> response = new HashMap<>();
        response.put("uploaded", savedPayments.size());
        response.put("failed", 0);
        response.put("message", "Payments uploaded successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get payments", description = "Get payments by user and optional status filter")
    public ResponseEntity<List<Payment>> getPayments(
            @RequestParam Long userId,
            @RequestParam(required = false) PaymentStatus status) {

        List<Payment> payments = paymentService.getPaymentsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all payments", description = "Get all payments for a user")
    public ResponseEntity<List<Payment>> getAllPayments(@RequestParam Long userId) {
        List<Payment> payments = paymentService.getAllPayments(userId);
        return ResponseEntity.ok(payments);
    }
}
