package org.example.klifurapp.Controller;

import jakarta.validation.Valid;
import org.example.klifurapp.DTO.workout.EditWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.WorkoutResponseDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.service.WorkoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteLoggedWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Workout> updateWorkout(
            @PathVariable Long id, 
            @Valid @RequestBody EditWorkoutRequestDTO dto) {
    
        Workout updatedWorkout = workoutService.updateWorkout(id, dto);
        return ResponseEntity.ok(updatedWorkout);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WorkoutResponseDTO>> getWorkoutsByUser(
            @PathVariable Long userId) {

        List<WorkoutResponseDTO> workouts =
                workoutService.getWorkoutsByUserId(userId);

        return ResponseEntity.ok(workouts);
    }
}
