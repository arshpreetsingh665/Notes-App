package com.example.roomdatabase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.roomdatabase.databinding.ActivityWelcomeScreenBinding
import com.example.roomdatabase.fragment1.NotesFragment

class WelcomeScreen : AppCompatActivity() {
    lateinit var mainBinding:ActivityWelcomeScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       mainBinding= DataBindingUtil.setContentView(this,R.layout.activity_welcome_screen)
        mainBinding.button.setOnClickListener{
         //   NotesFragment.a=0
            startActivity(Intent(this,MainActivity::class.java))
        }

    }
}