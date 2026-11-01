package org.example.klifurapp.Controller;

import jakarta.validation.Valid;

import org.example.klifurapp.DTO.workout.EditWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.WorkoutResponseDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

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
    public ResponseEntity<Page<WorkoutResponseDTO>> getWorkoutsByUser(
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
                Sort.by(Sort.Direction.DESC, "id")
        );

        Page<WorkoutResponseDTO> workouts =
                workoutService.getWorkoutsByUserId(userId, pageable);

        return ResponseEntity.ok(workouts);
    }
}
