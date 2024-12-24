package com.example.final_citra_digital.user

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.final_citra_digital.R

class Model : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set the back button drawable
        val backDrawable = resources.getDrawable(R.drawable.back, null)
        toolbar.setNavigationIcon(backDrawable)

        // Set the navigation click listener
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}