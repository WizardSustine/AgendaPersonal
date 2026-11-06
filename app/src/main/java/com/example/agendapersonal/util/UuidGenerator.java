package com.example.agendapersonal.util;

import java.util.UUID;

public class UuidGenerator {
    private UuidGenerator() {
        // Utility class: do not instantiate.
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        try {
            UUID.fromString(value.trim());
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
