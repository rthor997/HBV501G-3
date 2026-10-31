package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.CreateClimbRequestDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.service.ClimbService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.example.klifurapp.DTO.responses.ClimbResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.example.klifurapp.DTO.responses.ClimbSummaryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import java.security.Principal;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.security.Principal;

@RestController
@RequestMapping("/api/climbs")
public class ClimbController {

    private final ClimbService climbService;

    public ClimbController(ClimbService climbService) {
        this.climbService = climbService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Climb createClimb(@Valid @RequestBody CreateClimbRequestDTO dto) {
        return climbService.createClimb(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClimbResponseDTO> getClimb(@PathVariable Long id) {
        return ResponseEntity.ok(climbService.getClimb(id));
    }

    @GetMapping
    public ResponseEntity<Page<ClimbSummaryResponseDTO>> getClimbingHistory(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
            Principal principal) {
        return ResponseEntity.ok(climbService.getClimbingHistory(principal.getName(), grade, startDate, endDate, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClimb(@PathVariable Long id, Principal principal) {
        climbService.deleteClimb(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}