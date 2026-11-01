package org.example.klifurapp.service;

import org.example.klifurapp.DTO.climb.ClimbResponseDTO;
import org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClimbService {

    Climb createClimb(CreateClimbDTO dto);

    Page<ClimbResponseDTO> getClimbsByUserId(Long userId, Pageable pageable);
    Climb createClimb(CreateClimbDTO dto, Long userId);
    Climb updateClimbAsAdmin(Long id, AdminUpdateClimbDTO dto);
}