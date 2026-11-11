package org.example.klifurapp.service;

import org.example.klifurapp.entity.GradingSystem;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class GradeValidator {

    private static final String V_SCALE_PATTERN = "V(0|[1-9][0-9]*)\\+?";
    private static final String FONT_PATTERN = "[3-9][ABC]\\+?";

    private GradeValidator() {
    }

    public static boolean isValid(GradingSystem system, String grade) {
        if (system == null || grade == null) {
            return false;
        }
        return switch (system) {
            case V_SCALE -> grade.matches(V_SCALE_PATTERN);
            case FONT -> grade.matches(FONT_PATTERN);
            case COLOUR -> false;
        };
    }

    public static void validate(GradingSystem system, String grade) {
        if (system == GradingSystem.COLOUR) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Colour grades must be set with a colourGradeId"
            );
        }
        if (!isValid(system, grade)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid difficulty grade for " + system
            );
        }
    }

    public static int compare(GradingSystem system, String first, String second) {
        return Integer.compare(rank(system, first), rank(system, second));
    }

    private static int rank(GradingSystem system, String grade) {
        validate(system, grade);
        boolean plus = grade.endsWith("+");
        String base = plus ? grade.substring(0, grade.length() - 1) : grade;
        int value = switch (system) {
            case V_SCALE -> Integer.parseInt(base.substring(1));
            case FONT -> (base.charAt(0) - '0') * 3 + (base.charAt(1) - 'A');
            case COLOUR -> throw new IllegalStateException();
        };
        return value * 2 + (plus ? 1 : 0);
    }
}
