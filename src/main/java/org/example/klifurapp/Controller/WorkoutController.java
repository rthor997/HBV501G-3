package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.requests.EditWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.WorkoutResponseDTO;
import org.example.klifurapp.DTO.responses.PageResponseDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.exception.BadRequestException;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private static final int MAX_PAGE_SIZE = 50;
    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    public ResponseEntity<Workout> logWorkout(
            @Valid @RequestBody LogWorkoutRequestDTO dto,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Workout savedWorkout = workoutService.logWorkout(dto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedWorkout);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoggedWorkout(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        workoutService.deleteWorkout(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(
            @PathVariable Long id, 
            @Valid @RequestBody EditWorkoutRequestDTO dto,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        Workout updatedWorkout = workoutService.updateWorkout(id, dto, userId);
        return ResponseEntity.ok(updatedWorkout);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageResponseDTO<WorkoutResponseDTO>> getWorkoutsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = createPageable(page, size);

        Page<WorkoutResponseDTO> result =
                workoutService.getWorkoutsByUserId(userId, pageable);

        return ResponseEntity.ok(PageResponseDTO.from(result));
    }

    private Pageable createPageable(int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page must be zero or greater");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page,size,Sort.by(Sort.Order.desc("id")));
    }
}
