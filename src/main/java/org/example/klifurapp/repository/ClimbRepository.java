package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Climb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClimbRepository extends JpaRepository<Climb, Long> {
}