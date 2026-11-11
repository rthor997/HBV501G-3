package org.example.klifurapp.config;

import org.example.klifurapp.entity.ColourGrade;
import org.example.klifurapp.entity.ColourGradePreset;
import org.example.klifurapp.entity.GradingSystem;
import org.example.klifurapp.repository.ColourGradePresetRepository;
import org.example.klifurapp.repository.ColourGradeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ColourGradePresetSeeder implements ApplicationRunner {

    public static final String KLIFURHUSID = "Klifurhúsið";

    private static final List<String[]> KLIFURHUSID_GRADES = List.of(
            new String[]{"White", "V0", "V0"},
            new String[]{"Green", "V0", "V1"},
            new String[]{"Blue", "V1", "V2"},
            new String[]{"Yellow", "V2", "V3"},
            new String[]{"Orange", "V3", "V4"},
            new String[]{"Red", "V4", "V5"},
            new String[]{"Black", "V6", "V7"},
            new String[]{"Silver", "V8", "V9+"}
    );

    private final ColourGradePresetRepository presetRepository;
    private final ColourGradeRepository colourGradeRepository;

    public ColourGradePresetSeeder(
            ColourGradePresetRepository presetRepository,
            ColourGradeRepository colourGradeRepository
    ) {
        this.presetRepository = presetRepository;
        this.colourGradeRepository = colourGradeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (presetRepository.existsByBuiltInTrueAndNameIgnoreCase(KLIFURHUSID)) {
            return;
        }

        ColourGradePreset preset = new ColourGradePreset();
        preset.setName(KLIFURHUSID);
        preset.setBuiltIn(true);
        presetRepository.save(preset);

        for (String[] grade : KLIFURHUSID_GRADES) {
            ColourGrade colourGrade = new ColourGrade();
            colourGrade.setPreset(preset);
            colourGrade.setColour(grade[0]);
            colourGrade.setGradingSystem(GradingSystem.V_SCALE);
            colourGrade.setMinGrade(grade[1]);
            colourGrade.setMaxGrade(grade[2]);
            colourGradeRepository.save(colourGrade);
        }
    }
}
