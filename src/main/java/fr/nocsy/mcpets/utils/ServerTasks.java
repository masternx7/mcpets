package fr.nocsy.mcpets.utils;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import fr.nocsy.mcpets.MCPets;

/**
 * Paper/Folia-safe task helpers. On Paper these run on the main thread; on
 * Folia they run on the correct region.
 */
public final class ServerTasks {

    private ServerTasks() {
    }

    private static Plugin plugin() {
        return MCPets.getInstance();
    }

    public static void runGlobal(final Runnable run) {
        Bukkit.getGlobalRegionScheduler().execute(plugin(), run);
    }

    public static ScheduledTask runGlobalLater(final Runnable run, final long delayTicks) {
        return Bukkit.getGlobalRegionScheduler()
                .runDelayed(plugin(), task -> run.run(), Math.max(1L, delayTicks));
    }

    public static ScheduledTask runGlobalTimer(final Runnable run, final long delayTicks, final long periodTicks) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                plugin(), task -> run.run(), Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    public static void runAt(final Location location, final Runnable run) {
        if (location == null || location.getWorld() == null) {
            return;
        }
        Bukkit.getRegionScheduler().execute(plugin(), location, run);
    }

    public static ScheduledTask runAtLater(final Location location, final Runnable run, final long delayTicks) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return Bukkit.getRegionScheduler().runDelayed(
                plugin(), location, task -> run.run(), Math.max(1L, delayTicks));
    }

    public static void runOn(final Entity entity, final Runnable run) {
        if (entity == null) {
            return;
        }
        // Do not call entity.isValid() here: on Folia that can itself fail
        // the region thread check when invoked from the global scheduler.
        entity.getScheduler().run(plugin(), task -> run.run(), null);
    }

    public static ScheduledTask runOnLater(final Entity entity, final Runnable run, final long delayTicks) {
        if (entity == null) {
            return null;
        }
        return entity.getScheduler().runDelayed(plugin(), task -> run.run(), null, Math.max(1L, delayTicks));
    }

    public static ScheduledTask runOnTimer(final Entity entity,
                                           final Consumer<ScheduledTask> run,
                                           final Runnable retired,
                                           final long delayTicks,
                                           final long periodTicks) {
        if (entity == null) {
            return null;
        }
        return entity.getScheduler().runAtFixedRate(
                plugin(), run, retired, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    public static ScheduledTask runAsync(final Runnable run) {
        return Bukkit.getAsyncScheduler().runNow(plugin(), task -> run.run());
    }

    public static ScheduledTask runAsyncTimer(final Runnable run, final long delayTicks, final long periodTicks) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin(),
                task -> run.run(),
                Math.max(1L, delayTicks) * 50L,
                Math.max(1L, periodTicks) * 50L,
                TimeUnit.MILLISECONDS);
    }

    public static void cancel(final ScheduledTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public static boolean isOwned(final Location location) {
        return location == null || location.getWorld() == null || Bukkit.isOwnedByCurrentRegion(location);
    }

    public static boolean isOwned(final Entity entity) {
        return entity == null || Bukkit.isOwnedByCurrentRegion(entity);
    }

    public static void cancelAll() {
        final Plugin plugin = plugin();
        if (plugin == null) {
            return;
        }
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }
}
