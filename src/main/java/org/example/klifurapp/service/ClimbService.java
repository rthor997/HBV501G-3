package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.entity.Climb;

public interface ClimbService {

    Climb createClimb(CreateClimbRequestDTO dto);
}