package org.example.klifurapp.repository;

import org.example.klifurapp.entity.ColourGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColourGradeRepository extends JpaRepository<ColourGrade, Long> {
    List<ColourGrade> findByPresetIdOrderByIdAsc(Long presetId);

    boolean existsByPresetIdAndColourIgnoreCase(Long presetId, String colour);
}
