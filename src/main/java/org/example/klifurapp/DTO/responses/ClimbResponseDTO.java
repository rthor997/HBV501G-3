package org.example.klifurapp.DTO.responses;

import java.time.LocalDate;

public class ClimbResponseDTO {
    private Long id;
    private String username;
    private String grade;
    private LocalDate date;
    private String notes;

    public ClimbResponseDTO(Long id, String username, String grade, LocalDate date, String notes) {
        this.id = id;
        this.username = username;
        this.grade = grade;
        this.date = date;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}