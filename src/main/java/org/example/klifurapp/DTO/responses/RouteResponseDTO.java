package org.example.klifurapp.DTO.responses;

public record RouteResponseDTO(
        Long id,
        String name,
        String difficultyGrade,
        String gradingSystem,
        String climbingStyle,
        String wallIncline,
        String characteristics,
        Boolean indoor,
        String location
) {
}
