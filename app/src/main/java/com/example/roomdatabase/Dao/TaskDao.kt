package com.example.roomdatabase.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.roomdatabase.Model.Notes
import com.example.roomdatabase.Model.Task

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("SELECT * FROM task")
    fun getAllTask():LiveData<List<Task>>

    @Delete
    suspend fun delete(task: Task)
}