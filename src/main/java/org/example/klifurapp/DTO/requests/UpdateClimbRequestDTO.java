package org.example.klifurapp.DTO.requests;

import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.WallIncline;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateClimbRequestDTO(

        GradingSystem gradingSystem,

        @Pattern(regexp = ".*\\S.*", message = "Difficulty grade must not be blank")
        String difficultyGrade,

        ClimbingStyle climbingStyle,

        LocalDate date,

        @Size(max = 2000, message = "Notes must be at most 2000 characters")
        String notes,

        WallIncline wallIncline
) {}
