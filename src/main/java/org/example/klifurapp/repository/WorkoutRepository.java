package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    Page<Workout> findByUserId(Long userId, Pageable pageable);
}