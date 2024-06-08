package com.example.roomdatabase.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.roomdatabase.AddNotes.AddNotes
import com.example.roomdatabase.DataBase.NotesDataBase
import com.example.roomdatabase.MainActivity
import com.example.roomdatabase.Model.Notes
import com.example.roomdatabase.R
import com.example.roomdatabase.databinding.ItemViewBinding
import com.example.roomdatabase.fragment1.NotesFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class adpterForNotes(private var list: ArrayList<Notes>, private val context: Context) :
    RecyclerView.Adapter<adpterForNotes.ViewHolder>() {

    var filteredList: ArrayList<Notes> = ArrayList(list)


    var slectedItemList: ArrayList<Long> = ArrayList()
    private var selectedItems: Boolean = false
    private var a: Boolean = false

    class ViewHolder(val itemViewBinding: ItemViewBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(view)

    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged", "ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = filteredList[position]
        holder.itemViewBinding.title.text = data.title
        holder.itemViewBinding.description.setText(data.descriptor)
        val date1 = formatDate(data.date)
//holder.itemViewBinding.delete.visibility=View.GONE
        holder.itemViewBinding.date.text = date1
//        Toast.makeText(context, "cjasdjva" + slectedItemList.size, Toast.LENGTH_SHORT).show()
        holder.itemViewBinding.overflow.setOnClickListener {
            // Create a PopupMenu
            val popupMenu = PopupMenu(context, holder.itemView)
            popupMenu.inflate(R.menu.menu_for_itemview)

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit -> {
                        // Handle edit action
                        val intent = Intent(context, AddNotes::class.java)
                        intent.putExtra("title", data.title)
                        intent.putExtra("description", data.descriptor)
                        intent.putExtra("id", data.id)
                        context.startActivity(intent)
                        true
                    }

                    R.id.share -> {
                        val titleText = data.title
                        val descriptionText = data.descriptor
                        val shareText = "$titleText\n$descriptionText"

                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }

                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        // Handle edit action

                        true
                    }

                    R.id.delete -> {
                        // Handle delete action
                        deleteDialog(data.descriptor, data.id!!, data.title, data.date, position)
                        true
                    }

                    else -> false
                }
            }

            // Show the PopupMenu
            popupMenu.show()
        }
        if (slectedItemList.contains(data.id)) {
            holder.itemViewBinding.notesContainer.setBackgroundResource(R.drawable.back3)
        } else {
            holder.itemViewBinding.notesContainer.setBackgroundResource(R.drawable.back1)
        }

        if (slectedItemList.isEmpty()) {
            holder.itemViewBinding.overflow.visibility = View.VISIBLE
            NotesFragment.a = 1
        } else {
            holder.itemViewBinding.overflow.visibility = View.GONE
            NotesFragment.a = 0
        }

        holder.itemViewBinding.notesContainer.setOnLongClickListener {

            if (!a) {
                //   Toast.makeText(context, "cjasdjva" + a, Toast.LENGTH_SHORT).show()
                MainActivity.searchBtn.visibility = View.GONE
                MainActivity.deleteAll.visibility = View.VISIBLE

                toggleSelection(holder, position)
                a = true


            }
            //   Toast.makeText(context,"mssg"+selectedItems,Toast.LENGTH_SHORT).show()
            true
        }

        holder.itemViewBinding.notesContainer.setOnClickListener {
            //    Toast.makeText(context, "a" + a, Toast.LENGTH_SHORT).show()
            if (a) {
                toggleSelection(holder, position)


            } else {
                val intent = Intent(context, AddNotes::class.java)
                intent.putExtra("title", data.title)
                intent.putExtra("description", data.descriptor)
                intent.putExtra("id", data.id)
                context.startActivity(intent)
            }
        }
        // Click listeners
//
//        holder.itemViewBinding.notesContainer.setOnClickListener {
//
//        }

//
//        holder.itemViewBinding.delete.setOnClickListener {
//            //   Toast.makeText(context, "size" +slectedItemList.size, Toast.LENGTH_SHORT).show()
//
//        }
        MainActivity.deleteAll.setOnClickListener {
            //   Toast.makeText(context, "mssg" + slectedItemList.size, Toast.LENGTH_SHORT).show()
            val alertDialog=AlertDialog.Builder(context)
            alertDialog.setMessage("Are you sure to delete selected Notes ?")
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton("Yes"){
                    dialog, which ->
                deleteSelectedItemsFromDatabase()
                holder.itemViewBinding.notesContainer.setBackgroundResource(R.drawable.back1)
                if (filteredList.isEmpty()) {
                    NotesFragment.empty.visibility = View.VISIBLE
                }
                MainActivity.searchBtn.visibility = View.VISIBLE

                MainActivity.deleteAll.visibility = View.GONE
            }
            alertDialog.setNegativeButton("No"){
                    dialog, which ->
                dialog.dismiss()
            }
           alertDialog.show()
        }

        if (filteredList.isEmpty()) {
            NotesFragment.empty.visibility = View.VISIBLE
        } else {
            NotesFragment.empty.visibility = View.GONE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteSelectedItemsFromDatabase() {
        for (itemId in slectedItemList) {
            val selectedItem = filteredList.find { it.id == itemId }
            selectedItem?.let {
                GlobalScope.launch {
                    NotesDataBase.getAllNotes(context).notesDao().delete(it)
                }
            }
        }

        // Remove the deleted items from the filtered list
        filteredList.removeAll { slectedItemList.contains(it.id) }
        list.removeAll { slectedItemList.contains(it.id) }

        // Clear the selected items list after deletion
        slectedItemList.clear()
        selectedItems = false
        //   Toast.makeText(context, "mssg" + slectedItemList.size, Toast.LENGTH_SHORT).show()
        // Notify the adapter that the data set has changed
        a = false
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun toggleSelection(holder: ViewHolder, position: Int) {

        if (!a) {

            selectedItems = !selectedItems
            //    Toast.makeText(context, "cjasdjva" + selectedItems, Toast.LENGTH_SHORT).show()
        } else {
            selectedItems = !slectedItemList.contains(filteredList[position].id)
        }
        if (selectedItems) {
            if (!slectedItemList.contains(filteredList[position].id)) {

                slectedItemList.add(filteredList[position].id!!)
            }

        } else {
            slectedItemList.remove(filteredList[position].id)
            if (slectedItemList.isEmpty()) {
                a = false
                MainActivity.searchBtn.visibility = View.VISIBLE
                MainActivity.deleteAll.visibility = View.GONE
                //  holder.itemViewBinding.delete.visibility = View.VISIBLE

            } else {

                //    holder.itemViewBinding.delete.visibility = View.GONE
            }

        }

        if (slectedItemList.contains(filteredList[position].id)) {
            holder.itemViewBinding.notesContainer.setBackgroundResource(R.drawable.back3)
        } else {
            holder.itemViewBinding.notesContainer.setBackgroundResource(R.drawable.back1)
        }

        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return filteredList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun search(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(list)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            for (item in list) {
                if (item.title.lowercase(Locale.getDefault())
                        .contains(lowerCaseQuery) || item.descriptor.lowercase(Locale.getDefault())
                        .contains(lowerCaseQuery)
                ) {
                    filteredList.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setData(newList: ArrayList<Notes>) {
        val filteredNewList = newList.filter { note ->
            !list.any { it.id == note.id }
        }
        list.addAll(filteredNewList)
        filteredList.addAll(filteredNewList)
        search("") // Clear the filter when setting new data
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(date: LocalDateTime): String {
        // Define the desired date-time format
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

        // Format the LocalDateTime object using the formatter
        return date.format(formatter)
    }


    //    private fun updateSelectionState() {
//        isAnyItemSelected = slectedItemList.isNotEmpty()
//    }
    fun deleteDialog(
        description: String, id: Long, title: String, date: LocalDateTime, position: Int
    ) {
        val alert = AlertDialog.Builder(context)
        alert.setMessage("Are you sure to delete this Note ?")
        alert.setCancelable(false)
        alert.setPositiveButton("Yes") { dialog, which ->
            if (slectedItemList.isEmpty()) {
                GlobalScope.launch {
                    // Delete the item from the database
                    NotesDataBase.getAllNotes(context).notesDao()
                        .delete(Notes(id, title, description, date))
                }
                // Remove the item from the list
                filteredList.removeAt(position)
                list.removeAt(position)
                if (filteredList.isEmpty()) {
                    NotesFragment.empty.visibility = View.VISIBLE
                }
                // Notify the adapter that the item has been removed
                notifyDataSetChanged()
            }
        }
        alert.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        alert.show()
    }
}


