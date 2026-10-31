package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ClimbService;
import org.springframework.stereotype.Service;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import org.example.klifurapp.entity.Climb;

@Service
public class ClimbServiceImplementation implements ClimbService {

    private final ClimbRepository climbRepository;
    private final UserRepository userRepository;

    public ClimbServiceImplementation(
            ClimbRepository climbRepository,
            UserRepository userRepository) {
        this.climbRepository = climbRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Climb createClimb(CreateClimbRequestDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Climb climb = new Climb();
        climb.setUser(user);
        climb.setDifficultyGrade(dto.getDifficultyGrade());
        climb.setClimbingStyle(dto.getClimbingStyle());
        climb.setDate(dto.getDate());
        climb.setNotes(dto.getNotes());
        climb.setWallIncline(dto.getWallIncline());

        return climbRepository.save(climb);
    }

    @Override
    public ClimbResponseDTO getClimb(Long id) {
        Climb climb = climbRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Climb not found"));

        return new ClimbResponseDTO(
                climb.getId(),
                climb.getUser().getUsername(),
                climb.getDifficultyGrade().name(),
                climb.getDate(),
                climb.getNotes()
        );
    }

    @Override
    public Page<ClimbSummaryResponseDTO> getClimbingHistory(String userEmail, String grade, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Climb> climbs = climbRepository.findFilteredClimbs(userEmail, grade, startDate, endDate, pageable);

        return climbs.map(climb -> new ClimbSummaryResponseDTO(
                climb.getId(),
                climb.getDifficultyGrade().name(),
                climb.getDate()
        ));
    }

    @Override
    public void deleteClimb(Long id, String userEmail) {
        Climb climb = climbRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Climb not found"));

        if (!climb.getUser().getEmail().equals(userEmail)) {
            throw new SecurityException("Unauthorized access");
        }

        climbRepository.delete(climb);
    }
}