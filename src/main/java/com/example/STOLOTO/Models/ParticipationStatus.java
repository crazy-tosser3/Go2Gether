package com.example.STOLOTO.Models;

import java.util.EnumSet;
import java.util.Set;

public enum ParticipationStatus {
    PENDING, CONFIRMED, REJECTED, CANCELLED, WAITLIST;

    public static final Set<ParticipationStatus> ACTIVE = EnumSet.of(PENDING, CONFIRMED, WAITLIST);

    public boolean isActive() {
        return ACTIVE.contains(this);
    }
}
