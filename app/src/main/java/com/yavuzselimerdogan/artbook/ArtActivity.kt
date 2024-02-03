package com.yavuzselimerdogan.artbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.yavuzselimerdogan.artbook.databinding.ActivityArtBinding
import com.yavuzselimerdogan.artbook.databinding.ActivityMainBinding

class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    
    }


    fun save (view : View){

    }

    fun selectPhoto (view : View){

    }

}