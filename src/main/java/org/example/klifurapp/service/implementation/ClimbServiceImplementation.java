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
import org.example.klifurapp.strategy.SortingStrategy;
import org.example.klifurapp.service.ColourGradeService;
import org.example.klifurapp.service.GradeValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClimbServiceImplementation implements ClimbService {

    private final ClimbRepository climbRepository;
    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final Map<String, SortingStrategy> sortingStrategies;
    private final ColourGradeService colourGradeService;

    public ClimbServiceImplementation(
            ClimbRepository climbRepository,
            UserRepository userRepository,
            RouteRepository routeRepository,
            ColourGradeService colourGradeService,
            List<SortingStrategy> strategies) {
        this.climbRepository = climbRepository;
        this.userRepository = userRepository;
        this.routeRepository = routeRepository;
        this.colourGradeService = colourGradeService;
        this.sortingStrategies = strategies.stream()
                .collect(Collectors.toMap(
                        SortingStrategy::getName,
                        Function.identity()
                ));
    }

    @Override
    @Transactional
    public Climb createClimb(CreateClimbRequestDTO dto, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found")
                );
        Climb climb = new Climb();
        climb.setUser(user);
        climb.setGradingSystem(dto.getGradingSystem());

        if (dto.getGradingSystem() == GradingSystem.COLOUR) {
            if (dto.getColourGradeId() == null) {
                throw new BadRequestException("colourGradeId is required when gradingSystem is COLOUR");
            }
            ColourGrade colourGrade = colourGradeService.useColourGrade(dto.getColourGradeId(), user);
            climb.setColourGrade(colourGrade);
            climb.setDifficultyGrade(colourGrade.getColour());
        } else {
            if (dto.getColourGradeId() != null) {
                throw new BadRequestException("colourGradeId can only be used when gradingSystem is COLOUR");
            }
            validateGrade(dto.getGradingSystem(), dto.getDifficultyGrade());
            climb.setDifficultyGrade(dto.getDifficultyGrade());
        }

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
        GradeValidator.validate(system, grade);
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
    @Transactional
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
                dto.colourGradeId(),
                true,
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
    @Transactional
    public Climb updateClimbAsAdmin(Long id, AdminUpdateClimbDTO dto) {
        Climb climb = climbRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Climb not found")
                );
        applyClimbUpdates(
                climb,
                dto.gradingSystem(),
                dto.difficultyGrade(),
                dto.colourGradeId(),
                false,
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
            String sortBy,
            Pageable pageable) {

        // Select the sorting strategy, returns 400 if invalid
        SortingStrategy strategy = getSortingStrategy(sortBy);

        // Apply the selected strategy to the existing pagination
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                strategy.getSort()
        );

        Page<Climb> climbs =
                climbRepository.findFilteredClimbs(
                        userId,
                        grade,
                        startDate,
                        endDate,
                        sortedPageable
                );

        return climbs.map(climb ->
                new ClimbSummaryResponseDTO(
                        climb.getId(),
                        climb.getDifficultyGrade(),
                        climb.getDate()
                )
        );
    }


    private SortingStrategy getSortingStrategy(String sortBy) {

        SortingStrategy strategy = sortingStrategies.get(sortBy);

        if (strategy == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sorting option: " + sortBy
            );
        }

        return strategy;
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
            Long colourGradeId,
            boolean rememberPreset,
            ClimbingStyle climbingStyle,
            LocalDate date,
            String notes,
            WallIncline wallIncline) {
        GradingSystem resultingSystem =
                gradingSystem != null
                        ? gradingSystem
                        : climb.getGradingSystem();

        if (resultingSystem == GradingSystem.COLOUR) {
            applyColourGrade(climb, difficultyGrade, colourGradeId, rememberPreset);
        } else {
            applyScaleGrade(climb, resultingSystem, gradingSystem, difficultyGrade, colourGradeId);
        }

        climb.setGradingSystem(resultingSystem);

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

    private void applyColourGrade(
            Climb climb,
            String difficultyGrade,
            Long colourGradeId,
            boolean rememberPreset) {
        if (difficultyGrade != null) {
            throw new BadRequestException("Use colourGradeId instead of difficultyGrade when gradingSystem is COLOUR");
        }

        if (colourGradeId != null) {
            ColourGrade colourGrade = rememberPreset
                    ? colourGradeService.useColourGrade(colourGradeId, climb.getUser())
                    : colourGradeService.getUsableColourGrade(colourGradeId, climb.getUser());
            climb.setColourGrade(colourGrade);
            climb.setDifficultyGrade(colourGrade.getColour());
        } else if (climb.getColourGrade() == null) {
            throw new BadRequestException("colourGradeId is required when gradingSystem is COLOUR");
        }
    }

    private void applyScaleGrade(
            Climb climb,
            GradingSystem resultingSystem,
            GradingSystem gradingSystem,
            String difficultyGrade,
            Long colourGradeId) {
        if (colourGradeId != null) {
            throw new BadRequestException("colourGradeId can only be used when gradingSystem is COLOUR");
        }

        if (climb.getGradingSystem() == GradingSystem.COLOUR && difficultyGrade == null) {
            throw new BadRequestException("difficultyGrade is required when changing from COLOUR to " + resultingSystem);
        }

        if (gradingSystem != null || difficultyGrade != null) {
            String resultingGrade = difficultyGrade != null
                    ? difficultyGrade
                    : climb.getDifficultyGrade();
            validateGrade(resultingSystem, resultingGrade);
        }

        climb.setColourGrade(null);

        if (difficultyGrade != null) {
            climb.setDifficultyGrade(difficultyGrade);
        }
    }
}
