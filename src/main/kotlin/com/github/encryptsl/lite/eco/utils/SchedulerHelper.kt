package com.github.encryptsl.lite.eco.utils

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class SchedulerHelper(private val plugin: Plugin, private val isFolia: Boolean) {
    /**
     * Schedules a repeating asynchronous task.
     * * @param delay Delay in ticks (Bukkit) / converted to ms for Folia
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
     * * @param delay Delay in ticks (Bukkit) / converted to ms for Folia
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
     * * @param task The action to be executed
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

    fun cancelTasks() {
        if (!isFolia) {
            Bukkit.getScheduler().cancelTasks(plugin)
        } else {
            Bukkit.getAsyncScheduler().cancelTasks(plugin)
        }
    }
}

class TaskWrapper(
    private val bukkitTask: org.bukkit.scheduler.BukkitTask? = null,
    private val foliaTask: ScheduledTask? = null
) {
    fun cancel() {
        bukkitTask?.cancel()
        foliaTask?.cancel()
    }
}