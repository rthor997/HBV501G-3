package org.example.klifurapp.DTO.requests;

public record WorkoutResponseDTO(
        Long id,
        String exerciseName,
        Integer sets,
        Integer repetitions,
        Double weight,
        Integer durationMinutes
) {}