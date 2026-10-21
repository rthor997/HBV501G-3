package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ClimbService;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

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
            case FONT -> grade.matches("(3|4|5|6|7|8|9)[ABCabc]?\\+?");
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid grade for grading system: " + system
            );
        }
    }
}