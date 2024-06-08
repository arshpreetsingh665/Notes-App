package com.example.roomdatabase.DataBase

import android.content.Context
import androidx.room.*
import com.example.roomdatabase.Dao.NotesDao
import com.example.roomdatabase.Model.Notes
import com.example.roomdatabase.converter.DateConverter

@Database(entities = [Notes::class], version =1)
@TypeConverters(DateConverter::class)
abstract class NotesDataBase:RoomDatabase() {
     abstract fun notesDao():NotesDao
     companion object{
         fun getAllNotes(context: Context):NotesDataBase{
             return Room.databaseBuilder(context,NotesDataBase::class.java,"Notes_Database").build()
         }
     }
}