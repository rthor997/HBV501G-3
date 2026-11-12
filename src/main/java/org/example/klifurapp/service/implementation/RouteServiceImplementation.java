
package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateRouteDTO;
import org.example.klifurapp.DTO.requests.CreateRouteRequestDTO;
import org.example.klifurapp.DTO.responses.RouteDetailsResponseDTO;
import org.example.klifurapp.DTO.responses.RouteFriendDTO;
import org.example.klifurapp.DTO.responses.RouteResponseDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.Route;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.RouteRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.RouteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteServiceImplementation implements RouteService {

    private final RouteRepository routeRepository;
    private final ClimbRepository climbRepository;
    private final UserRepository userRepository;

    public RouteServiceImplementation(
            RouteRepository routeRepository,
            ClimbRepository climbRepository,
            UserRepository userRepository
    ) {
        this.routeRepository = routeRepository;
        this.climbRepository = climbRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RouteResponseDTO createRoute(CreateRouteRequestDTO dto) {
        Route route = new Route();

        route.setName(dto.name());
        route.setGradingSystem(dto.gradingSystem());
        route.setDifficultyGrade(dto.difficultyGrade());
        route.setLocation(dto.location());
        route.setClimbingStyle(dto.climbingStyle());
        route.setWallIncline(dto.wallIncline());
        route.setIndoor(dto.indoor());
        route.setCharacteristics(dto.characteristics());
        route.setActive(true);

        Route savedRoute = routeRepository.save(route);

        return new RouteResponseDTO(
                savedRoute.getId(),
                savedRoute.getName(),
                savedRoute.getDifficultyGrade(),
                savedRoute.getGradingSystem().name(),
                savedRoute.getClimbingStyle().name(),
                savedRoute.getWallIncline() != null ? savedRoute.getWallIncline().name():null,
                savedRoute.getCharacteristics(),
                savedRoute.isIndoor(),
                savedRoute.getLocation()
        );
    }

    // UC12 - View Route Details
    @Override
    @Transactional(readOnly = true)
    public RouteDetailsResponseDTO getRouteDetails(Long routeId, Long currentUserId) {

        Route route = routeRepository.findByIdAndActiveTrue(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found")
                );

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")
                );

        List<Long> friendIds = currentUser.getFriends()
                .stream()
                .map(User::getId)
                .toList();

        List<RouteFriendDTO> friendsWhoCompleted = friendIds.isEmpty()
                ? List.of()
                : climbRepository.findByRouteIdAndUserIdIn(routeId, friendIds)
                .stream()
                .map(Climb::getUser)
                .distinct()
                .map(user -> new RouteFriendDTO(
                        user.getId(),
                        user.getUsername()
                ))
                .toList();

        return new RouteDetailsResponseDTO(
                route.getId(),
                route.getName(),
                route.getDifficultyGrade(),
                route.getGradingSystem().name(),
                route.getClimbingStyle().name(),
                route.getWallIncline() != null
                        ? route.getWallIncline().name()
                        : null,
                route.getCharacteristics(),
                route.isIndoor(),
                route.getLocation(),
                friendsWhoCompleted
        );
    }

    // Admin - Update Route
    @Override
    public Route updateRouteAsAdmin(
            Long routeId,
            AdminUpdateRouteDTO dto
    ) {
        Route route = routeRepository.findByIdAndActiveTrue(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found")
                );

        if (dto.name() != null) {
            route.setName(dto.name());
        }

        if (dto.gradingSystem() != null) {
            route.setGradingSystem(dto.gradingSystem());
        }

        if (dto.difficultyGrade() != null) {
            route.setDifficultyGrade(dto.difficultyGrade());
        }

        if (dto.location() != null) {
            route.setLocation(dto.location());
        }

        if (dto.climbingStyle() != null) {
            route.setClimbingStyle(dto.climbingStyle());
        }

        if (dto.wallIncline() != null) {
            route.setWallIncline(dto.wallIncline());
        }

        if (dto.indoor() != null) {
            route.setIndoor(dto.indoor());
        }

        if (dto.characteristics() != null) {
            route.setCharacteristics(dto.characteristics());
        }

        return routeRepository.save(route);
    }

    @Override
    public void deleteRouteAsAdmin(Long routeId) {
        Route route = routeRepository.findByIdAndActiveTrue(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found")
                );
        route.setActive(false);
        routeRepository.save(route);
    }
}
