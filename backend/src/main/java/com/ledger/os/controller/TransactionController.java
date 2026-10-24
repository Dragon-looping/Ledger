package com.ledger.os.controller;

import com.ledger.os.dto.CreateTransactionRequestDto;
import com.ledger.os.dto.TransactionResponseDto;
import com.ledger.os.factory.TransactionType;
import com.ledger.os.model.Category;
import com.ledger.os.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getAllTransactions(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) TransactionType type
    ) {
        YearMonth period = month != null && !month.isBlank() ? YearMonth.parse(month.trim()) : null;
        List<TransactionResponseDto> results = ledgerService.getTransactions(period, category, type);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable Long id) {
        TransactionResponseDto tx = ledgerService.getTransactionById(id);
        return ResponseEntity.ok(tx);
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> createTransaction(
            @Valid @RequestBody CreateTransactionRequestDto dto
    ) {
        TransactionResponseDto created = ledgerService.createTransaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody CreateTransactionRequestDto dto
    ) {
        TransactionResponseDto updated = ledgerService.updateTransaction(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        ledgerService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
