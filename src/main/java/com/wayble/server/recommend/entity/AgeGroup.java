package com.wayble.server.recommend.entity;

import java.time.LocalDate;
import java.time.Period;

public enum AgeGroup {
    TEENAGERS,
    TWENTIES,
    THIRTIES,
    FORTIES,
    FIFTIES,
    SIXTIES,
    SEVENTIES,
    EIGHTIES,
    OTHERS;

    public static AgeGroup fromBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return OTHERS;
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (age >= 10 && age < 20) {
            return TEENAGERS;
        } else if (age < 30) {
            return TWENTIES;
        } else if (age < 40) {
            return THIRTIES;
        } else if (age < 50) {
            return FORTIES;
        } else if (age < 60) {
            return FIFTIES;
        } else if (age < 70) {
            return SIXTIES;
        } else if (age < 80) {
            return SEVENTIES;
        } else if (age < 90) {
            return EIGHTIES;
        } else {
            return OTHERS;
        }
    }
}
