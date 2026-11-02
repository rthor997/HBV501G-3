package org.example.klifurapp.DTO.responses;

import java.time.LocalDate;

public record ClimbSummaryResponseDTO(
        Long id,
        String difficultyGrade,
        LocalDate date
) {}