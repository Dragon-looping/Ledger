package com.ledger.os.controller;

import com.ledger.os.dto.ParseRequestDto;
import com.ledger.os.dto.ParseResponseDto;
import com.ledger.os.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parse")
public class ParseController {

    private final LedgerService ledgerService;

    public ParseController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<ParseResponseDto> parseTransactionText(
            @Valid @RequestBody ParseRequestDto request
    ) {
        ParseResponseDto result = ledgerService.parseNaturalText(request.text());
        return ResponseEntity.ok(result);
    }
}
