package com.mybillbook.controller;

import com.mybillbook.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reporting and analytics endpoints")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    @Operation(summary = "Get summary report", description = "Get comprehensive reconciliation summary with stats and AI accuracy")
    public ResponseEntity<Map<String, Object>> getSummaryReport(@RequestParam Long userId) {
        Map<String, Object> report = reportService.getSummaryReport(userId);
        return ResponseEntity.ok(report);
    }
}
