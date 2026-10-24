package com.ledger.os.dto;

import jakarta.validation.constraints.NotBlank;

public record ParseRequestDto(
        @NotBlank(message = "Text input cannot be empty")
        String text
) {
}
