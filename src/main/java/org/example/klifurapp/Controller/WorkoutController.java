package org.example.klifurapp.Controller;

import jakarta.validation.Valid;

import org.example.klifurapp.DTO.workout.EditWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    public ResponseEntity<Workout> logWorkout(@Valid @RequestBody LogWorkoutRequestDTO dto, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Workout savedWorkout = workoutService.logWorkout(dto, userId);
        return ResponseEntity.ok(savedWorkout);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteLoggedWorkout(@PathVariable Long id, Authentication authentication) {
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
}
