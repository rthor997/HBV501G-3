package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.CreateColourGradePresetRequestDTO;
import org.example.klifurapp.DTO.requests.CreateColourGradeRequestDTO;
import org.example.klifurapp.DTO.responses.ColourGradePresetResponseDTO;
import org.example.klifurapp.DTO.responses.ColourGradeResponseDTO;
import org.example.klifurapp.entity.ColourGrade;
import org.example.klifurapp.entity.User;

import java.util.List;
import java.util.Optional;

public interface ColourGradeService {

    List<ColourGradePresetResponseDTO> getPresets(Long userId);

    ColourGradePresetResponseDTO createPreset(CreateColourGradePresetRequestDTO dto, Long userId);

    List<ColourGradeResponseDTO> getColourGrades(Long presetId, Long userId);

    ColourGradeResponseDTO createColourGrade(Long presetId, CreateColourGradeRequestDTO dto, Long userId);

    ColourGradePresetResponseDTO selectPreset(Long presetId, Long userId);

    Optional<ColourGradePresetResponseDTO> getSelectedPreset(Long userId);

    ColourGrade useColourGrade(Long colourGradeId, User user);
}
