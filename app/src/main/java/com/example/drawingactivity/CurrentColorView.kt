package com.example.drawingactivity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * A custom view that is a circle filled with a color
 * This color reflects what the current primary color of the app is
 * When clicked, this circle will redraw itself with a new color and call to change the primary color of the app
 * has a companion object that allows adjustment of the radius of the circle
 * onChangeColor is the lambda that is called on click to change the primary color that is set
 * circlePrimaryColor is how the color of the circle is adjusted
 */
@SuppressLint("ClickableViewAccessibility")
class CurrentColorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    /**
     * This lambda calls for an update to the color
     */
    var onChangeColor: (() -> Unit)? = null
    var circlePrimaryColor = Color.RED

    private var circlePaint = Paint().apply {
        color = circlePrimaryColor
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            circlePaint.color = circlePrimaryColor
            drawCircle(width / 2f, height / 2f, RADIUS, circlePaint)
            drawCircle(width / 2f, height / 2f, RADIUS, borderPaint)
        }
    }

    init {
        setOnClickListener {
            onChangeColor?.invoke()
            invalidate()
        }
    }

    companion object CircleMeasurements {
        const val RADIUS = 36f
    }
}