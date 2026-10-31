package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

public interface ClimbService {

    Climb createClimb(CreateClimbRequestDTO dto);
    ClimbResponseDTO getClimb(Long id);
    Page<ClimbSummaryResponseDTO> getClimbingHistory(String userEmail, String grade, LocalDate startDate, LocalDate endDate, Pageable pageable);
    void deleteClimb(Long id, String userEmail);
}