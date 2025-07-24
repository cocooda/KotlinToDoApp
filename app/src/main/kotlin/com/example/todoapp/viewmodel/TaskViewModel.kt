package com.example.todoapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.Task
import com.example.todoapp.utils.ReminderScheduler
import com.example.todoapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing UI-related data for tasks.
 * Connects the repository layer with the UI (Fragment/Activity),
 * and handles asynchronous operations using Kotlin coroutines.
 */
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    /**
     * StateFlow that exposes the current list of tasks.
     * Automatically updates when the task database changes.
     */
    val allTasks: StateFlow<List<Task>> = repository
        .getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Retrieves a task by its ID once (non-observably).
     * Used for editing or viewing a specific task.
     */
    suspend fun getTaskByIdOnce(id: Int): Task? {
        return repository.getTaskByIdOnce(id)
    }

    /**
     * Inserts a task and schedules a notification if a due date is set.
     * Uses insertAndReturnId to retrieve the generated ID for notification uniqueness.
     */
    fun insert(task: Task, context: Context) {
        viewModelScope.launch {
            val taskId = repository.insertAndReturnId(task)

            // If the task has a due date in the future, schedule a reminder
            task.dueDate?.let { dueMillis ->
                val delayMillis = dueMillis - System.currentTimeMillis()
                if (delayMillis > 0) {
                    ReminderScheduler.scheduleReminder(
                        context,
                        delayMillis,
                        taskId,
                        task.title
                    )
                }
            }
        }
    }

    /**
     * Re-inserts a previously deleted task. Used for undo functionality.
     */
    fun undoDelete(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    /**
     * Updates a task in the database and schedules a new reminder
     * if the due date is in the future.
     */
    fun update(task: Task, context: Context) {
        viewModelScope.launch {
            val taskId = repository.insertAndReturnId(task)

            // Reschedule reminder for updated task if necessary
            task.dueDate?.let { dueMillis ->
                val delayMillis = dueMillis - System.currentTimeMillis()
                if (delayMillis > 0) {
                    ReminderScheduler.scheduleReminder(
                        context,
                        delayMillis,
                        taskId,
                        task.title
                    )
                }
            }
        }
    }

    /**
     * Deletes a task from the database.
     */
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
}
