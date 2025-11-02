package com.mybillbook.controller;

import com.mybillbook.model.ReconciliationSuggestion;
import com.mybillbook.service.ReconciliationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
@Tag(name = "Reconciliation", description = "AI-powered reconciliation endpoints")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    @PostMapping("/run")
    @Operation(summary = "Run AI reconciliation", description = "Process all unreconciled payments using AI to find matching invoices")
    public ResponseEntity<Map<String, Object>> runReconciliation(@RequestParam Long userId) {
        int suggestionsGenerated = reconciliationService.runReconciliation(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("suggestionsGenerated", suggestionsGenerated);
        response.put("message", "AI reconciliation completed successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get pending suggestions", description = "Retrieve all pending AI suggestions for a user")
    public ResponseEntity<List<ReconciliationSuggestion>> getPendingSuggestions(@RequestParam Long userId) {
        List<ReconciliationSuggestion> suggestions = reconciliationService.getPendingSuggestions(userId);
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/confirm/{suggestionId}")
    @Operation(summary = "Confirm suggestion", description = "Confirm an AI suggestion and update invoice/payment status")
    public ResponseEntity<Map<String, String>> confirmSuggestion(
            @PathVariable Long suggestionId,
            @RequestParam Long userId) {

        reconciliationService.confirmSuggestion(suggestionId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reconciliation confirmed successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reject/{suggestionId}")
    @Operation(summary = "Reject suggestion", description = "Reject an AI suggestion")
    public ResponseEntity<Map<String, String>> rejectSuggestion(@PathVariable Long suggestionId) {
        reconciliationService.rejectSuggestion(suggestionId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Suggestion rejected successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-confirm")
    @Operation(summary = "Bulk confirm suggestions", description = "Confirm multiple suggestions at once")
    public ResponseEntity<Map<String, Object>> bulkConfirm(
            @RequestParam Long userId,
            @RequestBody List<Long> suggestionIds) {

        int confirmed = reconciliationService.bulkConfirm(suggestionIds, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("confirmed", confirmed);
        response.put("total", suggestionIds.size());
        response.put("message", "Bulk confirmation completed");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-confirm-high-confidence")
    @Operation(summary = "Bulk confirm high confidence", description = "Auto-confirm all suggestions above confidence threshold")
    public ResponseEntity<Map<String, Object>> bulkConfirmHighConfidence(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0.90") BigDecimal minConfidence) {

        int confirmed = reconciliationService.bulkConfirmHighConfidence(minConfidence, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("confirmed", confirmed);
        response.put("minConfidence", minConfidence);
        response.put("message", "High confidence suggestions confirmed automatically");

        return ResponseEntity.ok(response);
    }
}
