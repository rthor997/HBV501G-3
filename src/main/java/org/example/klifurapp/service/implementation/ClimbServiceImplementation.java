package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.requests.UpdateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.example.klifurapp.entity.*;
import org.example.klifurapp.exception.BadRequestException;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.RouteRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ClimbService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ClimbServiceImplementation implements ClimbService {

    private final ClimbRepository climbRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;

    public ClimbServiceImplementation(
            ClimbRepository climbRepository,
            UserRepository userRepository,
            RouteRepository routeRepository) {
        this.climbRepository = climbRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public Climb createClimb(CreateClimbRequestDTO dto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
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

        if (dto.getRouteId() != null) {
            Route route = routeRepository
                    .findByIdAndActiveTrue(dto.getRouteId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Route not found")
                    );
            climb.setRoute(route);
        }
        return climbRepository.save(climb);
    }

    private void validateGrade(GradingSystem system, String grade) {
        boolean valid = switch (system) {
            case V_SCALE -> grade.matches("V(0|[1-9][0-9]*)\\+?");
            case FONT -> grade.matches("[3-9][ABC]\\+?");
        };

        if (!valid) {
            throw new BadRequestException("Invalid difficulty grade for " + system);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClimbResponseDTO> getClimbsByUserId(
            Long userId, Pageable pageable) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
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

    @Override
    public ClimbResponseDTO updateClimb(
            Long climbId,
            Long userId,
            UpdateClimbRequestDTO dto) {
        Climb climb = climbRepository.findById(climbId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Climb not found"));

        if (!climb.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this climb");
        }

        applyClimbUpdates(
                climb,
                dto.gradingSystem(),
                dto.difficultyGrade(),
                dto.climbingStyle(),
                dto.date(),
                dto.notes(),
                dto.wallIncline()
        );

        Climb savedClimb = climbRepository.save(climb);

        return new ClimbResponseDTO(
                savedClimb.getId(),
                savedClimb.getGradingSystem(),
                savedClimb.getDifficultyGrade(),
                savedClimb.getClimbingStyle(),
                savedClimb.getDate(),
                savedClimb.getNotes(),
                savedClimb.getWallIncline()
        );
    }

    @Override
    public Climb updateClimbAsAdmin(Long id, AdminUpdateClimbDTO dto) {
        Climb climb = climbRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Climb not found")
                );
        applyClimbUpdates(
                climb,
                dto.gradingSystem(),
                dto.difficultyGrade(),
                dto.climbingStyle(),
                dto.date(),
                dto.notes(),
                dto.wallIncline()
        );
        return climbRepository.save(climb);
    }


    @Override
    public ClimbResponseDTO getClimb(Long id) {

        Climb climb = climbRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Climb not found")
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
                        new ResourceNotFoundException("Climb not found")
                );

        if (!climb.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not own this climb");
        }
        climbRepository.delete(climb);
    }


    private void applyClimbUpdates(
            Climb climb,
            GradingSystem gradingSystem,
            String difficultyGrade,
            ClimbingStyle climbingStyle,
            LocalDate date,
            String notes,
            WallIncline wallIncline) {
        GradingSystem resultingSystem =
                gradingSystem != null
                        ? gradingSystem
                        : climb.getGradingSystem();

        String resultingGrade =
                difficultyGrade != null
                        ? difficultyGrade
                        : climb.getDifficultyGrade();

        if (gradingSystem != null || difficultyGrade != null) {
            validateGrade(resultingSystem, resultingGrade);
        }

        if (gradingSystem != null) {
            climb.setGradingSystem(gradingSystem);
        }

        if (difficultyGrade != null) {
            climb.setDifficultyGrade(difficultyGrade);
        }

        if (climbingStyle != null) {
            climb.setClimbingStyle(climbingStyle);
        }

        if (date != null) {
            climb.setDate(date);
        }

        if (notes != null) {
            climb.setNotes(notes);
        }

        if (wallIncline != null) {
            climb.setWallIncline(wallIncline);
        }
    }
}