package org.example.klifurapp.repository;

import org.example.klifurapp.entity.ColourGradePreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ColourGradePresetRepository extends JpaRepository<ColourGradePreset, Long> {

    @Query("""
            select p from ColourGradePreset p
            left join p.owner o
            where p.builtIn = true or o.id = :userId
            order by p.builtIn desc, p.id asc
            """)
    List<ColourGradePreset> findVisibleTo(@Param("userId") Long userId);

    boolean existsByBuiltInTrueAndNameIgnoreCase(String name);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}
