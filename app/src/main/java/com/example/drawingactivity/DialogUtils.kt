package com.example.drawingactivity

import android.content.Context
import android.graphics.Color
import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drawingactivity.Hsv.Companion.calculateHex
import com.example.drawingactivity.InputLimitations.CHAR_LIMIT
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object DialogUtils {

    fun showDialog(
        context: Context,
        layerList: List<String>,
        previousInput: String?,
        /**
         * Called whenever text is changed in the editable field.
         * This uses a string and returns nothing.
         */
        onEditText: ((String) -> Unit),
        /**
         * This is Called when there is no Error and the Positive Button is pressed on the Dialog.
         * This adds a Layer to a list.
         */
        onSubmit: ((String?) -> Unit),
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.add_layer_dialog, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(view)
        val errorView = view.findViewById<TextView>(R.id.errorText)
        val textField = view.findViewById<EditText>(R.id.layerNameField)
        textField.hint = createLayerHint(context, layerList)
        if (previousInput != null) {
            textField.setText(previousInput)
            val errorStatus = checkForErrors(context, layerList, previousInput.toString().trim())
            if (errorStatus != null) {
                errorView.text = errorStatus
                errorView.isVisible = true
            } else errorView.isVisible = false
        }
        builder.apply {
            setPositiveButton(context.getString(R.string.addLayerPositiveButton)) { _, _ ->
                when (textField.text.toString().trim()) {
                    "", textField.hint.toString() -> {
                        onSubmit.invoke(textField.hint.toString())
                    }
                    else -> {
                        onSubmit.invoke(textField.text.toString().trim())
                    }
                }
                onSubmit.invoke(null)
            }
            setNegativeButton(context.getString(R.string.addLayerNegativeButton)) { _, _ ->
                onSubmit.invoke(null)
            }
            setOnCancelListener {
                onSubmit.invoke(null)
            }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
        val submitButton: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        textField.addTextChangedListener {
            onEditText.invoke(it.toString().trim())
            val errorStatus = checkForErrors(context, layerList, it.toString().trim())
            if (errorStatus != null) {
                errorView.text = errorStatus
                errorView.isVisible = true
                submitButton.isEnabled = false
            } else {
                errorView.isVisible = false
                submitButton.isEnabled = true
            }
        }
    }

    fun layerListBottomSheet(
        context: Context,
        layerList: List<String>,
        /**
         * Opens a Dialog to Delete an object in the list.
         */
        onDelete: ((Int) -> Unit)?,
        /**
         * Opens a Dialog to Edit an object in the list.
         */
        onEdit: ((Int?) -> Unit)?,
        /**
         * Called when the Dialog Completes through any listener.
         * This will reset all values to null in preparation for the next opening of the Dialog.
         */
        onDialogComplete: (() -> Unit)?,
    ): ((Int, String?, DialogOperation) -> Unit) {
        val sheet = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_layer_list_layout, null)
        val data = layerList.map { LayerViewModel(it) }.toMutableList()
        val recyclerView = view.findViewById<RecyclerView>(R.id.listOfLayers)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RecyclerViewAdapter(data = data,
                onDeleteLayer = { onDelete?.invoke(it) },
                onEditName = { onEdit?.invoke(it) })
        }
        sheet.apply {
            setOnDismissListener {
                dismiss()
                onDialogComplete?.invoke()
            }
            setContentView(view)
            show()
        }
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        return { index, newName, operation ->
            when (operation) {
                DialogOperation.EDIT -> {
                    data[index] = LayerViewModel(layerName = newName)
                    recyclerView.adapter?.notifyItemChanged(index, newName)
                }
                DialogOperation.DELETE -> {
                    data.removeAt(index)
                    recyclerView.adapter?.notifyItemChanged(index)
                }
            }
        }
    }

    fun colorPickerBottomSheet(
        context: Context,
        lastHsv: Hsv?,
        /**
         * Lambda that provides the string of the current color.
         * Returns Nothing. Updates the Color in the viewmodel
         */
        onColorAdjust: ((Hsv?) -> Unit),
        /**
         * Lambda that provides the string of the current color.
         * Returns nothing. Used to confirm the use of the color.
         */
        onSubmit: ((Hsv?) -> Unit)
    ) {
        val sheet = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.color_dialog, null)
        val huePicker = view.findViewById<HueView>(R.id.huePicker)
        val gradientPicker = view.findViewById<GradientView>(R.id.gradientPicker)
        val hexText = view.findViewById<ForcableEditText>(R.id.hexText)
        val hueText = view.findViewById<ForcableEditText>(R.id.hueText)
        val satText = view.findViewById<ForcableEditText>(R.id.satText)
        val valText = view.findViewById<ForcableEditText>(R.id.valText)
        val redText = view.findViewById<ForcableEditText>(R.id.redText)
        val greenText = view.findViewById<ForcableEditText>(R.id.greenText)
        val blueText = view.findViewById<ForcableEditText>(R.id.blueText)
        val topColorSquare = view.findViewById<View>(R.id.topColor)
        val bottomColorSquare = view.findViewById<View>(R.id.bottomColor)
        val confirmButton = view.findViewById<Button>(R.id.okButton)
        val allText = listOf(hexText, hueText, satText, valText, redText, greenText, blueText)
        confirmButton.setOnClickListener {
            if (hueText.intValueOrNull != null && satText.intValueOrNull != null && valText.intValueOrNull != null) {
                onSubmit.invoke(
                    Hsv(
                        hueText.floatValue,
                        satText.floatValue / 100,
                        valText.floatValue / 100
                    )
                )
                sheet.dismiss()
            }
        }
        val cancelButton = view.findViewById<Button>(R.id.cancelButton)
        cancelButton.setOnClickListener {
            onSubmit.invoke(null)
            sheet.dismiss()
        }
        sheet.setOnCancelListener {
            onSubmit.invoke(null)
            sheet.dismiss()
        }
        /**
         * Lambda that updates all the views in the gradient.
         * Takes in a color state data class to update views.
         */
        val forceViewUpdates: ((Hsv) -> Unit) = { hsv ->
            val rgb = Hsv.toColor(hsv)
            val hex = calculateHex(rgb as IntArray)
            hexText.forceText(hex)
            hueText.forceText(hsv.hue.toInt().toString())
            satText.forceText((hsv.saturation * 100).toInt().toString())
            valText.forceText((hsv.value * 100).toInt().toString())
            redText.forceText(rgb[0].toString())
            greenText.forceText(rgb[1].toString())
            blueText.forceText(rgb[2].toString())
            huePicker.setHue(hsv.hue)
            gradientPicker.setHue(hsv.hue)
            gradientPicker.setSaturationAndValue(hsv.saturation, hsv.value)
            topColorSquare.setBackgroundColor(parseColor(hex))
        }
        if (lastHsv != null) {
            val rgb = Hsv.toColor(lastHsv)
            val hex = calculateHex(rgb as IntArray)
            bottomColorSquare.setBackgroundColor(parseColor(hex))
            forceViewUpdates.invoke(lastHsv)
            gradientPicker.setPicker(lastHsv.saturation, lastHsv.value)
        }
        listOf(redText, greenText, blueText).forEach {
            it.addTextChangedListener {
                if (redText.intValueOrNull != null && greenText.intValueOrNull != null && blueText.intValueOrNull != null) {
                    val hsv = Hsv.toColor(
                        intArrayOf(
                            redText.intValue,
                            greenText.intValue,
                            blueText.intValue
                        )
                    )
                    forceViewUpdates.invoke(hsv as Hsv)
                    onColorAdjust.invoke(hsv)
                }
            }
        }
        listOf(hueText, satText, valText).forEach {
            it.addTextChangedListener {
                if (hueText.intValueOrNull != null && satText.intValueOrNull != null && valText.intValueOrNull != null) {
                    val hsv =
                        Hsv(hueText.floatValue, satText.floatValue / 100, valText.floatValue / 100)
                    forceViewUpdates.invoke(hsv)
                    onColorAdjust.invoke(hsv)
                }
            }
        }
        huePicker?.apply {
            onHueChanged = { hue ->
                if (satText.intValueOrNull != null && valText.intValueOrNull != null) {
                    val hsv = Hsv(hue, satText.floatValue / 100, valText.floatValue / 100)
                    clearAllFocus(allText)
                    forceViewUpdates.invoke(hsv)
                    onColorAdjust.invoke(hsv)
                }
                onSliderClicked = { viewClicked ->
                    sheet.behavior.isDraggable = !viewClicked
                }
            }
        }
        gradientPicker?.apply {
            onSatValChanged = { saturation, value ->
                if (hueText.intValueOrNull != null) {
                    val hsv = Hsv(hueText.floatValue, saturation, value)
                    clearAllFocus(allText)
                    forceViewUpdates.invoke(hsv)
                    onColorAdjust.invoke(hsv)
                }
            }
            onGradientClicked = { viewClicked ->
                sheet.behavior.isDraggable = !viewClicked
            }
        }
        hexText.addTextChangedListener {
            if (ColorUtils.isHexadecimal(it.toString())) {
                val color = parseColor(it.toString())
                val hsv = Hsv.toColor(
                    intArrayOf(
                        Color.red(color), Color.green(color), Color.blue(color)
                    )
                )
                forceViewUpdates.invoke(hsv as Hsv)
                onColorAdjust.invoke(hsv)
            }
        }
        sheet.setContentView(view)
        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheet.show()
    }
}


/**
 * Function that will search for if the default hint is correct or needs to be changed.
 */
private fun createLayerHint(context: Context, layerList: List<String>): String {
    var hintIndex = 1
    var currentHint = context.getString(R.string.layerHint, hintIndex)
    while (layerList.contains(currentHint)) {
        hintIndex += 1
        currentHint = context.getString(R.string.layerHint, hintIndex)
    }
    return currentHint
}

/**
 * Function that will return a Pair of a String and a Boolean.
 * If there is a match for the String in the list, or if the String is too long, it will return an error message and true.
 * If there is no errors, it will return an empty String and False.
 */
private fun checkForErrors(context: Context, list: List<String>, newLayerName: String): String? {
    return if (list.contains(newLayerName)) {
        context.getString(R.string.layerNameInUse)
    } else if (newLayerName.length > CHAR_LIMIT) {
        context.getString(R.string.layerNameTooLong)
    } else {
        null
    }
}

/**
 * Function that clears focus on whatever view the user has currently selected.
 * Currently only called when the user interacts with the Gradient or Hue Slider.
 */
private fun clearAllFocus(list: List<ForcableEditText>) {
    list.forEach {
        it.clearFocus()
    }
}

object InputLimitations {
    const val CHAR_LIMIT = 16
}

/*
Rework how we process view info by invoking a lambda
 */