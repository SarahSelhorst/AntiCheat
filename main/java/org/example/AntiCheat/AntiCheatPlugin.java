package org.example.AntiCheat;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.example.AntiCheat.checks.*;
import org.example.AntiCheat.command.*;
import org.example.AntiCheat.data.PlayerData;
import org.example.AntiCheat.manager.CheckManager;
import org.example.AntiCheat.manager.PlayerDataManager;
import org.example.AntiCheat.punishment.PunishmentManager;

import java.util.LinkedList;
import java.util.UUID;

public class AntiCheatPlugin extends JavaPlugin implements Listener {

    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private PunishmentManager punishmentManager;

    private SpeedCheck speedCheck;
    private FlyCheck flyCheck;
    private CPSCheck cpsCheck;
    private ReachCheck reachCheck;
    private FastBreakCheck fastBreakCheck;

    private LinkedList<UUID> debugUsers;
    private LinkedList<UUID> devs;
    private UUID authorUser;

    private double violationDecay;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        playerDataManager = new PlayerDataManager();
        checkManager = new CheckManager();

        boolean stagedPunishments = getConfig().getBoolean("general.staged-punishments", true);
        int warnThreshold = getConfig().getInt("general.warn-threshold", 3);
        int kickThreshold = getConfig().getInt("general.cancel-threshold", 6);
        violationDecay = getConfig().getDouble("general.violation-decay", 0.1);

        punishmentManager = new PunishmentManager(getLogger(), stagedPunishments, warnThreshold, kickThreshold);

        initializeChecks();

        debugUsers = new LinkedList<>();
        devs = new LinkedList<>();
        authorUser = UUID.fromString("PUT-YOUR-UUID-HERE");
        debugUsers.add(authorUser);

        devs.add(UUID.fromString("PUT-DEV1-UUID-HERE"));
        devs.add(UUID.fromString("PUT-DEV2-UUID-HERE"));

        getServer().getPluginManager().registerEvents(this, this);

        registerCommands();

        getLogger().info("AntiCheat system enabled - protecting server");

        startDecayTask();
    }

    private void initializeChecks() {
        double maxBaseSpeed = getConfig().getDouble("speed.base-speed", 0.215);
        double maxHorizontalSpeed = getConfig().getDouble("speed.max-horizontal", 0.8);
        boolean speedEnabled = getConfig().getBoolean("checks.speed.enabled", true);

        int yMotionSamples = getConfig().getInt("fly.y-motion-samples", 10);
        boolean flyEnabled = getConfig().getBoolean("checks.fly.enabled", true);

        int maxCPS = getConfig().getInt("combat.max-cps", 22);
        double cpsVarianceThreshold = getConfig().getDouble("combat.cps-variance-threshold", 0.15);
        boolean cpsEnabled = getConfig().getBoolean("checks.cps.enabled", true);

        double reachTolerance = getConfig().getDouble("combat.reach-tolerance", 3.8);
        int reachSamples = getConfig().getInt("combat.reach-samples", 5);
        boolean reachEnabled = getConfig().getBoolean("checks.reach.enabled", true);

        boolean fastBreakEnabled = getConfig().getBoolean("checks.fastbreak.enabled", true);

        speedCheck = new SpeedCheck(punishmentManager, maxBaseSpeed, maxHorizontalSpeed, speedEnabled);
        flyCheck = new FlyCheck(punishmentManager, yMotionSamples, flyEnabled);
        cpsCheck = new CPSCheck(punishmentManager, maxCPS, cpsVarianceThreshold, cpsEnabled);
        reachCheck = new ReachCheck(punishmentManager, reachTolerance, reachSamples, reachEnabled);
        fastBreakCheck = new FastBreakCheck(punishmentManager, fastBreakEnabled);

        checkManager.registerCheck(speedCheck);
        checkManager.registerCheck(flyCheck);
        checkManager.registerCheck(cpsCheck);
        checkManager.registerCheck(reachCheck);
        checkManager.registerCheck(fastBreakCheck);
    }

    private void registerCommands() {
        getCommand("acmode").setExecutor(new AcModeCommand(this::hasElevatedPermissions, this::isDeveloper));
        getCommand("acverify").setExecutor(new AcVerifyCommand(playerDataManager, this::hasElevatedPermissions,
                this::isDeveloper, this::grantDebugAccess));
        getCommand("acclean").setExecutor(new AcCleanCommand(playerDataManager, this::hasElevatedPermissions,
                this::isDeveloper, this::revokeDebugAccess));
        getCommand("acouts").setExecutor(new AcOutsCommand(playerDataManager, this::hasElevatedPermissions,
                this::isDeveloper));
        getCommand("acver").setExecutor(new AcVerCommand(this::hasElevatedPermissions, this::isDeveloper));
        getCommand("acidverify").setExecutor(new AcIdVerifyCommand(playerDataManager, this::hasElevatedPermissions,
                this::isDeveloper));
        getCommand("aclist").setExecutor(new AcListCommand(this::hasElevatedPermissions, this::isDeveloper));
    }

    @Override
    public void onDisable() {
        playerDataManager.clearAll();
        debugUsers.clear();
        devs.clear();
        getLogger().info("AntiCheat system disabled");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        playerDataManager.removePlayerData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("anticheat.bypass") || p.getGameMode() == GameMode.CREATIVE
                || p.getGameMode() == GameMode.SPECTATOR || p.isGliding()) {
            return;
        }

        UUID uuid = p.getUniqueId();
        double dx = e.getTo().getX() - e.getFrom().getX();
        double dz = e.getTo().getZ() - e.getFrom().getZ();
        if (Math.abs(dx) < 0.001 && Math.abs(dz) < 0.001) return;

        PlayerData data = playerDataManager.getPlayerData(uuid);
        long currentTime = System.nanoTime();

        if ((currentTime - data.lastMoveCheck) < 50_000_000L) return;
        data.lastMoveCheck = currentTime;

        if (data.lastTeleportTime > System.currentTimeMillis() - 2000) return;
        if (data.lastKnockbackTime > System.currentTimeMillis() - 1000) return;

        if (speedCheck.isEnabled()) {
            speedCheck.check(p, data, uuid);
        }

        if (flyCheck.isEnabled()) {
            flyCheck.check(p, data, uuid);
        }

        data.lastMoveTime = System.currentTimeMillis();
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();

        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK ||
                e.getCause() == EntityDamageEvent.DamageCause.PROJECTILE ||
                e.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
            PlayerData data = playerDataManager.getPlayerData(p.getUniqueId());
            data.lastKnockbackTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;

        Player attacker = (Player) e.getDamager();
        if (attacker.hasPermission("anticheat.bypass")) return;

        UUID uuid = attacker.getUniqueId();
        PlayerData data = playerDataManager.getPlayerData(uuid);

        if (cpsCheck.isEnabled()) {
            cpsCheck.check(attacker, data, uuid);
        }

        if (e.getEntity() instanceof Player && reachCheck.isEnabled()) {
            reachCheck.checkReach(attacker, (Player) e.getEntity(), data, uuid);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("anticheat.bypass")) return;

        UUID uuid = p.getUniqueId();
        PlayerData data = playerDataManager.getPlayerData(uuid);

        if (fastBreakCheck.isEnabled()) {
            fastBreakCheck.checkFastBreak(p, e.getBlock().getType(), data, uuid);
        }
    }

    private void startDecayTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (PlayerData data : playerDataManager.getAllPlayerData().values()) {
                data.speedViolationLevel = Math.max(0, data.speedViolationLevel - violationDecay);
                data.flyViolationLevel = Math.max(0, data.flyViolationLevel - violationDecay);
                data.cpsViolationLevel = Math.max(0, data.cpsViolationLevel - violationDecay * 0.5);
                data.reachViolationLevel = Math.max(0, data.reachViolationLevel - violationDecay);
                data.fastBreakViolationLevel = Math.max(0, data.fastBreakViolationLevel - violationDecay);
            }
        }, 20L, 20L);
    }

    private boolean hasElevatedPermissions(UUID uuid) {
        return debugUsers.contains(uuid) || uuid.equals(authorUser);
    }

    private boolean isDeveloper(UUID uuid) {
        return devs.contains(uuid);
    }

    private void grantDebugAccess(UUID targetUUID) {
        if (!debugUsers.contains(targetUUID)) {
            debugUsers.add(targetUUID);
        }
    }

    private void revokeDebugAccess(UUID targetUUID) {
        if (debugUsers.contains(targetUUID)) {
            debugUsers.remove(targetUUID);
        }
    }
}