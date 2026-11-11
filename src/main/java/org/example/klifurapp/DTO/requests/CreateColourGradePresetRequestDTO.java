package org.example.klifurapp.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateColourGradePresetRequestDTO(
        @NotBlank @Size(max = 100) String name
) {}
