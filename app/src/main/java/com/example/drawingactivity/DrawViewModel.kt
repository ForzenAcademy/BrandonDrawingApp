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
class DrawViewModel : ViewModel() {

    private var primaryColors = listOf(Color.BLACK, Color.RED, Color.GREEN, Color.BLUE)
    private var primaryColor = primaryColors[0]
    private var currentColor: Int = 0
    private var currentBitmap: Bitmap? = null
    private var layerList = mutableListOf<String>()
    private var currentNewLayerName: String? = null
    private var editLayerIndex: Int = 0
    private var isDialogOpen = false
    private var isBottomSheetOpen = false
    private var isColorPickerOpen = false
    private var previousHSVColor: Hsv = Hsv(0f, 0f, 0f)

    /**
     * Invoked when a view requests an update from the viewModel.
     * This returns the data class ViewState with all the variables the viewModel is holding.
     */
    var onUpdate: ((ViewState) -> Unit)? = null

    /**
     * Invoked when a Layer is added to the List
     * Takes in a String and returns nothing
     * This informs the user on the result of adding a String to the list
     */
    var onAddLayerComplete: ((String?) -> Unit)? = null

    /**
     * Invoked when a Layer name is changed the List
     */
    var onEditLayerComplete: ((String?, String?) -> Unit)? = null

    /**
     * Invoked when a Layer is removed from the List
     * Confirms that the delete was a success
     */
    var onDeleteOperationComplete: ((String?) -> Unit)? = null

    data class ViewState(
        val primaryColor: Int,
        val currentBitmap: Bitmap?,
        val layerList: MutableList<String>,
        val currentNewLayerName: String?,
        val editLayerIndex: Int,
        val isDialogOpen: Boolean,
        val isLayerSheetOpen: Boolean,
        val isColorPickerSheetOpen: Boolean,
        val hsvArray: Hsv,
    )

    /**
     * Changes the primary color used to paint to a view
     */
    fun cycleCurrentColor() {
        if (currentColor < primaryColors.size - 1) {
            currentColor += 1
        } else currentColor = 0
        this.primaryColor = primaryColors[currentColor]
        updateViewStates()
    }

    /**
     * Updates the viewModel with the bitmap provided
     */
    fun setCurrentBitmapState(bitmap: Bitmap?) {
        currentBitmap = bitmap
        updateViewStates()
    }

    /**
     * Updates the viewModel with the status of open add dialog
     */
    fun setDialogState() {
        isDialogOpen = true
        updateViewStates()
    }

    /**
     * Updates the viewModel with the status of the layer list bottom sheet
     */
    fun setLayerListViewState() {
        isBottomSheetOpen = true
        updateViewStates()
    }

    fun setColorPickerViewState() {
        isColorPickerOpen = true
        updateViewStates()
    }

    /**
     * Updates the viewModel with the string being input by the user
     */
    fun setCurrentText(text: String?) {
        currentNewLayerName = text
        updateViewStates()
    }

    /**
     * Updates the viewModel with the index of the edit, if one is occurring
     */
    fun setEditIndex(index: Int?) {
        if (index != null) {
            editLayerIndex = index
        }
    }

    /**
     * Updates the viewModel with the current color of the color picker
     */
    fun setCurrentColor(Hsv: Hsv) {
        previousHSVColor = Hsv
    }

    fun isLayerListEmpty(): Boolean {
        return layerList.isEmpty()
    }

    /**
     * Adds a String to a List of Strings
     */
    fun addLayer(layerName: String) {
        layerList.add(layerName)
        onAddLayerComplete?.invoke(layerName)
        isDialogOpen = false
        updateViewStates()
    }

    /**
     * Removes a layer from the list of layers
     */
    fun deleteLayer(index: Int) {
        if (layerList.size == 0) {
            onDeleteOperationComplete?.invoke(null)
        } else {
            val layerName = layerList[index]
            layerList.removeAt(index)
            onDeleteOperationComplete?.invoke(layerName)
            updateViewStates()
        }
    }

    /**
     * Edits the name of the layer
     */
    fun editLayer(newLayerName: String, index: Int) {
        val oldName = layerList[index]
        layerList[index] = newLayerName
        onEditLayerComplete?.invoke(oldName, newLayerName)
        updateViewStates()
    }

    /**
     * Function called to reset layer name variables on closing the dialog intentionally
     */
    fun dialogCompleted() {
        isBottomSheetOpen = false
        isDialogOpen = false
        isColorPickerOpen = false
        currentNewLayerName = null
        editLayerIndex = 0
        updateViewStates()
    }

    private fun updateViewStates() {
        onUpdate?.invoke(
            ViewState(
                primaryColor,
                currentBitmap,
                layerList,
                currentNewLayerName,
                editLayerIndex,
                isDialogOpen,
                isBottomSheetOpen,
                isColorPickerOpen,
                previousHSVColor
            ),
        )
    }
}