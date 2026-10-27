package org.example.klifurapp.service;

import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.WorkoutResponseDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.DTO.workout.EditWorkoutRequestDTO;

import java.util.List;

public interface WorkoutService {
    Workout logWorkout(LogWorkoutRequestDTO dto);
    boolean deleteWorkout(Long id);
    Workout updateWorkout(Long id, EditWorkoutRequestDTO dto);
    List<WorkoutResponseDTO> getWorkoutsByUserId(Long userId);
}
