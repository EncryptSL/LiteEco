package com.github.encryptsl.lite.eco.utils

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class SchedulerHelper(private val plugin: Plugin, private val isFolia: Boolean) {
    /**
     * Executes a synchronous task immediately on the global main thread (Bukkit)
     * or on the Global Region Scheduler (Folia).
     *
     * @param task The action to be executed
     * @return A [TaskWrapper] to manage the scheduled task
     */
    fun runSyncNow(task: Runnable): TaskWrapper {
        return if (isFolia) {
            val scheduledTask = Bukkit.getGlobalRegionScheduler().run(plugin) { _ -> task.run() }
            TaskWrapper(foliaTask = scheduledTask)
        } else {
            val bukkitTask = Bukkit.getScheduler().runTask(plugin, task)
            TaskWrapper(bukkitTask = bukkitTask)
        }
    }

    /**
     * Executes a synchronous task on the thread owning a specific entity (e.g. Player).
     * Essential for Folia region-threading compliance.
     *
     * @param entity The entity context (e.g. Player)
     * @param task   The action to be executed
     * @return A [TaskWrapper] to manage the scheduled task
     */
    fun runEntityTask(entity: Entity, task: Runnable): TaskWrapper {
        return if (isFolia) {
            val scheduledTask = entity.scheduler.run(plugin, { _ -> task.run() }, null)
            TaskWrapper(foliaTask = scheduledTask)
        } else {
            val bukkitTask = Bukkit.getScheduler().runTask(plugin, task)
            TaskWrapper(bukkitTask = bukkitTask)
        }
    }

    /**
     * Safely dispatches a message to any [CommandSender] (Player, Console, CommandBlock)
     * on the appropriate thread context.
     *
     * @param sender  The message recipient
     * @param message The Adventure Component to be sent
     */
    fun sendMessageSync(sender: CommandSender, message: Component) {
        if (sender is Entity) {
            runEntityTask(sender) { sender.sendMessage(message) }
        } else {
            runSyncNow { sender.sendMessage(message) }
        }
    }

    /**
     * Schedules a repeating asynchronous task.
     *
     * @param delay  Delay in ticks (Bukkit) / converted to ms for Folia
     * @param period Period in ticks (Bukkit) / converted to ms for Folia
     * @param task   The action to be executed
     * @return A [TaskWrapper] to manage the scheduled task
     */
    fun runAsyncTimer(delay: Long, period: Long, task: Runnable): TaskWrapper {
        return if (isFolia) {
            val scheduledTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin,
                { _ -> task.run() },
                delay * 50,
                period * 50,
                TimeUnit.MILLISECONDS
            )
            TaskWrapper(foliaTask = scheduledTask)
        } else {
            val bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                task,
                delay,
                period
            )
            TaskWrapper(bukkitTask = bukkitTask)
        }
    }

    /**
     * Schedules a one-time asynchronous task with a delay.
     *
     * @param delay Delay in ticks (Bukkit) / converted to ms for Folia
     * @param task  The action to be executed
     * @return A [TaskWrapper] to manage the scheduled task
     */
    fun runAsyncLater(delay: Long, task: Runnable): TaskWrapper {
        return if (isFolia) {
            val scheduledTask = Bukkit.getAsyncScheduler().runDelayed(
                plugin,
                { _ -> task.run() },
                delay * 50,
                TimeUnit.MILLISECONDS
            )
            TaskWrapper(foliaTask = scheduledTask)
        } else {
            val bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(
                plugin,
                task,
                delay
            )
            TaskWrapper(bukkitTask = bukkitTask)
        }
    }

    /**
     * Executes an asynchronous task immediately.
     *
     * @param task The action to be executed
     * @return A [TaskWrapper] to manage the scheduled task
     */
    fun runAsyncNow(task: Runnable): TaskWrapper {
        return if (isFolia) {
            val scheduledTask = Bukkit.getAsyncScheduler().runNow(
                plugin,
                { _ -> task.run() }
            )
            TaskWrapper(foliaTask = scheduledTask)
        } else {
            val bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(
                plugin,
                task
            )
            TaskWrapper(bukkitTask = bukkitTask)
        }
    }

    /**
     * Cancels all scheduled tasks for this plugin across both Bukkit and Folia schedulers.
     */
    fun cancelTasks() {
        if (!isFolia) {
            Bukkit.getScheduler().cancelTasks(plugin)
        } else {
            Bukkit.getAsyncScheduler().cancelTasks(plugin)
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin)
        }
    }
}

class TaskWrapper(
    private val bukkitTask: org.bukkit.scheduler.BukkitTask? = null,
    private val foliaTask: ScheduledTask? = null
) {
    /**
     * Cancels the active task reference.
     */
    fun cancel() {
        bukkitTask?.cancel()
        foliaTask?.cancel()
    }
}