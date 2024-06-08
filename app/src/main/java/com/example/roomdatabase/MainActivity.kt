package com.example.roomdatabase

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.roomdatabase.DataBase.NotesDataBase
import com.example.roomdatabase.Model.Notes
import com.example.roomdatabase.Model.Task
import com.example.roomdatabase.adapter.ViewPagerAdapter
import com.example.roomdatabase.fragment1.NotesFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var deleteAll: ImageView

        @SuppressLint("StaticFieldLeak")
        lateinit var search: SearchView
        lateinit var searchBtn: ImageView
    }

    var list: ArrayList<Task> = ArrayList()
    lateinit var context: Context
    private var isRestore: Boolean = false
    lateinit var tabLayout: TabLayout
    private val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewPager = findViewById<ViewPager2>(R.id.Container)
        searchBtn = findViewById<ImageView>(R.id.searchBtn)
        tabLayout = findViewById<TabLayout>(R.id.main)

        search = findViewById(R.id.search)
        deleteAll = findViewById<ImageView>(R.id.deleteAll)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        // Set your custom toolbar as the support action bar
        setSupportActionBar(toolbar)
        val drawable = ContextCompat.getDrawable(this, R.drawable.toolbar_overflow_icon)
        toolbar.overflowIcon = drawable
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Notes"
                1 -> tab.text = "Task"
            }
        }.attach()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // This method will be called when a new page is selected
                // You can perform actions or update UI elements based on the selected page
                if (position == 0) {
                    searchBtn.visibility = View.VISIBLE
                    // The first page (index 0) is selected, show a toast specific to the NotesFragment
                    // Toast.makeText(applicationContext, "Toast from NotesFragment", Toast.LENGTH_SHORT).show()
                } else if (position == 1) {
                    searchBtn.visibility = View.GONE
                }

            }
        })

        searchBtn.setOnClickListener {
            tabLayout.visibility = View.GONE
            search.visibility = View.VISIBLE
            search.isIconified = false
            search.queryHint = "Search"
        }
        context = this
        search.setOnCloseListener {
            tabLayout.visibility = View.VISIBLE
            search.visibility = View.GONE
            false
        }
// Check if the WRITE_EXTERNAL_STORAGE permission is already granted

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Toast.makeText(context, "query" + newText, Toast.LENGTH_SHORT).show()
                if (newText != null) {
                    val fragment =
                        supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment
                    fragment?.search(newText)
                }


                return true
            }

        })
    }

    fun backupDataToStorage(data: List<Notes>): String {
        val gson = Gson()
        return gson.toJson(data)
    }

    fun writeJsonToFile(jsonString: String, fileName: String) {
        val directory: File = if (Build.VERSION.SDK_INT <= 30) {
            File(getFilesDir(), "Arhspreet")
        } else {
            // For Android versions below Q, you may choose a different fallback option
            File(Environment.getExternalStorageDirectory(), "Arhspreet")
        }

        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, fileName)
        try {
            val directory = File(getFilesDir(), "Arhspreet")
            if (directory.exists() && directory.isDirectory) {
                // Directory exists
                Log.d("FilePath", "Directory exists: ${directory.absolutePath}")
            } else {
                // Directory does not exist
                Log.d("FilePath", "Directory does not exist or is not a directory")
            }


            Log.d("arsh", "asrh" + file)
            Toast.makeText(this, "Successfully Backup", Toast.LENGTH_SHORT).show()
            file.writeText(jsonString)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("arsh", "asrh" + e)
        }
    }


    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Environment.isExternalStorageManager()) {
                // Permission granted, proceed with your code
            } else {
                // Permission denied, handle accordingly (e.g., show a message or disable functionality)
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertJsonToNotesList(jsonData: String?): List<Notes>? {
        if (jsonData.isNullOrEmpty()) {
            Log.e("MainActivity", "JSON data is null or empty")
            return null
        }
        val gson = Gson()
        return try {
            gson.fromJson(jsonData, Array<Notes>::class.java).toList()
        } catch (e: JsonSyntaxException) {
            Log.e("MainActivity", "Error parsing JSON data: ${e.message}")
            null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_for_toolbar, menu)
//        val listMenuItem = menu.findItem(R.id.list)
//        updateListMenuItemTitle(listMenuItem)
        val restoreMenuItem = menu.findItem(R.id.Restore)
//        val backup = menu.findItem(R.id.BackUp)
//        restoreMenuItem.isEnabled = isRestore == false
        return true
    }

    private fun updateListMenuItemTitle(item: MenuItem) {
        val viewPager: ViewPager2 = findViewById(R.id.Container)
        val fragment =
            supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment
        if (fragment?.isGridView() == true) {
            item.title = "GridView"
        } else {
            item.title = "ListView"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {

                true
            }

            R.id.deleteAll -> {

                deleteDialog()

                true
            }

            R.id.about -> {

                startActivity(Intent(this, AboutUS::class.java))

                true
            }

            R.id.BackUp -> {
                if (Build.VERSION.SDK_INT >= 31) {
                    if (Environment.isExternalStorageManager()) {
                        // Your code to backup data to storage
                        NotesDataBase.getAllNotes(this).notesDao().getAllNotes()
                            .observe(this) { notesList ->
                                val jsonData = backupDataToStorage(notesList)
                                writeJsonToFile(jsonData, "Arshpreet")
                            }
                        val viewPager: ViewPager2 = findViewById(R.id.Container)

                        val fragment =
                            supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment

                        fragment?.initiateAnotherBackup()

                    } else {
                        requestManageExternalStoragePermission()
                    }
                } else {
                    // Check if the permissions are already granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission is not granted, request it
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            READ_EXTERNAL_STORAGE_REQUEST_CODE
                        )
                    } else {
                        // Permission is already granted, proceed with file read operation
                        NotesDataBase.getAllNotes(this).notesDao().getAllNotes()
                            .observe(this) { notesList ->
                                val jsonData = backupDataToStorage(notesList)
                                writeJsonToFile(jsonData, "Arshpreet")
                            }
                        val viewPager: ViewPager2 = findViewById(R.id.Container)

                        val fragment =
                            supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment

                        fragment?.initiateAnotherBackup()
                    }

                }
                true
            }

            R.id.Restore -> {
                if (Build.VERSION.SDK_INT >= 31) {
                    if (Environment.isExternalStorageManager()) {
                        // Your code to backup data to storage
                        val viewPager: ViewPager2 = findViewById(R.id.Container)

                        val fragment =
                            supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment

                        fragment?.loadNotesDataFromStorage()
//
                        isRestore = true

                    } else {
                        requestManageExternalStoragePermission()

                    }
                } else {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission is not granted, request it
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            READ_EXTERNAL_STORAGE_REQUEST_CODE
                        )
                    } else {
                        // Permission is already granted, proceed with file read operation
                        val viewPager: ViewPager2 = findViewById(R.id.Container)

                        val fragment =
                            supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment

                        fragment?.loadNotesDataFromStorage()
//
                        isRestore = true
                    }
                }


                true
            }

//            R.id.list -> {
//
//                val viewPager: ViewPager2 = findViewById(R.id.Container)
//                val fragment =
//                    supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment
//
//                fragment?.list()
//                fragment?.toggleListView() // Toggle between list view and grid view
//                updateListMenuItemTitle(item)
//                true
//            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun deleteDialog() {
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Are you sure to delete all ?")
        alert.setCancelable(false)
        alert.setPositiveButton("Yes") { dialog, which ->
            val viewPager: ViewPager2 = findViewById(R.id.Container)

            val fragment =
                supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment

            fragment?.perform()
        }
        alert.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        alert.show()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {

        if (NotesFragment.a == 1) {
            startActivity(Intent(this, WelcomeScreen::class.java))
        } else {
            val viewPager: ViewPager2 = findViewById(R.id.Container)
            val fragment =
                supportFragmentManager.findFragmentByTag("f${viewPager.currentItem}") as? NotesFragment

            fragment?.clear()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with file read operation
                } else {
                    // Permission denied, handle accordingly (e.g., show a message)
                }
            }
            // Handle other permission requests if any
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}

// BroadcastReceiver to handle the notification
