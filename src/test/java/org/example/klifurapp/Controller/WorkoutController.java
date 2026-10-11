@PutMapping("/{id}")
public ResponseEntity<Workout> updateWorkout(
        @PathVariable Long id, 
        @Valid @RequestBody EditWorkoutRequestDTO dto) {
    
    Workout updatedWorkout = workoutService.updateWorkout(id, dto);
    return ResponseEntity.ok(updatedWorkout);
}
