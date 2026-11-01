package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ClimbService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

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
    public Climb createClimb(CreateClimbRequestDTO dto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found")
                );
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

    public Climb updateClimbAsAdmin(Long id, AdminUpdateClimbDTO dto) {
        Climb climb = climbRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Climb not found"
                        )
                );
        // passa að gradesystem og difficulty grade séu samrænt
        // gera ráð fyrir partial update
        GradingSystem gradingSystem =
                dto.difficultyGrade() != null
                        ? dto.gradingSystem() : climb.getGradingSystem();
        String difficultyGrade =
                dto.gradingSystem() != null
                ? dto.difficultyGrade() : climb.getDifficultyGrade();

        if(dto.gradingSystem() != null || dto.difficultyGrade() != null) {
            validateGrade(gradingSystem, difficultyGrade);
        }

        if(dto.gradingSystem() != null) {
            climb.setGradingSystem(dto.gradingSystem());
        }

        if(dto.difficultyGrade() != null) {
            climb.setDifficultyGrade(dto.difficultyGrade());
        }

        if(dto.climbingStyle() != null) {
            climb.setClimbingStyle(dto.climbingStyle());
        }

        if(dto.date() != null) {
            climb.setDate(dto.date());
        }

        if(dto.notes() != null) {
            climb.setNotes(dto.notes());
        }

        if (dto.wallIncline() != null) {
            climb.setWallIncline(dto.wallIncline());
        }

        return climbRepository.save(climb);
    }


    @Override
    public ClimbResponseDTO getClimb(Long id) {

        Climb climb = climbRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Climb not found"
                        )
                );

        return new ClimbResponseDTO(
                climb.getId(),
                climb.getGradingSystem(),
                climb.getDifficultyGrade(),
                climb.getClimbingStyle(),
                climb.getDate(),
                climb.getNotes(),
                climb.getWallIncline()
        );
    }


    @Override
    public Page<ClimbSummaryResponseDTO> getClimbingHistory(
            Long userId,
            String grade,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        Page<Climb> climbs =
                climbRepository.findFilteredClimbs(
                        userId,
                        grade,
                        startDate,
                        endDate,
                        pageable
                );

        return climbs.map(climb ->
                new ClimbSummaryResponseDTO(
                        climb.getId(),
                        climb.getDifficultyGrade(),
                        climb.getDate()
                )
        );
    }


    @Override
    public void deleteClimb(
            Long id,
            Long userId) {

        Climb climb = climbRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Climb not found"
                        )
                );

        if (!climb.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this climb"
            );
        }

        climbRepository.delete(climb);
    }
}