package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.ClimbResponseDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.example.klifurapp.entity.Climb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ClimbService {

    Page<ClimbResponseDTO> getClimbsByUserId(Long userId, Pageable pageable);
    Climb createClimb(CreateClimbRequestDTO dto, Long userId);
    Climb updateClimbAsAdmin(Long id, org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO dto);
    ClimbResponseDTO getClimb(Long id);
    Page<ClimbSummaryResponseDTO> getClimbingHistory(String userEmail, String grade, LocalDate startDate, LocalDate endDate, Pageable pageable);
    void deleteClimb(Long id, String userEmail);

}