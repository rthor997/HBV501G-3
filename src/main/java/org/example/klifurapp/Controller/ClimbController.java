package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.climb.ClimbResponseDTO;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.service.ClimbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @ResponseStatus(HttpStatus.CREATED)
    public Climb createClimb(@Valid @RequestBody CreateClimbDTO dto) {
        return climbService.createClimb(dto);
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