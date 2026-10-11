package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    public ResponseEntity<Workout> logWorkout(@Valid @RequestBody LogWorkoutRequestDTO dto) {
        Workout savedWorkout = workoutService.logWorkout(dto);
        return ResponseEntity.ok(savedWorkout);
    }
}
