package com.mybillbook.repository;

import com.mybillbook.enums.SuggestionStatus;
import com.mybillbook.model.ReconciliationSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReconciliationSuggestionRepository extends JpaRepository<ReconciliationSuggestion, Long> {

    List<ReconciliationSuggestion> findByPaymentIdAndStatus(Long paymentId, SuggestionStatus status);

    boolean existsByPaymentIdAndStatus(Long paymentId, SuggestionStatus status);

    List<ReconciliationSuggestion> findByPaymentId(Long paymentId);

    List<ReconciliationSuggestion> findByStatus(SuggestionStatus status);

    List<ReconciliationSuggestion> findByConfidenceGreaterThanEqualAndStatus(BigDecimal confidence, SuggestionStatus status);
}
