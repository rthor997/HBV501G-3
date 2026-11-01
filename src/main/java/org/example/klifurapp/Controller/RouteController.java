package org.example.klifurapp.Controller;

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

    @PutMapping("/admin/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Route> updateRouteAsAdmin(
            @PathVariable Long routeId,
            @RequestBody AdminUpdateRouteDTO dto) {
        Route updateRoute =
                routeService.updateRouteAsAdmin(routeId, dto);
        return ResponseEntity.ok(updateRoute);
    }
}
