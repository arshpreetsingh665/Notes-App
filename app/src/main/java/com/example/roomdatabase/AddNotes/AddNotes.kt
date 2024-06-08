package com.example.roomdatabase.AddNotes

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.roomdatabase.DataBase.NotesDataBase
import com.example.roomdatabase.MainActivity
import com.example.roomdatabase.Model.Notes
import com.example.roomdatabase.R
import com.example.roomdatabase.databinding.ActivityAddNotesBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime


class AddNotes : AppCompatActivity() {
    lateinit var mBinding: ActivityAddNotesBinding
    private val PICK_IMAGE_CAMERA = 101
    
    private val PICK_IMAGE_GALLERY = 102

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_notes)
        val title2 = intent.getStringExtra("title")
        val description2 = intent.getStringExtra("description")
        val id = intent.getLongExtra("id", -1)
        mBinding.image.setOnClickListener(View.OnClickListener {
            Toast.makeText(this, "mssg", Toast.LENGTH_SHORT).show()
            dialog()

        })
        // Toast.makeText(this, "msgg" + title2, Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "msgg" + id, Toast.LENGTH_SHORT).show()
        //   Toast.makeText(this, "msgg" + description2, Toast.LENGTH_SHORT).show()
        val done = findViewById<ImageView>(R.id.done)
        val share = findViewById<ImageView>(R.id.share)
        val title = findViewById<EditText>(R.id.title)
        val description = findViewById<EditText>(R.id.description)
        if (id == -1L) {
            share.visibility = View.GONE
            title.setText("")
            description.setText("")

        } else {
            share.visibility = View.VISIBLE
            title.setText(title2.toString())
            description.setText(description2.toString())
        }

        val notesDao = NotesDataBase.getAllNotes(this).notesDao()
        share.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet, null)
            dialog.setContentView(view)

            val btnClose = view.findViewById<TextView>(R.id.text)
            btnClose.setOnClickListener {
                val titleText = title.text.toString()
                val descriptionText = description.text.toString()
                val shareText = "$titleText\n$descriptionText"

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }

                startActivity(Intent.createChooser(shareIntent, "Share via"))
                dialog.dismiss()
            }
            val imageShare = view.findViewById<TextView>(R.id.image)
            imageShare.setOnClickListener {
                val title = title.text.toString()
                val description = description.text.toString()
                shareTextAsImage(title, description, this)
            }
            dialog.show()
        }


        done.setOnClickListener {
            val date = LocalDateTime.now() ?: throw IllegalStateException("Date is null")
            val title1 = title.text.toString()
            val description1 = description.text.toString()


            if (id == -1L) {

                val singleNoteList = listOf(Notes(null, title1, description1, date))

                lifecycleScope.launch {

                    notesDao.insertNotes(singleNoteList.toTypedArray())

                }
            } else {


                lifecycleScope.launch {
                    notesDao.update(Notes(id, title1, description1, date))
                }
            }

            val Intent = Intent(this, MainActivity::class.java)
            startActivity(Intent)
            finish()
        }
    }

    private fun pickImageFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, PICK_IMAGE_CAMERA)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_GALLERY)
    }

    private fun dialog() {
        Toast.makeText(this, "mssg", Toast.LENGTH_SHORT).show()
        val dialogView = LayoutInflater.from(this).inflate(R.layout.imagepicker, null)
        val dialog = AlertDialog.Builder(this)
        dialog.setView(dialogView)
        dialog.create()
        dialog.setCancelable(true)
        val camera = dialogView.findViewById<LinearLayout>(R.id.line1)
        val gallery = dialogView.findViewById<LinearLayout>(R.id.line2)
        camera.setOnClickListener {
            pickImageFromCamera()

        }
        gallery.setOnClickListener {
            pickImageFromGallery()
        }
        dialog.show()

    }


    fun combineTextAsBitmap(title: String, description: String): Bitmap {
        // Calculate dimensions for the combined bitmap
        val maxWidth = 500 // Example: maximum width of the combined bitmap
        val titleTextSize = 40f
        val descriptionTextSize = 25f
        val padding = 20

        // Create Paint objects for title and description
        val titlePaint = Paint().apply {
            textSize = titleTextSize
            color = Color.BLACK
        }
        val descriptionPaint = Paint().apply {
            textSize = descriptionTextSize
            color = Color.BLACK
        }

        // Measure text dimensions
        val titleBounds = Rect()
        titlePaint.getTextBounds(title, 0, title.length, titleBounds)
        val descriptionBounds = Rect()
        descriptionPaint.getTextBounds(description, 0, description.length, descriptionBounds)

        // Calculate bitmap dimensions
        val bitmapWidth = maxWidth
        val bitmapHeight = titleBounds.height() + descriptionBounds.height() + padding * 3

        // Create bitmap with calculated dimensions
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)

        // Draw title and description onto the bitmap
        val canvas = Canvas(bitmap)
        val titleX = (bitmapWidth - titleBounds.width()) / 2f
        val titleY = titleBounds.height() + padding
        canvas.drawText(title, titleX, titleY.toFloat(), titlePaint)
        val descriptionX = (bitmapWidth - descriptionBounds.width()) / 2f
        val descriptionY = titleY + descriptionBounds.height() + padding * 2
        canvas.drawText(description, descriptionX, descriptionY.toFloat(), descriptionPaint)

        return bitmap
    }


    fun shareTextAsImage(title: String, description: String, context: Context) {
        // Convert title and description to a single bitmap image
        val bitmap = combineTextAsBitmap(title, description)

        // Save bitmap to a temporary file
        val imageFile = saveBitmapToFile(bitmap, context)

        // Generate content URI using FileProvider
        val contentUri =
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)

        // Create an intent to share the image file
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Start the activity for sharing
        context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }

    fun saveBitmapToFile(bitmap: Bitmap, context: Context): File {
        // Save bitmap to a temporary file
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs() // Make sure the directory exists
        val imageFile = File(cachePath, "image.png")
        val stream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        return imageFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_CAMERA) {
                val bitmap = data?.extras?.get("data") as Bitmap?
                insertImageIntoEditText(bitmap)
            } else if (requestCode == PICK_IMAGE_GALLERY) {
                val selectedImage = data?.data
                try {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                    insertImageIntoEditText(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun insertImageIntoEditText(bitmap: Bitmap?) {
        bitmap?.let {
            val descriptionEditText = findViewById<EditText>(R.id.description)
            val imageSpan = ImageSpan(this, bitmap)

            val builder = SpannableStringBuilder(descriptionEditText.text)
            val selectionStart = descriptionEditText.selectionStart

            // Insert a newline before the image if the cursor is not at the beginning of the text
            if (selectionStart != 0) {
                builder.insert(selectionStart, "\n")
            }

            // Insert the image at the current cursor position
            builder.insert(selectionStart, " ")
            builder.setSpan(
                imageSpan,
                selectionStart,
                selectionStart + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Set the updated text in the EditText and move the cursor after the inserted image
            descriptionEditText.text = builder
            descriptionEditText.setSelection(selectionStart + 1)
        }
    }

}