
package org.example.klifurapp.Controller;

import org.example.klifurapp.DTO.route.RouteDetailsResponseDTO;
import org.example.klifurapp.DTO.climb.AdminUpdateRouteDTO;
import org.example.klifurapp.entity.Route;
import org.example.klifurapp.service.RouteService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    // UC12 - View Route Details
    @GetMapping("/{routeId}")
    public ResponseEntity<RouteDetailsResponseDTO> getRouteDetails(
            @PathVariable Long routeId) {

        RouteDetailsResponseDTO route =
                routeService.getRouteDetails(routeId);

        return ResponseEntity.ok(route);
    }

    // Admin - Update Route
    @PutMapping("/admin/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Route> updateRouteAsAdmin(
            @PathVariable Long routeId,
            @RequestBody AdminUpdateRouteDTO dto) {

        Route updatedRoute =
                routeService.updateRouteAsAdmin(routeId, dto);

        return ResponseEntity.ok(updatedRoute);
    }
}
