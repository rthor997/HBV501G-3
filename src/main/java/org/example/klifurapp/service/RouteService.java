package org.example.klifurapp.service;

import org.example.klifurapp.DTO.route.RouteDetailsResponseDTO;

public interface RouteService {

    RouteDetailsResponseDTO getRouteDetails(Long routeId);
}