
package org.example.klifurapp.service;

import org.example.klifurapp.DTO.route.RouteDetailsResponseDTO;
import org.example.klifurapp.DTO.requests.AdminUpdateRouteDTO;
import org.example.klifurapp.entity.Route;

public interface RouteService {

    // View Route Details
    RouteDetailsResponseDTO getRouteDetails(Long routeId);

    // Admin - Update Route
    Route updateRouteAsAdmin(Long routeId, AdminUpdateRouteDTO dto);
}
