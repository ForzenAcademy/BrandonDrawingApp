package com.example.drawingactivity

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class DrawActivity : AppCompatActivity() {

    private val viewModel: DrawViewModel by viewModels()
    private val dialogUtils = DialogUtils
    private var isAddDialogOpen = false
    private var isEditDialogOpen = false
    private var isLayerListViewSheetOpen = false
    private var bottomSheetLambda: ((Int, String?, DialogOperation) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawableArea = findViewById<DrawableAreaView>(R.id.drawableArea)
        drawableArea.onBitmapDrawn = { bitmap -> viewModel.setCurrentBitmapState(bitmap) }
        viewModel.currentBitmap?.let { drawableArea.setBitmap(it) }
        val colorSelector = findViewById<PrimaryColorCircleView>(R.id.colorCircle)
        colorSelector.onChangeColor = { viewModel.cycleCurrentColor() }

        findViewById<Button>(R.id.addLayer).setOnClickListener {
            viewModel.setDialogState()
        }

        findViewById<Button>(R.id.viewLayers).setOnClickListener {
            viewModel.setLayerListViewState()
        }

        viewModel.apply {
            onAddLayerComplete = {
                Toast.makeText(
                    this@DrawActivity,
                    this@DrawActivity.getString(R.string.addLayerSuccess, it),
                    Toast.LENGTH_SHORT
                ).show()
            }
            onDeleteLayerComplete = {
                Toast.makeText(
                    this@DrawActivity,
                    this@DrawActivity.getString(R.string.deleteLayerSuccess, it),
                    Toast.LENGTH_SHORT
                ).show()
            }
            onEditLayerComplete = { oldName, newName ->
                Toast.makeText(
                    this@DrawActivity,
                    this@DrawActivity.getString(R.string.editLayerSuccess, oldName, newName),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.onUpdate = {
            if (it.layerList.isEmpty()) {
                viewModel.addLayer(this.getString(R.string.layerHint, 1))
            }
            colorSelector.circlePrimaryColor = it.primaryColor
            drawableArea.pathPaint.color = it.primaryColor
            drawableArea.userDrawnBitmap = it.currentBitmap
            if (!it.isLayerSheetOpen && it.isDialogOpen && !isAddDialogOpen) {
                isAddDialogOpen = true
                dialogUtils.showDialog(
                    context = this@DrawActivity,
                    layerList = it.layerList,
                    previousInput = it.currentNewLayerName,
                    onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                    onSubmit = { layerName ->
                        if (layerName != null) {
                            viewModel.addLayer(layerName)
                        } else viewModel.dialogCompleted()
                    },
                )
            } else if (!it.isDialogOpen) {
                isAddDialogOpen = false
            }

            if (it.isLayerSheetOpen && !isLayerListViewSheetOpen) {
                isLayerListViewSheetOpen = true
                viewModel.setLayerListViewState()
                bottomSheetLambda = dialogUtils.layerListBottomSheet(context = this,
                    layerList = it.layerList,
                    onDialogComplete = { viewModel.dialogCompleted() },
                    onDelete = { index ->
                        viewModel.deleteLayer(index)
                        bottomSheetLambda?.invoke(index, null, DialogOperation.DELETE)
                    },
                    onEdit = { index ->
                        isEditDialogOpen = true
                        viewModel.setDialogState()
                        viewModel.setEditIndex(index)
                        dialogUtils.showDialog(
                            context = this,
                            layerList = it.layerList,
                            onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                            previousInput = it.currentNewLayerName,
                            onSubmit = { newLayerName ->
                                if (newLayerName != null) {
                                    viewModel.editLayer(
                                        newLayerName, index ?: it.editLayerIndex
                                    )
                                    if (index != null) {
                                        bottomSheetLambda?.invoke(index, newLayerName,
                                            DialogOperation.EDIT
                                        )
                                    } else {
                                        bottomSheetLambda?.invoke(
                                            it.editLayerIndex, newLayerName, DialogOperation.EDIT
                                        )
                                    }
                                } else viewModel.dialogCompleted()
                            },
                        )
                    })
                if (it.isDialogOpen && !isEditDialogOpen) {
                    isEditDialogOpen = true
                    dialogUtils.showDialog(
                        context = this,
                        layerList = it.layerList,
                        onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                        previousInput = it.currentNewLayerName,
                        onSubmit = { newLayerName ->
                            if (newLayerName != null) {
                                viewModel.editLayer(newLayerName, it.editLayerIndex)
                                bottomSheetLambda?.invoke(it.editLayerIndex, newLayerName,
                                    DialogOperation.EDIT
                                )
                            } else viewModel.dialogCompleted()
                        },
                    )
                } else if (!it.isDialogOpen) {
                    isEditDialogOpen = false
                }
            } else if (!it.isLayerSheetOpen) {
                isLayerListViewSheetOpen = false
            }
        }
    }

}