package com.example.roomdatabase.fragement2

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdatabase.AlarmReceiver
import com.example.roomdatabase.DataBase.TaskDb
import com.example.roomdatabase.Model.Task
import com.example.roomdatabase.R
import com.example.roomdatabase.adapter.adapterForTask
import com.example.roomdatabase.databinding.FragmentTaskContainerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class TaskContainer : Fragment(), adapterForTask.TaskClickListener {
    val NOTIFICATION_CHANNEL_ID = "Task"
    lateinit var mBinding: FragmentTaskContainerBinding
    lateinit var date: TextView
    private val REQUEST_CODE_SCHEDULE_ALARM = 123
    lateinit var date1: String
    var a: Int = 0
    var b: Int = 0
    lateinit var alarmManager: AlarmManager
    lateinit var formattedTime: String
    private val binding get() = mBinding
    var list: ArrayList<Task> = ArrayList()
    lateinit var mAdapter: adapterForTask

    companion object {
        lateinit var empty: LinearLayout


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentTaskContainerBinding.inflate(inflater, container, false)
//        binding.add.setOnClickListener {
//            bottomsheet()
//        }
        empty = binding.line1

        return binding.root
    }

    @SuppressLint("MissingInflatedId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // showData()
//        binding.add.setOnClickListener {
//            bottomsheet()
//        }
    }

    override fun onResume() {
        showData()
        empty = binding.line1


        binding.add.setOnClickListener {
            Toast.makeText(requireContext(), "mssg", Toast.LENGTH_SHORT).show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(requireContext(), "mssg1", Toast.LENGTH_SHORT).show()
                    bottomsheet()

                } else {
                    Toast.makeText(requireContext(), "mssg2", Toast.LENGTH_SHORT).show()
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                bottomsheet()
            }
        }
        super.onResume()
    }

    private fun showDatePickerDialog(year: Int, month: Int, dayOfMonth: Int) {
        val datePickerDialog =
            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date here
                date1 = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                date.text = date1
            }, year, month, dayOfMonth)
        datePickerDialog.show()
    }

    fun showData() {
        getData()
        binding.taskContainer.layoutManager = LinearLayoutManager(requireContext())
        mAdapter = adapterForTask(list, requireContext(), this)
        binding.taskContainer.adapter = mAdapter


    }

    fun getData() {
        TaskDb.getAllTasks(requireContext()).taskDao().getAllTask().observe(viewLifecycleOwner) {
            Log.d("TaskContainer", "Retrieved data: $it")
            list.clear()
            // Add the retrieved tasks to your list
            list.addAll(it)
            Log.d("TaskContainer", "List contents after data retrieval: $list")
            if (list.size == 0) {
                binding.line1.visibility = View.VISIBLE

            } else {
                binding.line1.visibility = View.GONE
            }
            // Notify the adapter of data changes
            mAdapter.notifyDataSetChanged()

//            mAdapter.setData(list as ArrayList<Task>)
//            mAdapter.notifyDataSetChanged()
        }

    }


    @SuppressLint("SuspiciousIndentation")
    fun bottomsheet() {
        formattedTime = ""
        date1 = ""
        Toast.makeText(requireContext(), "mssg", Toast.LENGTH_SHORT).show()
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.task_sheet, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)

        val calendarImageView = view.findViewById<ImageView>(R.id.image)
        val watch = view.findViewById<ImageView>(R.id.watch)
        val cancel = view.findViewById<ImageView>(R.id.cancel)
        date = view.findViewById<TextView>(R.id.date)
        val time = view.findViewById<TextView>(R.id.time)
        val title = view.findViewById<EditText>(R.id.text)
        val Done = view.findViewById<TextView>(R.id.done)
        calendarImageView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            showDatePickerDialog(year, month, dayOfMonth)
        }
        watch.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                requireContext(), { view, hourOfDay, minute ->
                    // on below line we are setting selected
                    formattedTime = String.format(
                        "%02d:%02d %s", hourOfDay % 12, minute, if (hourOfDay < 12) "AM" else "PM"
                    )
                    time.text = formattedTime

                }, hour, minute, false
            )
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show()
        }
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        Done.setOnClickListener {
            dialog.dismiss()
            val titletext = title.text.toString()
            if (titletext.isEmpty() || date1.isNullOrEmpty() || formattedTime.isNullOrEmpty()) {
                alert()
            } else {
                lifecycleScope.launch(Dispatchers.Main) {
                    TaskDb.getAllTasks(requireContext()).taskDao()
                        .insert(Task(0, titletext, date1, formattedTime))
                    getData()
                }
                scheduleNotification(Task(0, titletext, date1, formattedTime))
            }
        }

        dialog.show()

    }

    fun bottomsheet(title1: String, date2: String, time1: String, id1: Int) {

        formattedTime = ""
        date1 = ""
        Toast.makeText(requireContext(), "mssg", Toast.LENGTH_SHORT).show()
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.task_sheet, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)

        val calendarImageView = view.findViewById<ImageView>(R.id.image)
        val watch = view.findViewById<ImageView>(R.id.watch)
        val cancel = view.findViewById<ImageView>(R.id.cancel)
        date = view.findViewById<TextView>(R.id.date)
        val time = view.findViewById<TextView>(R.id.time)
        val title = view.findViewById<EditText>(R.id.text)
        val Done = view.findViewById<TextView>(R.id.done)
        title.setText(title1)
        date.text = date2
        time.text = time1
        calendarImageView.setOnClickListener {

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            showDatePickerDialog(year, month, dayOfMonth)
        }
        watch.setOnClickListener {

            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                requireContext(), { view, hourOfDay, minute ->
                    // on below line we are setting selected
                    formattedTime = String.format(
                        "%02d:%02d %s", hourOfDay % 12, minute, if (hourOfDay < 12) "AM" else "PM"
                    )
                    time.text = formattedTime

                }, hour, minute, false
            )
            // at last we are calling show to
            // display our time picker dialog.
            timePickerDialog.show()
        }
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        Done.setOnClickListener {

            dialog.dismiss()
            val titletext = title.text.toString()
            if (formattedTime.isNotEmpty() && date1.isEmpty()) {
                lifecycleScope.launch(Dispatchers.Main) {
                    TaskDb.getAllTasks(requireContext()).taskDao()
                        .update(Task(id1, titletext, date2, formattedTime))
                    getData()
                }
                scheduleNotification(Task(id1, titletext, date2, formattedTime))
            } else if (formattedTime.isEmpty() && date1.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.Main) {
                    TaskDb.getAllTasks(requireContext()).taskDao()
                        .update(Task(id1, titletext, date1, time1))
                    getData()
                }
                scheduleNotification(Task(id1, titletext, date1, time1))
            } else if (formattedTime.isEmpty() && date1.isEmpty()) {
                lifecycleScope.launch(Dispatchers.Main) {
                    TaskDb.getAllTasks(requireContext()).taskDao()
                        .update(Task(id1, titletext, date2, time1))
                    getData()
                }
                scheduleNotification(Task(id1, titletext, date2, time1))
            }

//                Log.e(TAG, "NO_PERMISSION")
            // Directly ask for the permission

            //   scheduleNotificationsForTasks(list)
            dialog.dismiss()


        }
        dialog.show()

    }

    override fun onTaskClicked(title: String, date: String, time: String, id: Int) {
        bottomsheet(title, date, time, id)
    }

    fun alert() {
        val alert = AlertDialog.Builder(requireContext())
        alert.setMessage("Please enter all the details")
        alert.setCancelable(false)
        alert.setPositiveButton("ok") { dialog, which ->
            dialog.dismiss()
        }

        alert.show()
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted, handle accordingly
                // For example, you can call your method to handle notification here

            } else {
                // Permission denied, handle accordingly
                // For example, display a message to the user
                Toast.makeText(
                    requireContext(), "Notification permission denied", Toast.LENGTH_SHORT
                ).show()
            }
        }

    @SuppressLint("MissingPermission")
    fun scheduleNotification(task: Task) {
        alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (alarmManager == null) {
            Log.e("AlarmManager", "Failed to obtain AlarmManager instance")
        } else {
            Log.d("AlarmManager", "AlarmManager instance obtained successfully")
        }
        val alarmIntent = Intent(requireContext(), AlarmReceiver::class.java)
        alarmIntent.putExtra("title", task.title)
        Log.d("AlarmManager23", "task.title" + task.title)
        alarmIntent.putExtra("id", task.id)// Pass title to the receiver
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            task.id,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val calendar = Calendar.getInstance()
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        dateFormatter.timeZone = TimeZone.getDefault()

        val dateTime = "${task.date} ${task.time}"

        val parsedDateTime = dateFormatter.parse(dateTime)
        parsedDateTime?.let {
            calendar.time = it
        }
        Log.d("AlarmManager23", "task.title" + calendar.timeInMillis)

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Ask the user to grant SCHEDULE_EXACT_ALARM permission
                val intent = Intent(
                    Settings


                        .ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                )
                startActivity(intent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }

        Toast.makeText(requireContext(), "Notification set Successfully", Toast.LENGTH_SHORT).show()
    }

}