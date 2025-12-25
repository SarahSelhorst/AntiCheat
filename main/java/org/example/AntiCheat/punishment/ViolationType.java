package org.example.AntiCheat.punishment;

public enum ViolationType {
    SPEED("Speed"),
    FLY("Fly"),
    CPS("AutoClick"),
    REACH("Reach"),
    FASTBREAK("FastBreak");

    private final String displayName;

    ViolationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}