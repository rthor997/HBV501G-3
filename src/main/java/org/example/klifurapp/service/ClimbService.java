package org.example.klifurapp.service;

import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;

public interface ClimbService {

    Climb createClimb(CreateClimbDTO dto);
}