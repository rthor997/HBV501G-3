package org.example.klifurapp.DTO.climb;

import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.WallIncline;

public record AdminUpdateRouteDTO(
        String name,
        GradingSystem gradingSystem,
        String difficultyGrade,
        String location,
        ClimbingStyle climbingStyle,
        WallIncline wallIncline,
        Boolean indoor,
        String characteristics
) {}