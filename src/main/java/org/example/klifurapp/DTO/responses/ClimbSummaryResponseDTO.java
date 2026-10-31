package org.example.klifurapp.DTO.responses;

import java.time.LocalDate;

public class ClimbSummaryResponseDTO {
    private Long id;
    private String grade;
    private LocalDate date;

    public ClimbSummaryResponseDTO(Long id, String grade, LocalDate date) {
        this.id = id;
        this.grade = grade;
        this.date = date;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}