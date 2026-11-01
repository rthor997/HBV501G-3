package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Climb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ClimbRepository extends JpaRepository<Climb, Long> {
    Page<Climb> findByUserId(Long userId, Pageable pageable);


    @Query("SELECT c FROM Climb c WHERE c.user.email = :email " +
            "AND (:grade IS NULL OR c.grade = :grade) " +
            "AND (CAST(:startDate AS date) IS NULL OR c.date >= :startDate) " +
            "AND (CAST(:endDate AS date) IS NULL OR c.date <= :endDate)")
    Page<Climb> findFilteredClimbs(
            @Param("email") String email,
            @Param("grade") String grade,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}