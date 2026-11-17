package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.AdminUpdateClimbDTO;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.DTO.requests.UpdateClimbRequestDTO;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.example.klifurapp.DTO.responses.PageResponseDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.exception.BadRequestException;
import org.example.klifurapp.service.ClimbService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/climbs")
public class ClimbController {

    private static final int MAX_PAGE_SIZE = 50;
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

    @PatchMapping("/{climbId}")
    public ResponseEntity<ClimbResponseDTO> updateClimb(
            @PathVariable Long climbId,
            @Valid @RequestBody UpdateClimbRequestDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        ClimbResponseDTO updatedClimb = climbService.updateClimb(climbId, userId, dto);

        return ResponseEntity.ok(updatedClimb);
    }

    @PutMapping("/admin/{climbId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Climb> updateClimbAsAdmin(
            @PathVariable Long climbId,
            @Valid @RequestBody AdminUpdateClimbDTO dto) {
        Climb updatedClimb =
                climbService.updateClimbAsAdmin(climbId, dto);
        return ResponseEntity.ok(updatedClimb);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDTO<ClimbResponseDTO>> getClimbsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = createPageable(page, size);

        Page<ClimbResponseDTO> result =
                climbService.getClimbsByUserId(userId, pageable);
        return ResponseEntity.ok(PageResponseDTO.from(result));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClimbResponseDTO> getClimb(@PathVariable Long id) {
        return ResponseEntity.ok(climbService.getClimb(id));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ClimbSummaryResponseDTO>> getClimbingHistory(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        Pageable pageable = createPageable(page, size);

        Page<ClimbSummaryResponseDTO> result =
                climbService.getClimbingHistory(
                        userId,
                        grade,
                        startDate,
                        endDate,
                        pageable);
        return ResponseEntity.ok(PageResponseDTO.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClimb(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        climbService.deleteClimb(id, userId);

        return ResponseEntity.noContent().build();
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page,size,Sort.by(
                Sort.Order.desc("date"),Sort.Order.desc("id")));
    }
}