package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.WallIncline;

public record CreateRouteRequestDTO(

        @NotBlank
        String name,

        @NotNull
        GradingSystem gradingSystem,

        @NotBlank
        String difficultyGrade,

        @NotBlank
        String location,

        @NotNull
        ClimbingStyle climbingStyle,

        WallIncline wallIncline,

        @NotNull
        Boolean indoor,

        String characteristics
) {}
