package com.example.roomdatabase.Model

import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notes")
data class Notes(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long ?= 0,
    val title: String,
    val descriptor: String,
    val date: LocalDateTime
)
