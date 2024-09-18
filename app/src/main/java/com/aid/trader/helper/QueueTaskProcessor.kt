package com.aid.trader.helper

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class QueueTaskProcessor(private val interval: Long = 300_000L) { // interval in milliseconds (300_000 ms = 5 minutes)
    private val taskQueue = LinkedBlockingQueue<Task>()
    private val taskRegistry = ConcurrentHashMap<String, Task>()
    private val lock = Mutex()

    data class Task(
        val id: String,
        val func: suspend () -> Unit,
        val timestamp: LocalDateTime
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun addTask(func: suspend () -> Unit): String {
        val taskId = UUID.randomUUID().toString()
        val task = Task(
            id = taskId,
            func = func,
            timestamp = LocalDateTime.now()
        )
        taskQueue.put(task)
        taskRegistry[taskId] = task
        println("Task $taskId added to the queue.")
        return taskId
    }

    fun removeTask(taskId: String) {
        val task = taskRegistry.remove(taskId)
        if (task != null) {
            println("Task $taskId removed from the queue.")
        } else {
            println("Task $taskId not found in the queue.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun processTasks() {
        lock.withLock {
            while (taskQueue.isNotEmpty()) {
                val task = taskQueue.poll()
                if (task != null && taskRegistry.containsKey(task.id)) {
                    try {
                        println("Processing task ${task.id} at ${LocalDateTime.now()}")
                        task.func()
                        println("Task ${task.id} completed at ${LocalDateTime.now()}")

                        // Re-queue the task after it completes
//                        addTask(task.func)

                    } catch (e: Exception) {
                        println("Task ${task.id} failed with error: $e")
                    } finally {
                        taskRegistry.remove(task.id)
                    }
                } else {
                    println("Task ${task?.id} has been removed and will not be processed.")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun start() {
        while (true) {
            val startTime = LocalDateTime.now()
            println("Starting task processing at $startTime")
            processTasks()  // Process tasks
            val endTime = LocalDateTime.now()
            val elapsedTime = java.time.Duration.between(startTime, endTime).seconds
            println("Task processing completed at $endTime, duration: $elapsedTime seconds")
            delay(interval)  // Wait for the next interval
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun run() {
        CoroutineScope(Dispatchers.Default).launch {
            start()
        }
    }
}

// Usage Example
@RequiresApi(Build.VERSION_CODES.O)
fun main() {
    val processor = QueueTaskProcessor(60_000L) // 1-minute interval

    // Add tasks
    processor.addTask {
        println("Executing task 1 at ${LocalDateTime.now()}")
    }

    processor.addTask {
        println("Executing task 2 at ${LocalDateTime.now()}")
    }

    // Run the processor
    processor.run()

    // Keep the main function running to allow the processor to operate
    runBlocking { delay(5 * 60_000L) } // Run for 5 minutes
}
