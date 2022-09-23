package com.example.drawingactivity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private val viewModel: CustomColorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawableArea = findViewById<DrawableAreaView>(R.id.drawableArea)
        drawableArea.onBitmapDrawn = { bitmap -> viewModel.saveUserBitmap(bitmap) }
        viewModel.currentBitmap?.let { drawableArea.setBitmap(it) }
        val colorSelector = findViewById<PrimaryColorCircleView>(R.id.colorCircle)
        colorSelector.onChangeColor = { viewModel.cyclePrimaryColor() }

        viewModel.onUpdate = {
            colorSelector.circlePrimaryColor = it.primaryColor
            drawableArea.pathPaint.color = it.primaryColor
            drawableArea.userDrawnBitmap = it.currentBitmap
        }
    }
}