package com.ledger.os.controller;

import com.ledger.os.dto.BudgetDto;
import com.ledger.os.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final LedgerService ledgerService;

    public BudgetController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public ResponseEntity<List<BudgetDto>> getBudgets(
            @RequestParam(required = false) String month
    ) {
        YearMonth period = month != null && !month.isBlank()
                ? YearMonth.parse(month.trim())
                : YearMonth.now();

        List<BudgetDto> budgets = ledgerService.getBudgets(period);
        return ResponseEntity.ok(budgets);
    }

    @PutMapping
    public ResponseEntity<BudgetDto> setBudget(
            @Valid @RequestBody BudgetDto dto
    ) {
        BudgetDto saved = ledgerService.setBudget(dto);
        return ResponseEntity.ok(saved);
    }
}
