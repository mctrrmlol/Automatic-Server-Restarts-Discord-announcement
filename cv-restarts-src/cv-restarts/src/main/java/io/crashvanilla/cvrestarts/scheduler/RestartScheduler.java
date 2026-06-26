package io.crashvanilla.cvrestarts.scheduler;

import io.crashvanilla.cvrestarts.CvRestarts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles the daily restart announcement schedule.
 *
 * Timeline (all times in UTC-4):
 *   7:50:00 PM → Discord warning + in-game action bar "10 MINUTES"
 *   7:59:55 PM → 5-second countdown begins in action bar
 *   8:00:00 PM → server stops (via /stop)
 */
public class RestartScheduler {

    private final CvRestarts plugin;
    private BukkitTask mainTask;
    private BukkitTask countdownTask;
    private final AtomicBoolean active = new AtomicBoolean(false);

    // Ticks between each main-loop poll (20 ticks = 1 second)
    private static final long POLL_INTERVAL_TICKS = 20L;

    public RestartScheduler(CvRestarts plugin) {
        this.plugin = plugin;
    }

    /** Starts the scheduler. Called by /startrestart. */
    public void start() {
        if (active.getAndSet(true)) {
            return; // already running
        }

        mainTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            ZoneOffset offset = ZoneOffset.ofHours(plugin.getConfig().getInt("timezone-offset", -4));
            ZonedDateTime now = ZonedDateTime.now(offset);

            int h = now.getHour();
            int m = now.getMinute();
            int s = now.getSecond();

            // 7:50:00 PM — fire warning
            if (h == 19 && m == 50 && s == 0) {
                fireWarning();
            }

            // 7:59:55 PM — start 5-second countdown
            if (h == 19 && m == 59 && s == 55) {
                startCountdown(false);
            }

        }, 0L, POLL_INTERVAL_TICKS);

        plugin.getLogger().info("[cv-restarts] Restart scheduler is now ACTIVE. Server will restart daily at 8:00 PM UTC-4.");
    }

    /** Fires the 10-minute warning (Discord + action bar). */
    public void fireWarning() {
        // Discord
        if (plugin.getDiscordBot() != null) {
            plugin.getDiscordBot().sendRestartWarning();
        }

        // Action bar — must run on main thread
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Component msg = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&bs\u1D07\u0280v\u1D07\u0280 \u0280\u1D07s\u1D1B\u1D00\u0280\u1D1B \u026A\u0274 10 \u1D0D\u026A\u0274\u1D1B\u1D07s");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendActionBar(msg);
            }
        });

        // Keep action bar visible for a few seconds (re-send every second for 10s)
        scheduleActionBarRefresh("&bs\u1D07\u0280v\u1D07\u0280 \u0280\u1D07s\u1D1B\u1D00\u0280\u1D1B \u026A\u0274 10 \u1D0D\u026A\u0274\u1D1B\u1D07s", 10);
    }

    /**
     * Starts a 5-second countdown in the action bar.
     * @param testMode if true, does NOT call /stop at the end
     */
    public void startCountdown(boolean testMode) {
        AtomicInteger remaining = new AtomicInteger(5);

        countdownTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            int secs = remaining.getAndDecrement();

            if (secs > 0) {
                String raw = "&bs\u1D07\u0280v\u1D07\u0280 \u0280\u1D07s\u1D1B\u1D00\u0280\u1D1B \u026A\u0274 " + secs + "s";
                Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(msg);
                }
            } else {
                // Countdown finished
                if (countdownTask != null) {
                    countdownTask.cancel();
                    countdownTask = null;
                }

                if (!testMode) {
                    plugin.getLogger().info("[cv-restarts] Restarting server now!");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                } else {
                    plugin.getLogger().info("[cv-restarts] TEST MODE: Countdown finished. Server would restart here.");
                    // Notify online players
                    Component done = Component.text("[cv-restarts] TEST: Countdown complete — server would restart now.", NamedTextColor.GREEN);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("cvrestarts.admin")) {
                            p.sendMessage(done);
                        }
                    }
                }
            }
        }, 0L, 20L); // every second
    }

    /**
     * Sends an action bar message every second for {@code durationSeconds} seconds,
     * so players actually see it (action bar fades after ~1.5s without re-sends).
     */
    private void scheduleActionBarRefresh(String rawMessage, int durationSeconds) {
        AtomicInteger ticks = new AtomicInteger(durationSeconds);
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (ticks.getAndDecrement() <= 0) {
                task.cancel();
                return;
            }
            Component msg = LegacyComponentSerializer.legacyAmpersand().deserialize(rawMessage);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendActionBar(msg);
            }
        }, 20L, 20L);
    }

    /** Cancels all running tasks and marks scheduler as inactive. */
    public void cancel() {
        active.set(false);
        if (mainTask != null) {
            mainTask.cancel();
            mainTask = null;
        }
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    public boolean isActive() {
        return active.get();
    }
}
