package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.AdminUpdateRouteDTO;
import org.example.klifurapp.DTO.requests.CreateRouteRequestDTO;
import org.example.klifurapp.DTO.responses.RouteDetailsResponseDTO;
import org.example.klifurapp.DTO.responses.RouteResponseDTO;
import org.example.klifurapp.entity.Route;
import org.example.klifurapp.service.RouteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponseDTO> createRoute(
            @Valid @RequestBody CreateRouteRequestDTO dto) {

        RouteResponseDTO createRoute =
                routeService.createRoute(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createRoute);
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteDetailsResponseDTO> getRouteDetails(
            @PathVariable Long routeId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        RouteDetailsResponseDTO route =
                routeService.getRouteDetails(routeId, userId);

        return ResponseEntity.ok(route);
    }

    @PutMapping("/admin/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Route> updateRouteAsAdmin(
            @PathVariable Long routeId,
            @RequestBody AdminUpdateRouteDTO dto) {

        Route updatedRoute =
                routeService.updateRouteAsAdmin(routeId, dto);

        return ResponseEntity.ok(updatedRoute);
    }

    @DeleteMapping("/admin/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRouteAsAdmin(
            @PathVariable Long routeId) {
        routeService.deleteRouteAsAdmin(routeId);
        return ResponseEntity.noContent().build();

    }

}
