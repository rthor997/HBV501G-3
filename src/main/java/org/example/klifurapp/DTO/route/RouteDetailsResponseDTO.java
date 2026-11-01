package org.example.klifurapp.DTO.route;

import java.util.List;

public record RouteDetailsResponseDTO(
        Long id,
        String name,
        String difficultyGrade,
        String gradingSystem,
        String climbingStyle,
        String wallIncline,
        String characteristics,
        Boolean indoor,
        String location,
        List<RouteFriendDTO> friendsWhoCompleted
) {
}