package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.climb.ClimbResponseDTO;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.service.ClimbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/climbs")
public class ClimbController {

    private final ClimbService climbService;

    public ClimbController(ClimbService climbService) {
        this.climbService = climbService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Climb createClimb(@Valid @RequestBody CreateClimbDTO dto) {
        return climbService.createClimb(dto);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ClimbResponseDTO>> getClimbsByUser(
            @PathVariable Long userId) {

        List<ClimbResponseDTO> climbs =
                climbService.getClimbsByUserId(userId);

        return ResponseEntity.ok(climbs);
    }
}