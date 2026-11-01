package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.service.ClimbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/climbs")
public class ClimbController {

    private final ClimbService climbService;

    public ClimbController(ClimbService climbService) {
        this.climbService = climbService;
    }

    @PostMapping
    public ResponseEntity<Climb> createClimb(
            @Valid @RequestBody CreateClimbDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Climb climb = climbService.createClimb(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(climb);
    }

    @PutMapping("/admin/{climbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Climb> updateClimbAsAdmin(
            @PathVariable Long climbId,
            @RequestBody AdminUpdateClimbDTO dto) {
        Climb updatedClimb =
                climbService.updateClimbAsAdmin(climbId, dto);
        return ResponseEntity.ok(updatedClimb);
    }
}