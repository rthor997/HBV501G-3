package org.example.klifurapp.repository;

import org.example.klifurapp.entity.Climb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClimbRepository extends JpaRepository<Climb, Long> {

    Page<Climb> findByUserId(Long userId, Pageable pageable);
}