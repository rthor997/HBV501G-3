package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.CreateColourGradePresetRequestDTO;
import org.example.klifurapp.DTO.requests.CreateColourGradeRequestDTO;
import org.example.klifurapp.DTO.requests.SelectColourGradePresetRequestDTO;
import org.example.klifurapp.DTO.responses.ColourGradePresetResponseDTO;
import org.example.klifurapp.DTO.responses.ColourGradeResponseDTO;
import org.example.klifurapp.service.ColourGradeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colour-presets")
public class ColourGradePresetController {

    private final ColourGradeService colourGradeService;

    public ColourGradePresetController(ColourGradeService colourGradeService) {
        this.colourGradeService = colourGradeService;
    }

    @GetMapping
    public ResponseEntity<List<ColourGradePresetResponseDTO>> getPresets(
            Authentication authentication) {
        return ResponseEntity.ok(colourGradeService.getPresets(userId(authentication)));
    }

    @PostMapping
    public ResponseEntity<ColourGradePresetResponseDTO> createPreset(
            @Valid @RequestBody CreateColourGradePresetRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(colourGradeService.createPreset(dto, userId(authentication)));
    }

    @GetMapping("/selected")
    public ResponseEntity<ColourGradePresetResponseDTO> getSelectedPreset(
            Authentication authentication) {
        return colourGradeService.getSelectedPreset(userId(authentication))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/selected")
    public ResponseEntity<ColourGradePresetResponseDTO> selectPreset(
            @Valid @RequestBody SelectColourGradePresetRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(
                colourGradeService.selectPreset(dto.presetId(), userId(authentication)));
    }

    @GetMapping("/{presetId}/colour-grades")
    public ResponseEntity<List<ColourGradeResponseDTO>> getColourGrades(
            @PathVariable Long presetId,
            Authentication authentication) {
        return ResponseEntity.ok(
                colourGradeService.getColourGrades(presetId, userId(authentication)));
    }

    @PostMapping("/{presetId}/colour-grades")
    public ResponseEntity<ColourGradeResponseDTO> createColourGrade(
            @PathVariable Long presetId,
            @Valid @RequestBody CreateColourGradeRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(colourGradeService.createColourGrade(presetId, dto, userId(authentication)));
    }

    private Long userId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
