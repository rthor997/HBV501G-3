package org.example.klifurapp.service;

import org.example.klifurapp.entity.GradingSystem;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

class GradeValidatorTest {

    @Test
    void acceptsValidVScaleGrades() {
        assertTrue(GradeValidator.isValid(GradingSystem.V_SCALE, "V0"));
        assertTrue(GradeValidator.isValid(GradingSystem.V_SCALE, "V10"));
        assertTrue(GradeValidator.isValid(GradingSystem.V_SCALE, "V4+"));
    }

    @Test
    void acceptsValidFontGrades() {
        assertTrue(GradeValidator.isValid(GradingSystem.FONT, "6A"));
        assertTrue(GradeValidator.isValid(GradingSystem.FONT, "7C+"));
    }

    @Test
    void rejectsGradesFromWrongSystem() {
        assertFalse(GradeValidator.isValid(GradingSystem.V_SCALE, "6A"));
        assertFalse(GradeValidator.isValid(GradingSystem.FONT, "V3"));
        assertFalse(GradeValidator.isValid(GradingSystem.V_SCALE, "V01"));
        assertFalse(GradeValidator.isValid(GradingSystem.FONT, null));
    }

    @Test
    void validateThrowsBadRequestForInvalidGrade() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> GradeValidator.validate(GradingSystem.FONT, "6D"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void comparesVScaleGrades() {
        assertTrue(GradeValidator.compare(GradingSystem.V_SCALE, "V2", "V10") < 0);
        assertTrue(GradeValidator.compare(GradingSystem.V_SCALE, "V3+", "V3") > 0);
        assertEquals(0, GradeValidator.compare(GradingSystem.V_SCALE, "V5", "V5"));
    }

    @Test
    void comparesFontGrades() {
        assertTrue(GradeValidator.compare(GradingSystem.FONT, "6C+", "7A") < 0);
        assertTrue(GradeValidator.compare(GradingSystem.FONT, "6B", "6A+") > 0);
        assertTrue(GradeValidator.compare(GradingSystem.FONT, "5C", "6A") < 0);
    }
}
