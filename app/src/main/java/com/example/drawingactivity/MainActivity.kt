package com.example.drawingactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val viewModel = CustomColorViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val colorSelector = findViewById<PrimaryColorCircleView>(R.id.colorCircle)
        colorSelector.onChangeColor = { viewModel.cyclePrimaryColor() }

        viewModel.onUpdate = {
            colorSelector.circlePrimaryColor = it.primaryColor
        }
    }
}