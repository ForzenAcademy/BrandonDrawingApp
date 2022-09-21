package com.example.drawingactivity

import android.graphics.Color

/**
 * A viewModel that controls the colors set in multiple views
 * primaryColor sets the primary color for the app
 * ViewStates is used in the lambda onUpdate to update views in whatever activity it is invoked from
 * cyclePrimaryColor is the internal function that sets a new Primary Color by selecting from a list
 */
class CustomColorViewModel(

) {
    var onUpdate: ((ViewState) -> Unit)? = null

    data class ViewState(
        var primaryColor: Int,
    )

    private var primaryColors = listOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE)
    private var primaryColor = primaryColors[0]
    private var currentColor: Int = 0

    fun cyclePrimaryColor() {
        if (currentColor < primaryColors.size - 1) {
            currentColor += 1
        } else currentColor = 0
        this.primaryColor = primaryColors[currentColor]
        updateViews()
    }

    private fun updateViews() {
        onUpdate?.invoke(
            ViewState(
                primaryColor
            )
        )
    }
}