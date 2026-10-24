package com.ledger.os.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponseDto(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    public static ErrorResponseDto of(int status, String error, String message, String path) {
        return new ErrorResponseDto(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponseDto of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponseDto(Instant.now(), status, error, message, path, fieldErrors);
    }
}
