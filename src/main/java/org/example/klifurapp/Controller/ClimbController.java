package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.climb.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.service.ClimbService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

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


    @GetMapping("/{id}")
    public ResponseEntity<ClimbResponseDTO> getClimb(@PathVariable Long id) {
        return ResponseEntity.ok(climbService.getClimb(id));
    }

    @GetMapping
    public ResponseEntity<Page<ClimbSummaryResponseDTO>> getClimbingHistory(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @PageableDefault(
                    size = 20,
                    sort = "date",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return ResponseEntity.ok(
                climbService.getClimbingHistory(
                        userId,
                        grade,
                        startDate,
                        endDate,
                        pageable
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClimb(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        climbService.deleteClimb(id, userId);

        return ResponseEntity.noContent().build();
    }
}