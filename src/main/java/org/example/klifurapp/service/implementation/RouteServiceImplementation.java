package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.climb.AdminUpdateRouteDTO;
import org.example.klifurapp.entity.Route;
import org.example.klifurapp.repository.RouteRepository;
import org.example.klifurapp.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

        if(dto.grade() != null) {
            route.setGrade(dto.grade());
        }
        return routeRepository.save(route);
    }

}
