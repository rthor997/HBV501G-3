package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByIdAndActiveTrue(Long id);
}