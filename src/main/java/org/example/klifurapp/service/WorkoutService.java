package org.example.klifurapp.service;

import org.example.klifurapp.DTO.workout.LogWorkoutRequestDTO;
import org.example.klifurapp.DTO.workout.WorkoutResponseDTO;
import org.example.klifurapp.entity.Workout;
import org.example.klifurapp.DTO.workout.EditWorkoutRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkoutService {
    Workout logWorkout(LogWorkoutRequestDTO dto);
    boolean deleteWorkout(Long id);
    Workout updateWorkout(Long id, EditWorkoutRequestDTO dto);
    Page<WorkoutResponseDTO> getWorkoutsByUserId(Long userId, Pageable pageable);
}
