package com.example.drawingactivity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.drawingactivity.ColorConstants.hueMax

@SuppressLint("ClickableViewAccessibility")
class HueView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var onHueChanged: ((Float) -> Unit)? = null
    var onSliderClicked: ((Boolean) -> Unit)? = null
    private var hueSlider = hueMax
    private var currentHue: Float? = null
    private var sliderBitmap: Bitmap? = null
    private var dy = hueSlider

    init {
        setOnTouchListener { _, mEvent ->
            dy = ((mEvent.y / height) * hueMax).coerceIn(0f, hueMax)
            when (mEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    onSliderClicked?.invoke(true)
                    onHueChanged?.invoke(dy)
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    onHueChanged?.invoke(dy)
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    onSliderClicked?.invoke(false)
                }
            }
            true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        currentHue.let { hue ->
            if (hue != null) {
                setHue(hue)
                currentHue = null
            }
        }
        canvas?.apply {
            if (sliderBitmap == null) {
                sliderBitmap = makeBitmap()
            }
            sliderBitmap?.let { drawBitmap(it, 0f, 0f, null) }
            drawLine(0f, (dy / hueMax) * height, width * 1f, (dy / hueMax) * height, selectPaint)
        }
    }

    fun setHue(newHue: Float) {
        if (width == 0 || height == 0) {
            currentHue = newHue
        } else {
            dy = newHue
        }
        invalidate()
    }

    private fun makeBitmap(): Bitmap {
        var hue = 0f
        val vStep = 1f / height * hueMax
        val hsv = floatArrayOf(hue, 1f, 1f)
        var index = 0
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)

        repeat(height) {
            hsv[0] = hue
            val color = Color.HSVToColor(hsv)
            repeat(width) {
                pixels[index] = color
                index += 1
            }
            hue += vStep
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    companion object {
        private val selectPaint = Paint().apply {
            strokeWidth = 12f
            color = Color.BLACK
            style = Paint.Style.STROKE
        }
    }
}