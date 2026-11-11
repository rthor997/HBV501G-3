package org.example.klifurapp.service.implementation;

import org.example.klifurapp.DTO.requests.CreateColourGradePresetRequestDTO;
import org.example.klifurapp.DTO.requests.CreateColourGradeRequestDTO;
import org.example.klifurapp.DTO.responses.ColourGradePresetResponseDTO;
import org.example.klifurapp.DTO.responses.ColourGradeResponseDTO;
import org.example.klifurapp.entity.ColourGrade;
import org.example.klifurapp.entity.ColourGradePreset;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.exception.ConflictException;
import org.example.klifurapp.exception.ForbiddenException;
import org.example.klifurapp.exception.ResourceNotFoundException;
import org.example.klifurapp.repository.ColourGradePresetRepository;
import org.example.klifurapp.repository.ColourGradeRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.ColourGradeService;
import org.example.klifurapp.service.GradeValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ColourGradeServiceImplementation implements ColourGradeService {

    private final ColourGradePresetRepository presetRepository;
    private final ColourGradeRepository colourGradeRepository;
    private final UserRepository userRepository;

    public ColourGradeServiceImplementation(
            ColourGradePresetRepository presetRepository,
            ColourGradeRepository colourGradeRepository,
            UserRepository userRepository
    ) {
        this.presetRepository = presetRepository;
        this.colourGradeRepository = colourGradeRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColourGradePresetResponseDTO> getPresets(Long userId) {
        return presetRepository.findVisibleTo(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ColourGradePresetResponseDTO createPreset(
            CreateColourGradePresetRequestDTO dto,
            Long userId
    ) {
        User user = findUser(userId);
        String name = dto.name().trim();

        if (presetRepository.existsByOwnerIdAndNameIgnoreCase(userId, name)) {
            throw new ConflictException("You already have a preset named " + name);
        }

        ColourGradePreset preset = new ColourGradePreset();
        preset.setName(name);
        preset.setBuiltIn(false);
        preset.setOwner(user);

        return toResponse(presetRepository.save(preset));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ColourGradeResponseDTO> getColourGrades(Long presetId, Long userId) {
        ColourGradePreset preset = findVisiblePreset(presetId, userId);

        return colourGradeRepository.findByPresetIdOrderByIdAsc(preset.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ColourGradeResponseDTO createColourGrade(
            Long presetId,
            CreateColourGradeRequestDTO dto,
            Long userId
    ) {
        ColourGradePreset preset = findVisiblePreset(presetId, userId);

        if (preset.isBuiltIn()) {
            throw new ForbiddenException("Built-in presets cannot be modified");
        }

        if (dto.gradingSystem() == GradingSystem.COLOUR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Colour grade ranges must use V_SCALE or FONT"
            );
        }

        String minGrade = dto.minGrade().trim();
        String maxGrade = dto.maxGrade().trim();

        GradeValidator.validate(dto.gradingSystem(), minGrade);
        GradeValidator.validate(dto.gradingSystem(), maxGrade);

        if (GradeValidator.compare(dto.gradingSystem(), minGrade, maxGrade) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum grade cannot be harder than maximum grade"
            );
        }

        String colour = dto.colour().trim();

        if (colourGradeRepository.existsByPresetIdAndColourIgnoreCase(preset.getId(), colour)) {
            throw new ConflictException("This preset already has a grade for colour " + colour);
        }

        ColourGrade colourGrade = new ColourGrade();
        colourGrade.setPreset(preset);
        colourGrade.setColour(colour);
        colourGrade.setGradingSystem(dto.gradingSystem());
        colourGrade.setMinGrade(minGrade);
        colourGrade.setMaxGrade(maxGrade);

        return toResponse(colourGradeRepository.save(colourGrade));
    }

    @Override
    @Transactional
    public ColourGradePresetResponseDTO selectPreset(Long presetId, Long userId) {
        User user = findUser(userId);
        ColourGradePreset preset = findVisiblePreset(presetId, userId);

        user.setSelectedColourPreset(preset);
        userRepository.save(user);

        return toResponse(preset);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ColourGradePresetResponseDTO> getSelectedPreset(Long userId) {
        User user = findUser(userId);

        return Optional.ofNullable(user.getSelectedColourPreset())
                .filter(preset -> preset.isVisibleTo(userId))
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ColourGrade useColourGrade(Long colourGradeId, User user) {
        ColourGrade colourGrade = colourGradeRepository.findById(colourGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Colour grade not found"));

        ColourGradePreset preset = colourGrade.getPreset();

        if (!preset.isVisibleTo(user.getId())) {
            throw new ForbiddenException("You do not have access to this colour grade");
        }

        user.setSelectedColourPreset(preset);
        userRepository.save(user);

        return colourGrade;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ColourGradePreset findVisiblePreset(Long presetId, Long userId) {
        ColourGradePreset preset = presetRepository.findById(presetId)
                .orElseThrow(() -> new ResourceNotFoundException("Preset not found"));

        if (!preset.isVisibleTo(userId)) {
            throw new ForbiddenException("You do not have access to this preset");
        }

        return preset;
    }

    private ColourGradePresetResponseDTO toResponse(ColourGradePreset preset) {
        List<ColourGradeResponseDTO> grades = colourGradeRepository
                .findByPresetIdOrderByIdAsc(preset.getId())
                .stream()
                .map(this::toResponse)
                .toList();

        return new ColourGradePresetResponseDTO(
                preset.getId(),
                preset.getName(),
                preset.isBuiltIn(),
                grades
        );
    }

    private ColourGradeResponseDTO toResponse(ColourGrade colourGrade) {
        return new ColourGradeResponseDTO(
                colourGrade.getId(),
                colourGrade.getPreset().getId(),
                colourGrade.getColour(),
                colourGrade.getGradingSystem(),
                colourGrade.getMinGrade(),
                colourGrade.getMaxGrade()
        );
    }
}
