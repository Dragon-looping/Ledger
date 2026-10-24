package com.ledger.os.controller;

import com.ledger.os.dto.HeatmapItemDto;
import com.ledger.os.dto.MonthlySummaryDto;
import com.ledger.os.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final LedgerService ledgerService;

    public AnalyticsController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryDto> getSummary(
            @RequestParam(required = false) String month
    ) {
        YearMonth period = month != null && !month.isBlank()
                ? YearMonth.parse(month.trim())
                : YearMonth.now();

        MonthlySummaryDto summary = ledgerService.getMonthlySummary(period);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapItemDto>> getHeatmap(
            @RequestParam(required = false) Integer year
    ) {
        int targetYear = year != null ? year : LocalDate.now().getYear();
        List<HeatmapItemDto> heatmap = ledgerService.getYearlyHeatmap(targetYear);
        return ResponseEntity.ok(heatmap);
    }
}
