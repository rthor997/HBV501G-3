package org.example.klifurapp.Controller;

import org.example.klifurapp.DTO.route.RouteDetailsResponseDTO;
import org.example.klifurapp.service.RouteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteDetailsResponseDTO> getRouteDetails(
            @PathVariable Long routeId) {

        RouteDetailsResponseDTO route =
                routeService.getRouteDetails(routeId);

        return ResponseEntity.ok(route);
    }
}