package org.example.klifurapp.DTO.requests;

import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.WallIncline;

import java.time.LocalDate;

public record ClimbResponseDTO(
        Long id,
        GradingSystem gradingSystem,
        String difficultyGrade,
        ClimbingStyle climbingStyle,
        LocalDate date,
        String notes,
        WallIncline wallIncline
) {}