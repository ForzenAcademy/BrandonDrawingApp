package com.example.drawingactivity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.example.drawingactivity.CustomColorViewModel.InputLimitations.NAME_EMPTY
import com.example.drawingactivity.CustomColorViewModel.InputLimitations.NAME_LENGTH_MAX

class MainActivity : AppCompatActivity() {

    private val viewModel: CustomColorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawableArea = findViewById<DrawableAreaView>(R.id.drawableArea)
        drawableArea.onBitmapDrawn = { bitmap -> viewModel.saveUserBitmap(bitmap) }
        viewModel.currentBitmap?.let { drawableArea.setBitmap(it) }
        val colorSelector = findViewById<PrimaryColorCircleView>(R.id.colorCircle)
        colorSelector.onChangeColor = { viewModel.cyclePrimaryColor() }
        findViewById<Button>(R.id.add_layer).setOnClickListener {
            openColorPickerDialog()
        }

        if (viewModel.isDialogOpen) {
            openColorPickerDialog()
        }

        viewModel.onAddLayerOperationDone = {
            if (it != null) {
                Toast.makeText(this, getString(R.string.addLayerSuccess, it), Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, getString(R.string.addLayerFailure), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.onUpdate = {
            colorSelector.circlePrimaryColor = it.primaryColor
            drawableArea.pathPaint.color = it.primaryColor
            drawableArea.userDrawnBitmap = it.currentBitmap
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun openColorPickerDialog() {
        viewModel.isDialogOpen = true
        val inflater = LayoutInflater.from(this@MainActivity)
        val view = inflater.inflate(R.layout.add_layer_dialog, null)
        val dialog = AlertDialog.Builder(this).apply {
            setTitle(context.getString(R.string.addLayerDialogTitle))
            setView(view)
            val errorView = view.findViewById<TextView>(R.id.errorText)
            val layerNameInput = view.findViewById<EditText>(R.id.layerNameField)
            while (!viewModel.defaultLayerHintFound) {
                val currentHint = getString(R.string.addLayerDefaultName, viewModel.layerNumber)
                viewModel.createLayerHint(currentHint)
            }
            layerNameInput.hint = getString(R.string.addLayerDefaultName, viewModel.layerNumber)
            if (viewModel.isDialogOpen && viewModel.currentUserEnteredLayerName != null) {
                layerNameInput.setText(viewModel.currentUserEnteredLayerName)
                errorView.isVisible = layerNameInput.text.toString().length > NAME_LENGTH_MAX
            }
            layerNameInput.addTextChangedListener {
                viewModel.currentUserEnteredLayerName = it.toString()
                errorView.isVisible = layerNameInput.text.toString().length > NAME_LENGTH_MAX
            }
            setPositiveButton(getString(R.string.addLayerPositiveButton)) { _, _ ->
                var layerName: String = layerNameInput.text.toString()
                when (layerName) {
                    getString(R.string.addLayerDefaultName, viewModel.layerNumber) -> {
                        viewModel.addLayer(layerName)
                    }
                    NAME_EMPTY -> {
                        layerName = getString(R.string.addLayerDefaultName, viewModel.layerNumber)
                        viewModel.addLayer(layerName)
                    }
                    else -> {
                        viewModel.addLayer(layerName)
                    }
                }
                viewModel.dialogCompleted()
            }
            setNegativeButton(getString(R.string.addLayerNegativeButton)) { _, _ ->
                viewModel.dialogCompleted()
            }
            setOnCancelListener {
                viewModel.dialogCompleted()
            }
        }
        dialog.show()
    }
}