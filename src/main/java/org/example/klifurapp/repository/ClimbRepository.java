
package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Climb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

import java.util.List;

public interface ClimbRepository extends JpaRepository<Climb, Long> {

    Page<Climb> findByUserId(
            Long userId,
            Pageable pageable
    );

    @Query("""
        SELECT c
        FROM Climb c
        WHERE c.user.id = :userId
        AND (:grade IS NULL OR c.difficultyGrade = :grade)
        AND (:startDate IS NULL OR c.date >= :startDate)
        AND (:endDate IS NULL OR c.date <= :endDate)
        """)
    Page<Climb> findFilteredClimbs(
            @Param("userId") Long userId,
            @Param("grade") String grade,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
    // View Route Details
    List<Climb> findByRouteId(Long routeId);

    List<Climb> findByRouteIdAndUserIdIn(
            Long routeId,
            List<Long> userIds
    );

}
