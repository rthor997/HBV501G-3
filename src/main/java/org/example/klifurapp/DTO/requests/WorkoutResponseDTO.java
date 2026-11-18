package org.example.klifurapp.DTO.requests;

import java.util.List;

public record WorkoutResponseDTO(
        Long id,
        String exerciseName,
        Integer sets,
        Integer repetitions,
        Double weight,
        Integer durationMinutes,
        List<String> muscleGroups
) {}