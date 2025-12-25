package org.example.AntiCheat.data;

import org.bukkit.Location;
import java.util.*;

public class PlayerData {
    public Location lastLocation;
    public long lastMoveTime;
    public long lastMoveCheck;
    public long lastTeleportTime;
    public long lastKnockbackTime;
    public long lastBlockBreakTime;

    public double speedViolationLevel;
    public double flyViolationLevel;
    public double cpsViolationLevel;
    public double reachViolationLevel;
    public double fastBreakViolationLevel;

    public int airTicks;
    public int flyBuffer;
    public double cpsBuffer;
    public double reachBuffer;
    public double lastYMotion;

    public ArrayDeque<Double> yMotionHistory;
    public ArrayDeque<Long> clickTimestamps;
    public ArrayDeque<Double> reachHistory;

    public PlayerData() {
        this.lastMoveTime = System.currentTimeMillis();
        this.lastMoveCheck = 0;
        this.lastTeleportTime = 0;
        this.lastKnockbackTime = 0;
        this.lastBlockBreakTime = 0;

        this.speedViolationLevel = 0.0;
        this.flyViolationLevel = 0.0;
        this.cpsViolationLevel = 0.0;
        this.reachViolationLevel = 0.0;
        this.fastBreakViolationLevel = 0.0;

        this.airTicks = 0;
        this.flyBuffer = 0;
        this.cpsBuffer = 0.0;
        this.reachBuffer = 0.0;
        this.lastYMotion = 0.0;

        this.yMotionHistory = new ArrayDeque<>();
        this.clickTimestamps = new ArrayDeque<>();
        this.reachHistory = new ArrayDeque<>();
    }
}