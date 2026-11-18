package org.example.klifurapp.DTO.responses;

import java.util.List;

public record ColourGradePresetResponseDTO(
        Long id,
        String name,
        boolean builtIn,
        List<ColourGradeResponseDTO> colourGrades
) {}
