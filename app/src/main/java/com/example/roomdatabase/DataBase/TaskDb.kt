package com.example.roomdatabase.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.roomdatabase.Dao.TaskDao
import com.example.roomdatabase.Model.Task

@Database(entities = [Task::class], version = 1)
abstract class TaskDb:RoomDatabase() {
    abstract fun taskDao():TaskDao
    companion object{
        fun getAllTasks(context:Context): TaskDb {
            return Room.databaseBuilder(context,TaskDb::class.java,"TaskDataBase").build()
        }
    }
}