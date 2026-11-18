package org.example.klifurapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "colour_grades",
        uniqueConstraints = @UniqueConstraint(columnNames = {"preset_id", "colour"})
)
public class ColourGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "preset_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ColourGradePreset preset;

    @Column(nullable = false)
    private String colour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradingSystem gradingSystem;

    @Column(nullable = false)
    private String minGrade;

    @Column(nullable = false)
    private String maxGrade;

    public ColourGrade() {
    }

    public Long getId() {
        return id;
    }

    public ColourGradePreset getPreset() {
        return preset;
    }

    public void setPreset(ColourGradePreset preset) {
        this.preset = preset;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public GradingSystem getGradingSystem() {
        return gradingSystem;
    }

    public void setGradingSystem(GradingSystem gradingSystem) {
        this.gradingSystem = gradingSystem;
    }

    public String getMinGrade() {
        return minGrade;
    }

    public void setMinGrade(String minGrade) {
        this.minGrade = minGrade;
    }

    public String getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(String maxGrade) {
        this.maxGrade = maxGrade;
    }
}
