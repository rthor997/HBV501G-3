package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.Pattern;
import org.example.klifurapp.entity.ClimbingStyle;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.WallIncline;

public record AdminUpdateRouteDTO(

        @Pattern(regexp = ".*\\S.*", message = "Route name must not be blank")
        String name,

        GradingSystem gradingSystem,

        @Pattern(regexp = ".*\\S.*", message = "Difficulty grade must not be blank")
        String difficultyGrade,

        @Pattern(regexp = ".*\\S.*", message = "Location must not be blank")
        String location,
        ClimbingStyle climbingStyle,
        WallIncline wallIncline,
        Boolean indoor,
        String characteristics
) {}