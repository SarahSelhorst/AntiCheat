package org.example.AntiCheat.manager;

import org.example.AntiCheat.checks.Check;
import java.util.ArrayList;
import java.util.List;

public class CheckManager {

    private final List<Check> checks;

    public CheckManager() {
        this.checks = new ArrayList<>();
    }

    public void registerCheck(Check check) {
        checks.add(check);
    }

    public List<Check> getChecks() {
        return checks;
    }

    public List<Check> getEnabledChecks() {
        List<Check> enabled = new ArrayList<>();
        for (Check check : checks) {
            if (check.isEnabled()) {
                enabled.add(check);
            }
        }
        return enabled;
    }
}