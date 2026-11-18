package org.example.klifurapp.repository;

import org.example.klifurapp.entity.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MuscleGroupRepository extends JpaRepository<MuscleGroup, Long> {
    List<MuscleGroup> findAllByOrderByIdAsc();

    boolean existsByNameIgnoreCase(String name);
}
