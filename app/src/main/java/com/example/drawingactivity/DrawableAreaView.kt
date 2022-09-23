package com.example.drawingactivity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * This view is the drawable area of the app.
 * drawing on this location will draw to a bitmap which is loaded
 * onBitmapDrawn saves the bitmap to a different location as needed
 * attemptBitmapUpdate will update the bitmap to another one if provided to it
 * At the start of onDraw, if the bitmap is null, it looks to update to a bitmap
 * if no bitmap is found, it will generate a blank one
 * After being interacted with, it will save the current bitmap
 */
@SuppressLint("ClickableViewAccessibility")
class DrawableAreaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    /**
     * This lambda takes in a bitmap and returns nothing
     * This is being used to save the bitmap to a different location
     */
    var onBitmapDrawn: ((Bitmap?) -> Unit)? = null
    var userDrawnBitmap: Bitmap? = null

    var pathPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = STROKEWIDTH
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private var path: Path? = null

    init {
        setOnTouchListener { _, mEvent ->
            val intermediaryCanvas = userDrawnBitmap?.let { Canvas(it) }
            when (mEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    path = Path()
                    path?.moveTo(mEvent.x, mEvent.y)
                }
            }
            path?.lineTo(mEvent.x, mEvent.y)
            path?.let { path -> intermediaryCanvas?.drawPath(path, pathPaint) }
            invalidate()
            true
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawColor(Color.LTGRAY)
            if (userDrawnBitmap == null) {
                userDrawnBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }
            userDrawnBitmap?.let { bitmap -> drawBitmap(bitmap, 0f, 0f, null) }
            onBitmapDrawn?.invoke(userDrawnBitmap)
        }
    }

    fun setBitmap(b: Bitmap) {
        userDrawnBitmap = b
    }

    companion object DrawingWidgetValues {
        const val STROKEWIDTH = 15f
    }
}