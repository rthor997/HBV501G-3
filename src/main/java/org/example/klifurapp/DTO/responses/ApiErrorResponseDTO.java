package org.example.klifurapp.DTO.responses;

import java.time.Instant;

public record ApiErrorResponseDTO(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path
) {}