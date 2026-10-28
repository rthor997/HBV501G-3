package org.example.klifurapp.service;

import org.example.klifurapp.DTO.climb.ClimbResponseDTO;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;

import java.util.List;

public interface ClimbService {

    Climb createClimb(CreateClimbDTO dto);

    List<ClimbResponseDTO> getClimbsByUserId(Long userId);
}