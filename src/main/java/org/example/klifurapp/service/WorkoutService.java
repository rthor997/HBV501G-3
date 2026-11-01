package org.example.klifurapp.service;

import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.DTO.workout.EditWorkoutRequestDTO;

public interface WorkoutService {
    Workout logWorkout(LogWorkoutRequestDTO dto, Long userId);
    boolean deleteWorkout(Long id, Long userId);
    Workout updateWorkout(Long id, EditWorkoutRequestDTO dto, Long userId);
}
