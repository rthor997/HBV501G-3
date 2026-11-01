package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.requests.ClimbResponseDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.service.ClimbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/climbs")
public class ClimbController {

    private final ClimbService climbService;

    public ClimbController(ClimbService climbService) {
        this.climbService = climbService;
    }

    @PostMapping
    public ResponseEntity<Climb> createClimb(
            @Valid @RequestBody CreateClimbRequestDTO dto,
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ClimbResponseDTO>> getClimbsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("date"),
                        Sort.Order.desc("id")
                )
        );

        Page<ClimbResponseDTO> climbs =
                climbService.getClimbsByUserId(userId, pageable);

        return ResponseEntity.ok(climbs);
    }
}