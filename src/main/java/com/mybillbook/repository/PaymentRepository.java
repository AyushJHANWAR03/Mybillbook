package com.mybillbook.repository;

import com.mybillbook.enums.PaymentStatus;
import com.mybillbook.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserIdAndStatus(Long userId, PaymentStatus status);

    List<Payment> findByUserId(Long userId);
}
