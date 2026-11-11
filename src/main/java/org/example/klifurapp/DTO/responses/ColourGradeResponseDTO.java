package org.example.klifurapp.DTO.responses;

import org.example.klifurapp.entity.GradingSystem;

public record ColourGradeResponseDTO(
        Long id,
        Long presetId,
        String colour,
        GradingSystem gradingSystem,
        String minGrade,
        String maxGrade
) {}
