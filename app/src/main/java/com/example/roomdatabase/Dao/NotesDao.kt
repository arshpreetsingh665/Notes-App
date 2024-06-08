package com.example.roomdatabase.Dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.roomdatabase.Model.Notes

@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: Array<Notes>)

    @Delete
    suspend fun delete(notes: Notes)

    @Update
    suspend fun update(notes: Notes)

    @Query("SELECT * FROM notes")
    fun getAllNotes(): LiveData<List<Notes>>

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Query("DELETE FROM notes WHERE id IN (:noteIds)")
    suspend fun deleteByIds(noteIds: List<Long>)
}