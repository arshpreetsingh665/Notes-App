package com.example.roomdatabase.fragment1

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.roomdatabase.AddNotes.AddNotes
import com.example.roomdatabase.DataBase.NotesDataBase
import com.example.roomdatabase.Model.Notes
import com.example.roomdatabase.R
import com.example.roomdatabase.adapter.adpterForNotes
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime


class NotesFragment : Fragment() {
    lateinit var adapter: adpterForNotes
    var list: ArrayList<Notes> = ArrayList()
    private var isDataLoaded: Boolean = false

    private var isGridMode: Boolean = false


    @SuppressLint("StaticFieldLeak")
    companion object {
        lateinit var empty: LinearLayout
        lateinit var space: View
        var a:Int=0

    }

    private lateinit var fragmentContext: Context


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)
        val add = view.findViewById<ImageView>(R.id.add)
        val backup = view.findViewById<ImageView>(R.id.backup)
        empty = view.findViewById(R.id.line1)
        space = view.findViewById(R.id.linear)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = adpterForNotes(list, requireContext())
        recycler.adapter = adapter
        if (list.isNullOrEmpty()) {
            empty.visibility = View.VISIBLE
        } else {
            empty.visibility = View.GONE
        }

        NotesDataBase.getAllNotes(requireContext()).notesDao().getAllNotes()
            .observe(viewLifecycleOwner) { notesList ->
                //   list.clear()
                list.addAll(notesList)
                adapter.setData(notesList as ArrayList<Notes>)
                adapter.notifyDataSetChanged()
            }
        add.setOnClickListener {
            val Intent = Intent(requireContext(), AddNotes::class.java)
            startActivity(Intent)
        }


//        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//
//                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                val itemCount = layoutManager.itemCount
//
//                if (lastVisibleItemPosition != RecyclerView.NO_POSITION && lastVisibleItemPosition == itemCount - 1) {
//                    // Last item is visible, check if its bottom edge is above the bottom edge of the RecyclerView
//                    val lastView = layoutManager.findViewByPosition(lastVisibleItemPosition)
//                    val bottomEdge = lastView?.bottom ?: 0
//                    val recyclerViewBottom = recyclerView.height - recyclerView.paddingBottom
//
//                    if (bottomEdge <= recyclerViewBottom) {
//                        // Last item is fully or partially visible, make space layout visible
//                        space.visibility = View.VISIBLE
//                    } else {
//                        // Last item is not fully visible, hide space layout
//                        space.visibility = View.GONE
//                    }
//                } else {
//                    // Last item is not visible, hide space layout
//                    space.visibility = View.GONE
//                }
//            }
//        })


        fragmentContext = requireContext()
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadNotesDataFromStorage() {
        // Check if data has already been loaded
        if (!isDataLoaded) {
            Toast.makeText(requireContext(), "Loading data from storage...", Toast.LENGTH_SHORT)
                .show()
            lifecycleScope.launch {
                val jsonString = readJsonFromFile("Arshpreet")
                Toast.makeText(requireContext(), "Data loaded: $jsonString", Toast.LENGTH_SHORT)
                    .show()
                if (jsonString.isNotEmpty()) {
                    // Convert JSON string to notes list
                    val notesList = convertJsonToNotesList(jsonString)
                    notesList?.let {
                        // Get existing notes data from the database
                        val existingNotes =
                            NotesDataBase.getAllNotes(requireContext()).notesDao().getAllNotes()
                        // Data is not null, proceed with your logic
                        updateDatabaseAndUI(existingNotes, it)
                    }
                }
            }
            // Mark data as loaded
            isDataLoaded = false
        } else {
            // Data has already been loaded, no need to load again
            Toast.makeText(requireContext(), "Data already loaded.", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to update database and UI with loaded data
    // Function to update database and UI with loaded data
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateDatabaseAndUI(
        existingNotes: LiveData<List<Notes>>, notesList: List<Notes>
    ) {
        val currentTime = LocalDateTime.now()

        // Observe the LiveData to get the existing notes list
        existingNotes.observe(this@NotesFragment) { mergedList ->
            mergedList?.let {
                // Filter out notes that already exist in the database
                val newNotes = notesList.filter { updatedNote ->
                    // Check if the updated note's ID already exists in the merged list
                    val hasSameId = mergedList.any { it.id == updatedNote.id }
                    Toast.makeText(requireContext(), "Data $hasSameId", Toast.LENGTH_SHORT).show()

                    if (hasSameId) {
                        val conflictingNote = mergedList.find { it.id == updatedNote.id }
                        Log.d(
                            "NotesFragment",
                            "Duplicate ID found: ${updatedNote.id}. Conflicting note: $conflictingNote"
                        )
                    }
                    !hasSameId
                }

                // Update the timestamps for new notes
                val currentTime = LocalDateTime.now()
                val updatedNewNotes = newNotes.map { note ->
                    note.copy(date = currentTime)
                }

                // Add new notes to the merged list
                mergedList.addAll(updatedNewNotes)

                // Insert new notes into the database
                lifecycleScope.launch(Dispatchers.Main) {
                    NotesDataBase.getAllNotes(requireContext()).notesDao()
                        .insertNotes(updatedNewNotes.toTypedArray())
                }
                // Update the UI with new notes
                if (updatedNewNotes.isNotEmpty()) {
                    list.addAll(updatedNewNotes)
                    adapter.filteredList.addAll(updatedNewNotes)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }


    fun List<Notes>.addAll(newItems: List<Notes>): List<Notes> {
        val mutableList = this.toMutableList()
        mutableList.addAll(newItems)
        return mutableList.toList()
    }


    // Function to handle initiating another backup
    fun initiateAnotherBackup() {
        // Reset the flag to indicate that data needs to be loaded again
        isDataLoaded = false
        // Perform any other backup-related tasks if needed
        // For example, you can prompt the user to select a new backup file
    }


    private suspend fun readJsonFromFile(fileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
//                val directory = File("/storage/emulated/0/Arshpreet", "Arshpreet")
//                if (!directory.exists()) {
//                    Log.e("NotesFragment", "Directory does not exist: ${directory.absolutePath}")
//                    return@withContext ""
//                }
                val directory: File = if (Build.VERSION.SDK_INT <=30) {
                    File(requireContext().getFilesDir(), "Arhspreet")
                } else {
                    // For Android versions below Q, you may choose a different fallback option
                    File(Environment.getExternalStorageDirectory(), "Arhspreet")
                }

                if (!directory.exists()) {
                    directory.mkdirs()
                }
                val file = File(directory, fileName)
                if (!file.exists()) {
                    Log.e("NotesFragment", "File does not exist: ${file.absolutePath}")
                    return@withContext ""
                }
                Log.d("NotesFragment", "File exists at: ${file.absolutePath}")
                val stringBuilder = StringBuilder()
                val br = BufferedReader(FileReader(file))
                var line: String? = br.readLine()
                while (line != null) {
                    stringBuilder.append(line).append('\n')
                    line = br.readLine()
                }
                br.close()
                stringBuilder.toString()
            } catch (e: Exception) {
                Log.e("NotesFragment", "Error reading file: ${e.message}")
                ""
            }
        }
    }

    private fun convertJsonToNotesList(jsonData: String?): List<Notes>? {
        if (jsonData.isNullOrEmpty()) {
            return null
        }
        return try {
            Gson().fromJson(jsonData, Array<Notes>::class.java).toList()
        } catch (e: Exception) {
            null
        }
    }

    // Your other fragment code...


    fun perform() {
        lifecycleScope.launch {
            NotesDataBase.getAllNotes(requireContext()).notesDao().deleteAll()
        }
        NotesFragment.empty.visibility = View.VISIBLE
        adapter.filteredList.clear()
        list.clear()
        adapter.notifyDataSetChanged()

    }

    fun search(query: String) {
        adapter.search(query)
    }

    fun list() {
        val recycler = view?.findViewById<RecyclerView>(R.id.recycler)
        if (isGridMode == false) {
            recycler?.layoutManager = LinearLayoutManager(requireContext())
        } else {
            recycler?.layoutManager = GridLayoutManager(requireContext(), 2)
        }

        NotesDataBase.getAllNotes(requireContext()).notesDao().getAllNotes()
            .removeObservers(viewLifecycleOwner) // Remove any existing observers
        NotesDataBase.getAllNotes(requireContext()).notesDao().getAllNotes()
            .observe(viewLifecycleOwner) { notesList ->
                list.clear()
                list.addAll(notesList)
                adapter.setData(notesList as ArrayList<Notes>)
                adapter.notifyDataSetChanged()
            }
    }


    fun isGridView(): Boolean {
        return isGridMode
    }

    fun toggleListView() {
        isGridMode = !isGridMode
        // Update UI to reflect the new view mode (list or grid)
        // For example, update RecyclerView layout manager here
        // recyclerView.layoutManager = if (isGridMode) GridLayoutManager(context, 2) else LinearLayoutManager(context)
    }

    fun clear() {
        Toast.makeText(requireContext(),"mssg"+adapter.slectedItemList.size,Toast.LENGTH_SHORT).show()

        adapter.slectedItemList.clear()
        Toast.makeText(requireContext(),"mssg"+adapter.slectedItemList.size,Toast.LENGTH_SHORT).show()

        adapter.notifyDataSetChanged()
        if (adapter.slectedItemList.isEmpty()){
        a=1
        }
    }


}