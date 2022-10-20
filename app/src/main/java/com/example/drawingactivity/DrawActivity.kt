package com.example.drawingactivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior


class DrawActivity : AppCompatActivity() {

    private val viewModel: DrawViewModel by viewModels()
    private val dialogUtils = DialogUtils
    private var isAddDialogOpen = false
    private var isEditDialogOpen = false
    private var isDeleteDialogOpen = false
    private var isLayerListViewSheetOpen = false
    private var isColorPickerSheetOpen = false
    lateinit var behavior: BottomSheetBehavior<LinearLayout>

    /**
     * Saves the status of the layerList dialog if the user happens to somehow close or leave it after hitting edit or delete.
     * This is done to return the values on what is being edited into the dialog so it reopens correctly.
     */
    private var onLayerListUpdate: ((LayerViewModel?, LayerViewModel?, DialogOperation) -> Unit)? =
        null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawableArea = findViewById<DrawableAreaView>(R.id.drawableArea)
        drawableArea.onBitmapDrawn = { bitmap -> viewModel.setCurrentBitmapState(bitmap) }
        val colorSelector = findViewById<CurrentColorView>(R.id.colorCircle)
        colorSelector.onChangeColor = { viewModel.cycleCurrentColor() }
        val coordinator = findViewById<CoordinatorLayout>(R.id.coordinator)
        val bottomSheet = coordinator.findViewById<LinearLayout>(R.id.toolLayout)
        behavior = BottomSheetBehavior.from(bottomSheet)
        listOf<View>(
            bottomSheet.findViewById<CurrentColorView>(R.id.pickerButton),
            bottomSheet.findViewById<ImageView>(R.id.brushButton),
            bottomSheet.findViewById<ImageView>(R.id.moveButton),
            bottomSheet.findViewById<ImageView>(R.id.resizeButton),
            bottomSheet.findViewById<ImageView>(R.id.filterButton),
            bottomSheet.findViewById<ImageView>(R.id.layerButton),
        ).forEach {
            it.setOnClickListener { behavior.state = BottomSheetBehavior.STATE_EXPANDED }
        }

        findViewById<Button>(R.id.addLayer).setOnClickListener {
            viewModel.addOrEditClicked()
        }

        findViewById<Button>(R.id.viewLayers).setOnClickListener {
            viewModel.bottomSheetOpened()
        }

        findViewById<Button>(R.id.gradientButton).setOnClickListener {
            viewModel.colorPickerOpened()
        }

        val imageView = findViewById<ImageView>(R.id.placeImage)
        val imageContent = registerForActivityResult(ActivityResultContracts.GetContent()) { it ->
            it?.let { uri ->
                BitmapFromUri(context = this, url = uri, onComplete = {
                    imageView.setImageBitmap(it)
                }, onError = {
                    imageView.setImageBitmap(it)
                    Log.v(
                        "Error in Getting Bitmap Image from uri!", "Placing Error Bitmap Instead."
                    )
                })
            }
        }

        findViewById<Button>(R.id.grabImage).setOnClickListener {
            Intent(it.context, DrawActivity::class.java).apply {
                imageContent.launch("image/*")
            }
        }

        viewModel.apply {
            onAddLayer = {
                Toast.makeText(
                    this@DrawActivity,
                    this@DrawActivity.getString(R.string.addLayerSuccess, it),
                    Toast.LENGTH_SHORT
                ).show()
            }
            onDeleteLayer = {
                if (it != null) {
                    Toast.makeText(
                        this@DrawActivity,
                        this@DrawActivity.getString(R.string.deleteLayerSuccess, it),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@DrawActivity,
                        this@DrawActivity.getString(R.string.deleteLayerFailure),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            onEditLayer = { oldName, newName ->
                Toast.makeText(
                    this@DrawActivity,
                    this@DrawActivity.getString(R.string.editLayerSuccess, oldName, newName),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.onUpdate = {
            colorSelector.circlePrimaryColor = it.primaryColor
            drawableArea.setPathColor(it.primaryColor)
            it.currentBitmap?.let { bitmap -> drawableArea.setBitmap(bitmap) }
            if (it.layerList.isEmpty()) {
                viewModel.addLayer(LayerViewModel(this.getString(R.string.layerHint, 1)))
            }
            if (!it.isLayerListViewSheetOpen && it.isDialogOpen && !isAddDialogOpen) {
                isAddDialogOpen = true
                dialogUtils.showDialog(
                    context = this@DrawActivity,
                    layerList = it.layerList,
                    previousInput = it.currentNewLayerName,
                    onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                    onSubmit = { layerName ->
                        val model = LayerViewModel(layerName)
                        viewModel.addLayer(model)
                        if (layerName != null) {
                            onLayerListUpdate?.invoke(
                                null, null, DialogOperation.ADD
                            )
                        }
                    },
                )
            } else isAddDialogOpen = false
            if (it.isLayerListViewSheetOpen && !isLayerListViewSheetOpen) {
                isLayerListViewSheetOpen = true
                onLayerListUpdate = dialogUtils.layerListBottomSheet(context = this,
                    layerList = it.layerList,
                    onSheetComplete = {
                        isLayerListViewSheetOpen = false
                        viewModel.bottomSheetDone()
                    },
                    onDelete = { model ->
                        viewModel.setCurrentModel(model)
                        viewModel.deleteClicked()
                        isDeleteDialogOpen = true
                        if (it.isDeleteDialogOpen) {
                            dialogUtils.confirmationDialog(context = this@DrawActivity,
                                title = this@DrawActivity.getString(R.string.deleteLayerDialogTitle),
                                message = this@DrawActivity.getString(
                                    R.string.deleteLayerDialogBody, model.layerName
                                ),
                                onSubmit = { isConfirmed ->
                                    viewModel.setCurrentModel(model)
                                    viewModel.deleteLayer(model, isConfirmed)
                                    if (isConfirmed) {
                                        onLayerListUpdate?.invoke(
                                            model, null, DialogOperation.DELETE
                                        )
                                    }
                                })
                        }
                    },
                    onEdit = { model ->
                        isEditDialogOpen = true
                        viewModel.addOrEditClicked()
                        viewModel.setCurrentModel(model)
                        dialogUtils.showDialog(
                            context = this,
                            layerList = it.layerList,
                            onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                            previousInput = it.currentNewLayerName,
                            onSubmit = { newLayerName ->
                                val newModel = LayerViewModel(newLayerName)
                                viewModel.editLayer(newModel, model)
                                if (newLayerName != null) {
                                    onLayerListUpdate?.invoke(
                                        model, newModel, DialogOperation.EDIT
                                    )
                                }
                            },
                        )
                    })
            }
            if (it.isDeleteDialogOpen && !isDeleteDialogOpen) {
                isDeleteDialogOpen = true
                dialogUtils.confirmationDialog(context = this@DrawActivity,
                    title = this@DrawActivity.getString(R.string.deleteLayerDialogTitle),
                    message = this@DrawActivity.getString(
                        R.string.deleteLayerDialogBody, it.targetModel?.layerName
                    ),
                    onSubmit = { isConfirmed ->
                        onLayerListUpdate?.invoke(
                            it.targetModel, null, DialogOperation.DELETE
                        )
                        it.targetModel?.let { targetModel ->
                            if (isConfirmed) {
                                viewModel.deleteLayer(
                                    targetModel, true
                                )
                            }
                        }
                    })
            } else isDeleteDialogOpen = false
            if (it.isLayerListViewSheetOpen && it.isDialogOpen && !isEditDialogOpen) {
                isEditDialogOpen = true
                dialogUtils.showDialog(
                    context = this,
                    layerList = it.layerList,
                    onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                    previousInput = it.currentNewLayerName,
                    onSubmit = { newName ->
                        val newModel = LayerViewModel(newName)
                        it.targetModel?.let { targetModel ->
                            viewModel.editLayer(
                                newModel, targetModel
                            )
                        }
                        if (newName != null) {
                            onLayerListUpdate?.invoke(
                                it.targetModel, newModel, DialogOperation.EDIT
                            )
                        }
                    },
                )
            } else isEditDialogOpen = false
            if (it.isLayerListViewSheetOpen && it.isColorPickerSheetOpen && !isColorPickerSheetOpen) {
                isColorPickerSheetOpen = true
                dialogUtils.colorPickerBottomSheet(context = this@DrawActivity,
                    lastHsv = it.hsvArray,
                    onColorAdjust = { hsv -> hsv?.let { viewModel.setCurrentColor(hsv) } }) { hsv ->
                    hsv?.let { viewModel.setCurrentColor(hsv) }
                    viewModel.gradientCompleted()
                    isColorPickerSheetOpen = false
                }
            } else if (!it.isColorPickerSheetOpen) {
                isColorPickerSheetOpen = false
            }
        }
    }
}