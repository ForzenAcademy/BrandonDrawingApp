package com.example.drawingactivity

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText

@SuppressLint("ClickableViewAccessibility")
class ForcableEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : EditText(context, attrs, defStyle) {

    override fun addTextChangedListener(watcher: TextWatcher?) {
        watcher?.let { if (!textListeners.contains(it)) textListeners.add(it) }
        super.addTextChangedListener(watcher)
    }

    private val textListeners = mutableListOf<TextWatcher>()

    private fun removeAllListeners() {
        textListeners.forEach { removeTextChangedListener(it) }
    }

    private fun addBackAllListeners() {
        textListeners.forEach { addTextChangedListener(it) }
    }

    fun forceText(text: String) {
        if (!this.hasFocus()) {
            removeAllListeners()
            setText(text)
            addBackAllListeners()
        }
    }

    /**
     * Checks to confirm if the string input is an int or not.
     * If it is an int, converts it to an int.
     */
    val intValueOrNull: Int?
        get() {
            return this.text.toString().toIntOrNull()
        }

    val intValue: Int
        get() {
            return this.text.toString().toInt()
        }

    val floatValue: Float
        get() {
            return this.text.toString().toFloat()
        }
}