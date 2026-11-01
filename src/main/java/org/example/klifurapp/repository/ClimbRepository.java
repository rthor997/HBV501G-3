package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Climb;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClimbRepository extends JpaRepository<Climb, Long> {
    List<Climb> findByRouteId(Long routeId);

    List<Climb> findByRouteIdAndUserIdIn(
            Long routeId,
            List<Long> userIds
    );
}