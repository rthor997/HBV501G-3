package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.ClimbResponseDTO;
import org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.entity.Climb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClimbService {

    Page<ClimbResponseDTO> getClimbsByUserId(Long userId, Pageable pageable);
    Climb createClimb(CreateClimbRequestDTO dto, Long userId);
    Climb updateClimbAsAdmin(Long id, AdminUpdateClimbDTO dto);
}
