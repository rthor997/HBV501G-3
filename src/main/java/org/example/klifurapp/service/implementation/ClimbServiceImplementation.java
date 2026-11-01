package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.climb.ClimbResponseDTO;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ClimbService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    public Climb createClimb(CreateClimbDTO dto) {

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        validateGrade(dto.getGradingSystem(), dto.getDifficultyGrade());
        Climb climb = new Climb();
        climb.setUser(user);
        climb.setGradingSystem(dto.getGradingSystem());
        climb.setDifficultyGrade(dto.getDifficultyGrade());
        climb.setClimbingStyle(dto.getClimbingStyle());
        climb.setDate(dto.getDate());
        climb.setNotes(dto.getNotes());
        climb.setWallIncline(dto.getWallIncline());

        return climbRepository.save(climb);
    }

    private void validateGrade(GradingSystem system, String grade) {
        boolean valid = switch (system) {
            case V_SCALE -> grade.matches("V(0|[1-9][0-9]*)\\+?");
            case FONT -> grade.matches("[3-9][ABC]\\+?");
        };

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid difficulty grade for " + system
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClimbResponseDTO> getClimbsByUserId(
            Long userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return climbRepository.findByUserId(userId, pageable)
                .map(climb -> new ClimbResponseDTO(
                        climb.getId(),
                        climb.getGradingSystem(),
                        climb.getDifficultyGrade(),
                        climb.getClimbingStyle(),
                        climb.getDate(),
                        climb.getNotes(),
                        climb.getWallIncline()
                ));
    }
}