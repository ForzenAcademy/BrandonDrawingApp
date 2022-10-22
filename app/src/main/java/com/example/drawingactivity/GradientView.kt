package com.example.drawingactivity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Color.BLACK
import android.graphics.Color.HSVToColor
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.drawingactivity.ColorConstants.hueMax
import kotlinx.coroutines.*

@SuppressLint("ClickableViewAccessibility")
class GradientView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var onSatValChanged: ((Float, Float) -> Unit)? = null
    var onGradientClicked: ((Boolean) -> Unit)? = null
    private var currentHue = hueMax
    private var shape: Rect? = null
    private var lastHsv: Hsv? = null
    private var dx = 0f
    private var dy = 0f

    init {
        setOnTouchListener { _, mEvent ->
            dx = mEvent.x.coerceIn(0f, width * 1f)
            dy = mEvent.y.coerceIn(0f, height * 1f)
            when (mEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    onGradientClicked?.invoke(true)
                    if (dy > height / 2) {
                        selectPaint.color = Color.WHITE
                    } else selectPaint.color = BLACK
                    onSatValChanged?.invoke(dx / width, 1 - (dy / height))
                }
                MotionEvent.ACTION_MOVE -> {
                    if (dy > height / 2) {
                        selectPaint.color = Color.WHITE
                    } else selectPaint.color = BLACK
                    onSatValChanged?.invoke(dx / width, 1 - (dy / height))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onGradientClicked?.invoke(false)
                }
            }
            true
        }
    }

    fun setCurrentHsv(hsv: Hsv) {
        if (width == 0 || height == 0) {
            lastHsv = hsv
        } else {
            currentHue = hsv.hue
            dx = hsv.saturation * width
            dy = hsv.value * height
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        lastHsv.let { hsv ->
            if (hsv != null) {
                setCurrentHsv(hsv)
                lastHsv = null
            }
        }
        canvas?.apply {
            if (shape == null) {
                shape = Rect(0, 0, width, height)
            }
            generateAllBitmaps()
            getBitmap(currentHue.toInt())?.let { bitmap ->
                shape?.let { shape ->
                    drawBitmap(
                        bitmap,
                        null,
                        shape,
                        null
                    )
                }
            }
            drawCircle(dx, height - dy, selectorCircleRadius, selectPaint)
        }
    }

    companion object {
        private const val imageSize: Int = 50
        private const val maxPossibleHue: Int = 360
        private const val selectorCircleRadius: Float = 12f
        private val selectPaint = Paint().apply {
            strokeWidth = 3f
            color = BLACK
            style = Paint.Style.STROKE
        }
        private var bitmaps: Array<Bitmap?>? = Array(maxPossibleHue) { null }
        private var needsToFillArray = true

        @OptIn(DelicateCoroutinesApi::class)
        private fun generateAllBitmaps() {
            if (needsToFillArray) {
                GlobalScope.launch {
                    withContext(Dispatchers.Default) {
                        for (i in 0..90) {
                            bitmaps?.let { bitmaps -> bitmaps[i] = makeBitmap(i.toFloat()) }
                        }
                    }
                }
                GlobalScope.launch {
                    withContext(Dispatchers.Default) {
                        for (i in 91..180) {
                            bitmaps?.let { bitmaps -> bitmaps[i] = makeBitmap(i.toFloat()) }
                        }
                    }
                }
                GlobalScope.launch {
                    withContext(Dispatchers.Default) {
                        for (i in 181..270) {
                            bitmaps?.let { bitmaps -> bitmaps[i] = makeBitmap(i.toFloat()) }
                        }
                    }
                }
                GlobalScope.launch {
                    withContext(Dispatchers.Default) {
                        for (i in 271..359) {
                            bitmaps?.let { bitmaps -> bitmaps[i] = makeBitmap(i.toFloat()) }
                        }
                    }
                }
                needsToFillArray = false
            }
        }

        private fun makeBitmap(hue: Float): Bitmap {
            val step = 1f / imageSize
            var v = 1f
            var s = 0f
            val hsv = floatArrayOf(hue, 0f, 0f)
            var index = 0
            val pixels = IntArray(imageSize * imageSize)

            repeat(imageSize) {
                repeat(imageSize) {
                    hsv[1] = s
                    hsv[2] = v
                    pixels[index] = HSVToColor(hsv)
                    s += step
                    index += 1
                }
                s = 0f
                v -= step
            }
            return Bitmap.createBitmap(pixels, imageSize, imageSize, Bitmap.Config.ARGB_8888)
        }

        private fun getBitmap(index: Int): Bitmap? {
            val offsetIndex = index.coerceIn(0, maxPossibleHue - 1)
            return if (bitmaps?.get(offsetIndex) == null) {
                bitmaps?.set(offsetIndex, makeBitmap(index.toFloat()))
                bitmaps?.get(offsetIndex)
            } else bitmaps?.let { bitmaps -> bitmaps[offsetIndex] }
        }
    }
}