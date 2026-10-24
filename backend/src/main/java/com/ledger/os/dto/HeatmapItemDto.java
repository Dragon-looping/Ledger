package com.ledger.os.dto;

import java.math.BigDecimal;

public record HeatmapItemDto(
        String date,
        BigDecimal amount,
        int count,
        int level
) {
}
