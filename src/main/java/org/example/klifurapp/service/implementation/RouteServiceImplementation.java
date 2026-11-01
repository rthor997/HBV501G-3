package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.climb.AdminUpdateRouteDTO;
import org.example.klifurapp.entity.Route;
import org.example.klifurapp.repository.RouteRepository;
import org.example.klifurapp.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RouteServiceImplementation implements RouteService {
    private final RouteRepository routeRepository;

    public RouteServiceImplementation(
            RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public Route updateRouteAsAdmin(
            Long routeId,
            AdminUpdateRouteDTO dto) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Route not found"
                        )
                );
        if (dto.name() != null) {
            route.setName(dto.name());
        }

        if (dto.gradingSystem() != null) {
            route.setGradingSystem(dto.gradingSystem());
        }

        if(dto.difficultyGrade() != null) {
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

        if(dto.characteristics() != null) {
            route.setCharacteristics(dto.characteristics());
        }
        return routeRepository.save(route);
    }

}
