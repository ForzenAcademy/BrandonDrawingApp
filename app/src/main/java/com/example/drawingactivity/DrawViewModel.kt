package com.example.drawingactivity

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel

/**
 * A viewModel that controls the colors set in multiple views
 * primaryColor sets the primary color for the app
 * ViewStates is used in the lambda onUpdate to update views in whatever activity it is invoked from
 * cyclePrimaryColor is the internal function that sets a new Primary Color by selecting from a list
 */
class DrawViewModel : ViewModel() {

    private var currentBitmap: Bitmap? = null
    private var layerList = mutableListOf<LayerViewModel>()
    private var currentLayerName: String? = null
    private var currentModel: LayerViewModel? = null
    private var isAddOrEditDialogOpen = false
    private var isDeleteDialogOpen = false
    private var isBottomSheetOpen = false
    private var isColorPickerOpen = false
    private var currentTool: ToolButtonData =
        ToolButtonData(R.id.pickerButton, R.id.colorSelectorLayout)
    private var previousHSVColor: Hsv = Hsv()

    /**
     * Invoked when a view requests an update from the viewModel.
     * This returns the data class ViewState with all the variables the viewModel is holding.
     */
    var onUpdate: ((ViewState) -> Unit)? = null

    /**
     * Invoked when a String is added to the List.
     * Takes in a String and returns nothing.
     * This informs the user on the result of adding a String to the list.
     */
    var onAddLayer: ((String?) -> Unit)? = null

    /**
     * Invoked when a String is changed the List.
     * Confirms that the String in the list has correctly Changed.
     */
    var onEditLayer: ((String?, String?) -> Unit)? = null

    /**
     * Invoked when a String is removed from the List.
     * Confirms that the delete was a success.
     */
    var onDeleteLayer: ((String?) -> Unit)? = null

    /**
     * Invoked when a String is removed from the List.
     * Confirms that the delete was a success.
     */
    var onConfirmColor: ((Int?) -> Unit)? = null

    data class ViewState(
        val currentBitmap: Bitmap?,
        val layerList: MutableList<LayerViewModel>,
        val currentNewLayerName: String?,
        val targetModel: LayerViewModel?,
        val isDialogOpen: Boolean,
        val isDeleteDialogOpen: Boolean,
        val isLayerListViewSheetOpen: Boolean,
        val isColorPickerSheetOpen: Boolean,
        val currentHsv: Hsv,
        val activeTool: ToolButtonData,
    )

    /**
     * Updates the viewModel with the bitmap provided.
     */
    fun setCurrentBitmapState(bitmap: Bitmap?) {
        currentBitmap = bitmap
        updateViewStates()
    }

    fun toolIconClicked(tool: ToolButtonData) {
        currentTool = tool
    }

    /**
     * Alerts viewModel that the edit dialog has been opened.
     */
    fun addOrEditClicked() {
        isAddOrEditDialogOpen = true
        updateViewStates()
    }

    /**
     * Alerts viewModel that the delete dialog has been opened.
     * If the size of the list is 1, it will not open the Delete Dialog.
     */
    fun deleteClicked() {
        if (layerList.size == 1) {
            onDeleteLayer?.invoke(null)
        } else isDeleteDialogOpen = true
        updateViewStates()
    }

    /**
     * Alerts viewModel that the bottomSheet has been opened.
     */
    fun bottomSheetOpened() {
        isBottomSheetOpen = true
        updateViewStates()
    }

    /**
     * Updates the viewModel with the string being input by the user
     */
    fun setCurrentText(text: String?) {
        currentLayerName = text
    }

    /**
     * Updates the viewModel with the index of the edit.
     */
    fun setCurrentModel(model: LayerViewModel) {
        currentModel = model
    }

    /**
     * Updates the viewModel with the current color of the color picker
     */
    fun userUpdatesColor(Hsv: Hsv) {
        previousHSVColor = Hsv
    }

    /**
     * Invokes a lambda do convert the current HSV array stored in the VM to a color int for use.
     */
    fun userConfirmsColor() {
        onConfirmColor?.invoke(previousHSVColor.toColorInt())
    }

    /**
     * Adds a Layer to the List.
     */
    fun addLayer(model: LayerViewModel) {
        if (model.layerName != null) {
            if (layerList.size != 0) {
                onAddLayer?.invoke(model.layerName)
            }
            layerList.add(model)
        }
        currentLayerName = null
        isAddOrEditDialogOpen = false
        updateViewStates()
    }

    /**
     * Removes a Layer from the list.
     */
    fun deleteLayer(model: LayerViewModel, isConfirmed: Boolean) {
        if (isConfirmed) {
            if (layerList.size == 1) {
                onDeleteLayer?.invoke(null)
            } else {
                onDeleteLayer?.invoke(model.layerName)
                layerList.remove(currentModel)
            }
        }
        isDeleteDialogOpen = false
        currentModel = null
        updateViewStates()
    }

    /**
     * Edits a Layer in the list.
     * Takes in the new String and the Index of the edit.
     */
    fun editLayer(newModel: LayerViewModel, previousModel: LayerViewModel) {
        if (newModel.layerName != null) {
            val index = layerList.indexOf(previousModel)
            layerList[index] = newModel
            onEditLayer?.invoke(previousModel.layerName, newModel.layerName)
        }
        currentLayerName = null
        isAddOrEditDialogOpen = false
        currentModel = null
        updateViewStates()
    }

    /**
     * Function called to set the state of the BottomSheet
     */
    fun bottomSheetDone() {
        isBottomSheetOpen = false
    }

    fun initialize() {
        updateViewStates()
    }

    private fun updateViewStates() {
        onUpdate?.invoke(
            ViewState(
                currentBitmap,
                layerList,
                currentLayerName,
                currentModel,
                isAddOrEditDialogOpen,
                isDeleteDialogOpen,
                isBottomSheetOpen,
                isColorPickerOpen,
                previousHSVColor,
                currentTool,
            ),
        )
    }
}

enum class ToolType {
    GRADIENT, BRUSH, MOVE, RESIZE, FILTER, LAYERS
}