package com.example.todoapp.data.repository

import com.example.todoapp.data.db.TaskDao
import com.example.todoapp.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

class TaskRepository (
    private val taskDao: TaskDao
) {

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    suspend fun insertAndReturnId(task: Task): Int {
        return taskDao.insertAndReturnId(task).toInt()
    }

    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks()
    }

    suspend fun getTaskByIdOnce(id: Int): Task? = taskDao.getTaskByIdOnce(id)

}
