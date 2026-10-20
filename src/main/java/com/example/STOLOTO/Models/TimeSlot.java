package com.example.STOLOTO.Models;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;

public enum TimeSlot {
    MORNING, DAY, EVENING, WEEKEND;

    public boolean matches(ZonedDateTime t) {
        int hour = t.getHour();
        return switch (this) {
            case MORNING -> hour < 11;
            case DAY -> hour >= 11 && hour < 17;
            case EVENING -> hour >= 17;
            case WEEKEND -> t.getDayOfWeek() == DayOfWeek.SATURDAY || t.getDayOfWeek() == DayOfWeek.SUNDAY;
        };
    }
}
