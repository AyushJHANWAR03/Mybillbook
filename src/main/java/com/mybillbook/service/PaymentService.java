package com.mybillbook.service;

import com.mybillbook.enums.PaymentStatus;
import com.mybillbook.model.Payment;
import com.mybillbook.model.User;
import com.mybillbook.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public List<Payment> uploadPayments(List<Payment> payments, User user) {
        payments.forEach(payment -> {
            payment.setUser(user);
            if (payment.getStatus() == null) {
                payment.setStatus(PaymentStatus.UNRECONCILED);
            }
        });

        List<Payment> saved = paymentRepository.saveAll(payments);
        log.info("Uploaded {} payments for user {}", saved.size(), user.getId());
        return saved;
    }

    public List<Payment> getPaymentsByUserIdAndStatus(Long userId, PaymentStatus status) {
        if (status != null) {
            return paymentRepository.findByUserIdAndStatus(userId, status);
        }
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getAllPayments(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
}
