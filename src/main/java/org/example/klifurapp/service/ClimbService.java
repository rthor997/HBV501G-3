package org.example.klifurapp.service;

import org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;

public interface ClimbService {

    Climb createClimb(CreateClimbDTO dto, Long userId);
    Climb updateClimbAsAdmin(Long id, AdminUpdateClimbDTO dto);
}