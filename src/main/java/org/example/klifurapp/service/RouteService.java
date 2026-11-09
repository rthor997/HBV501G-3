
package org.example.klifurapp.service;

import org.example.klifurapp.DTO.responses.RouteDetailsResponseDTO;
import org.example.klifurapp.DTO.requests.AdminUpdateRouteDTO;
import org.example.klifurapp.entity.Route;

public interface RouteService {

    // View Route Details
    RouteDetailsResponseDTO getRouteDetails(Long routeId, Long currentUserId);
    // Admin - Update Route
    Route updateRouteAsAdmin(Long routeId, AdminUpdateRouteDTO dto);
    void deleteRouteAsAdmin(Long routeId);
}
