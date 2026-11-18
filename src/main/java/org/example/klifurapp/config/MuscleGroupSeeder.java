package org.example.klifurapp.config;

import org.example.klifurapp.entity.MuscleGroup;
import org.example.klifurapp.repository.MuscleGroupRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class MuscleGroupSeeder implements ApplicationRunner {

    public static final List<String> MUSCLE_GROUPS = List.of(
            "Fingers", "Forearms", "Biceps", "Triceps", "Shoulders",
            "Chest", "Back", "Core", "Legs"
    );

    private final MuscleGroupRepository muscleGroupRepository;

    public MuscleGroupSeeder(MuscleGroupRepository muscleGroupRepository) {
        this.muscleGroupRepository = muscleGroupRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : MUSCLE_GROUPS) {
            if (!muscleGroupRepository.existsByNameIgnoreCase(name)) {
                muscleGroupRepository.save(new MuscleGroup(name));
            }
        }
    }
}
