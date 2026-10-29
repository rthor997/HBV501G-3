package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.climb.CreateClimbDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.service.ClimbService;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public Climb createClimb(@Valid @RequestBody CreateClimbDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return climbService.createClimb(dto, userId);
    }
}