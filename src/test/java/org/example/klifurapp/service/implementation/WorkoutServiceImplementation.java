@Override
public Workout updateWorkout(Long id, EditWorkoutRequestDTO dto) {
    Workout workout = workoutRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Æfing fannst ekki"));

    workout.setExerciseName(dto.getExerciseName());
    workout.setSets(dto.getSets());
    workout.setRepetitions(dto.getRepetitions());
    workout.setWeight(dto.getWeight());
    workout.setDurationMinutes(dto.getDurationMinutes());

    return workoutRepository.save(workout);
}
