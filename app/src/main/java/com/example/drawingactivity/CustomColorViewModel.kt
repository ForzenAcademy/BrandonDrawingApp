package com.example.drawingactivity

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel

/**
 * A viewModel that controls the colors set in multiple views
 * primaryColor sets the primary color for the app
 * ViewStates is used in the lambda onUpdate to update views in whatever activity it is invoked from
 * cyclePrimaryColor is the internal function that sets a new Primary Color by selecting from a list
 */
class CustomColorViewModel : ViewModel() {

    var onUpdate: ((ViewState) -> Unit)? = null
    var onAddLayerOperationDone: ((String?) -> Unit)? = null

    data class ViewState(
        var primaryColor: Int,
        var currentBitmap: Bitmap?,
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

    var currentBitmap: Bitmap? = null

    fun saveUserBitmap(b: Bitmap?) {
        currentBitmap = b
    }

    var layerList = mutableListOf<String>()
    var isDialogOpen = false
    var defaultLayerHintFound = false
    var currentUserEnteredLayerName: String? = null
    var layerNumber: Int = LAYER_COUNT_START

    /**
     * Function that will search for if the default hint is correct or needs to be changed
     */
    fun createLayerHint(defaultLayerName: String) {
        if (layerList.isNotEmpty()) {
            for (i in 0 until layerList.size) {
                if (layerList[i] == defaultLayerName) {
                    layerNumber += 1
                    return
                }
            }
        }
        defaultLayerHintFound = true
    }

    /**
     * Function called to reset layer name variables on closing the dialog intentionally
     */
    fun dialogCompleted() {
        isDialogOpen = false
        currentUserEnteredLayerName = null
        defaultLayerHintFound = false
    }

    /**
     * Adds a string to a list of layer names
     */
    fun addLayer(LayerName: String) {
        if (LayerName.length < NAME_LENGTH_MAX) {
            if (layerList.isNotEmpty()) {
                if (layerList.contains(LayerName)) {
                    onAddLayerOperationDone?.invoke(null)
                    return
                }
            }
            layerList.add(LayerName)
            onAddLayerOperationDone?.invoke(LayerName)
        }
    }

    private fun updateViews() {
        onUpdate?.invoke(
            ViewState(
                primaryColor, currentBitmap
            )
        )
    }

    companion object InputLimitations {
        const val NAME_LENGTH_MAX = 16
        const val LAYER_COUNT_START = 1
        const val NAME_EMPTY = ""
    }
}