package com.example.roomdatabase.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task")
data class Task (
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val title:String,
    val date:String,
    val time:String

)