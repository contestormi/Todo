package com.example.todolist.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks ORDER BY createdAtMillis DESC")
    fun observeAllOrderByCreatedAtDesc(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY priority DESC, createdAtMillis DESC")
    fun observeAllOrderByPriorityDesc(): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT tasks.*
        FROM tasks
        JOIN tasks_fts ON tasks.id = tasks_fts.rowid
        WHERE tasks_fts MATCH :ftsQuery
        ORDER BY createdAtMillis DESC
        """
    )
    fun searchOrderByCreatedAtDesc(ftsQuery: String): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT tasks.*
        FROM tasks
        JOIN tasks_fts ON tasks.id = tasks_fts.rowid
        WHERE tasks_fts MATCH :ftsQuery
        ORDER BY priority DESC, createdAtMillis DESC
        """
    )
    fun searchOrderByPriorityDesc(ftsQuery: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}