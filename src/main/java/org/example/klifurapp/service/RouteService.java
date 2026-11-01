package org.example.klifurapp.service;

import org.example.klifurapp.DTO.climb.AdminUpdateRouteDTO;
import org.example.klifurapp.entity.Route;

public interface RouteService {
    Route updateRouteAsAdmin(Long routeId, AdminUpdateRouteDTO dto);
}
