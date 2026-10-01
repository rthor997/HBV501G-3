package org.example.klifurapp.service;

import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.entity.Workout;

public interface WorkoutService {
    Workout logWorkout(LogWorkoutRequestDTO dto);
}
