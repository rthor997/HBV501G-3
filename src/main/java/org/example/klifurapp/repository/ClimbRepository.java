
package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Climb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClimbRepository extends JpaRepository<Climb, Long> {

    // View Route Details
    List<Climb> findByRouteId(Long routeId);

    List<Climb> findByRouteIdAndUserIdIn(
            Long routeId,
            List<Long> userIds
    );

    // View Personal Climbing History
    Page<Climb> findByUserId(Long userId, Pageable pageable);
}
