package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.klifurapp.entity.GradingSystem;

public record CreateColourGradeRequestDTO(
        @NotBlank @Size(max = 50) String colour,
        @NotNull GradingSystem gradingSystem,
        @NotBlank String minGrade,
        @NotBlank String maxGrade
) {}
