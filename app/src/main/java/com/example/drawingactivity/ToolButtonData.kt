package com.example.drawingactivity

import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible

/**
 * This Data class consists of a view, a ToolType, and a LinearLayout.
 * It contains functions to show and hide layouts based on what view is contained.
 */
data class ToolButtonData(val view: Int, val layout: Int) {

    /**
     * This function hides all layouts and animates in the newly picked one to be visible.
     */
    fun showView(currentView: View, toolList: Map<ToolType, ToolButtonData>) {
        toolList.forEach {
            currentView.findViewById<View>(it.value.view).background = null
            currentView.findViewById<View>(it.value.layout).apply {
                isVisible = false
                clearAnimation()
            }
        }
        currentView.findViewById<View>(view).background =
            AppCompatResources.getDrawable(
                currentView.context,
                R.drawable.selected_button_background
            )
        currentView.findViewById<View>(layout).isVisible = true
    }

    fun fadeInView(currentView: View, view: Int) {
        currentView.findViewById<View>(view).startAnimation(
            AnimationUtils.loadAnimation(
                currentView.context,
                R.anim.fadein
            )
        )
    }
}
