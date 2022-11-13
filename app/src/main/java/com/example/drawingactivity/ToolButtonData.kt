package com.example.drawingactivity

import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible

/**
 * This Data class consists of a view, and a LinearLayout.
 * It contains functions to show and hide layouts based on what view is contained.
 */
data class ToolButtonData(val view: View, val layout: View) {

    /**
     * This function hides all layouts and animates in the newly picked one to be visible.
     */
    fun showView(currentView: LinearLayout, toolList: Map<ToolType, ToolButtonData>) {
        toolList.forEach {
            it.value.view.background = null
            it.value.layout.apply {
                isVisible = false
                clearAnimation()
            }
        }
        view.background =
            AppCompatResources.getDrawable(
                currentView.context,
                R.drawable.selected_button_background
            )
        layout.isVisible = true
    }

    fun fadeInView(currentView: LinearLayout, view: View) {
        view.startAnimation(
            AnimationUtils.loadAnimation(
                currentView.context,
                R.anim.fadein
            )
        )
    }
}
