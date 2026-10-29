package org.example.klifurapp.service;

import org.example.klifurapp.DTO.requests.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.requests.EditWorkoutRequestDTO;
import org.example.klifurapp.entity.Workout;

public interface WorkoutService {
    Workout logWorkout(String userEmail, LogWorkoutRequestDTO dto);

    boolean deleteWorkout(Long id);
    Workout updateWorkout(Long id, EditWorkoutRequestDTO dto);
}