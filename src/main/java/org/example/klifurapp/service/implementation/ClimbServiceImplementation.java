package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
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

        Climb climb = new Climb();
        climb.setUser(user);
        climb.setDifficultyGrade(dto.getDifficultyGrade());
        climb.setClimbingStyle(dto.getClimbingStyle());
        climb.setDate(dto.getDate());
        climb.setNotes(dto.getNotes());
        climb.setWallIncline(dto.getWallIncline());

        return climbRepository.save(climb);
    }
}