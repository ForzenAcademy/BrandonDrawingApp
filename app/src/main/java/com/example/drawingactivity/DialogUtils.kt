package com.example.drawingactivity

import android.content.Context
import android.media.VolumeShaper
import android.os.FileObserver.DELETE
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
            adapter = RecyclerViewAdapter(
                data = data,
                onDeleteLayer = { onDelete?.invoke(it) },
                onEditName = { onEdit?.invoke(it) }
            )
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
private fun checkForErrors(
    context: Context, layerList: List<String>, newLayerName: String
): String? {
    return if (layerList.contains(newLayerName)) {
        context.getString(R.string.layerNameInUse)
    } else if (newLayerName.length > CHAR_LIMIT) {
        context.getString(R.string.layerNameTooLong)
    } else {
        null
    }
}

object InputLimitations {
    const val CHAR_LIMIT = 16
}