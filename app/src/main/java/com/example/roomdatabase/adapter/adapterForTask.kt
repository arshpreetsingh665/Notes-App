package com.example.roomdatabase.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.roomdatabase.DataBase.TaskDb
import com.example.roomdatabase.Model.Task
import com.example.roomdatabase.databinding.TaskViewBinding
import com.example.roomdatabase.fragement2.TaskContainer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class adapterForTask(
    val list: ArrayList<Task>,
    val context: Context,
    val listener: TaskClickListener
) : RecyclerView.Adapter<adapterForTask.ViewHolder>() {
    class ViewHolder(val taskBinding: TaskViewBinding) : RecyclerView.ViewHolder(taskBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = TaskViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface TaskClickListener {
        fun onTaskClicked(title: String, date: String, time: String, id: Int)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.taskBinding.titletext.text = data.title
        val dateandtime = data.date + " " + data.time
        holder.taskBinding.dateandtime.text = dateandtime
        holder.taskBinding.delete.setOnClickListener {
            deleteDialog(data.time, data.date, data.title, data.id)
        }
        holder.taskBinding.linear1.setOnClickListener {
            listener.onTaskClicked(data.title, data.date, data.time, data.id)
        }

    }

    fun deleteDialog(time: String, date: String, title: String, id: Int) {
        val alert = AlertDialog.Builder(context)
        alert.setMessage("Are you sure to delete this Note ?")
        alert.setCancelable(false)
        alert.setPositiveButton("Yes") { dialog, which ->

            GlobalScope.launch {
                // Delete the item from the database
                TaskDb.getAllTasks(context).taskDao()
                    .delete(Task(id, title, date, time))
            }
            val index = list.indexOfFirst { it.id == id }
            if (index != -1) {
                list.removeAt(index)
                if (list.size==0){
                    TaskContainer.empty.visibility=View.VISIBLE                }
                // Notify adapter about the change in the dataset
                notifyDataSetChanged()
            }
        }
        alert.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        alert.show()
    }
}