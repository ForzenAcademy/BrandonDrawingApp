package com.example.drawingactivity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.drawingactivity.databinding.ActivityMainBinding
import com.example.drawingactivity.databinding.AddLayerDialogBinding
import com.example.drawingactivity.databinding.RecyclerLayerListLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DrawActivity : AppCompatActivity() {

    private val viewModel: DrawViewModel by viewModels()
    private val dialogUtils = DialogUtils
    private var isAddDialogOpen = false
    private var isEditDialogOpen = false
    private var isDeleteDialogOpen = false
    private var isLayerListViewSheetOpen = false

    /**
     * Saves the status of the layerList dialog if the user happens to somehow close or leave it after hitting edit or delete.
     * This is done to return the values on what is being edited into the dialog so it reopens correctly.
     */
    private var onLayerListUpdate: ((LayerViewModel?, LayerViewModel?, DialogOperation) -> Unit)? =
        null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val bottomSheet = binding.toolbar
        val colorPicker = bottomSheet.colorSelectorLayout
        val toolMap = mapOf(
            Pair(
                ToolType.GRADIENT,
                ToolButtonData(bottomSheet.pickerButton, bottomSheet.colorSelectorLayout.root)
            ),
            Pair(
                ToolType.BRUSH,
                ToolButtonData(bottomSheet.brushButton, bottomSheet.brushLayout.root)
            ),
            Pair(
                ToolType.MOVE,
                ToolButtonData(bottomSheet.moveButton, bottomSheet.moveLayout.root)
            ),
            Pair(
                ToolType.RESIZE,
                ToolButtonData(bottomSheet.resizeButton, bottomSheet.resizeLayout.root)
            ),
            Pair(
                ToolType.FILTER,
                ToolButtonData(bottomSheet.filterButton, bottomSheet.filterLayout.root)
            ),
            Pair(
                ToolType.LAYERS,
                ToolButtonData(bottomSheet.layerButton, bottomSheet.layerLayout.root)
            ),
        )

        val drawableArea = binding.drawableArea
        drawableArea.onBitmapDrawn = { bitmap ->
            viewModel.setCurrentBitmapState(bitmap)
            if (bitmap != null) {
                drawableArea.setBitmap(bitmap)
            }
        }
        val previousColorSelection = colorPicker.bottomColor
        val behavior = BottomSheetBehavior.from(bottomSheet.toolLayout)
        toolMap.forEach { toolData ->
            toolData.value.view.setOnClickListener {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                viewModel.toolIconClicked(toolData.key)
                toolData.value.showView(bottomSheet.toolLayout, toolMap)
                toolData.value.fadeInView(bottomSheet.toolLayout, toolData.value.layout)
            }
        }

        dialogUtils.colorPickerCalculations(picker = colorPicker,
            behavior = behavior,
            lastHsv = Hsv(),
            onColorAdjust = { hsv -> hsv?.let { viewModel.userUpdatesColor(hsv) } },
            onSubmit = { hsv ->
                hsv?.let {
                    viewModel.userUpdatesColor(hsv)
                    viewModel.userConfirmsColor()
                }
            })

        binding.addLayer.setOnClickListener {
            viewModel.addOrEditClicked()
        }

        binding.viewLayers.setOnClickListener {
            viewModel.bottomSheetOpened()
        }

        val imageView = binding.placeImage
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

        binding.grabImage.setOnClickListener {
            Intent(it.context, DrawActivity::class.java).apply {
                imageContent.launch("image/*")
            }
        }

        viewModel.initialize()

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
            onConfirmColor = {
                if (it != null) {
                    bottomSheet.pickerButton.setCircleColor(it)
                    drawableArea.setPathColor(it)
                    previousColorSelection.setBackgroundColor(it)
                }
            }
        }

        viewModel.onUpdate = {
            it.currentBitmap?.let { bitmap ->
                Log.v("brabrabra vm bitmap", it.currentBitmap.toString())
                drawableArea.setBitmap(bitmap)
            }
            if (it.layerList.isEmpty()) {
                viewModel.addLayer(LayerViewModel(this.getString(R.string.layerHint, 1)))
            }
            toolMap[it.activeTool]?.showView(bottomSheet.toolLayout, toolMap)
            if (!it.isLayerListViewSheetOpen && it.isDialogOpen && !isAddDialogOpen) {
                isAddDialogOpen = true
                dialogUtils.showDialog(
                    binding = AddLayerDialogBinding.inflate(layoutInflater),
                    context = this@DrawActivity,
                    layerList = it.layerList,
                    previousInput = it.currentNewLayerName,
                    onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                    onSubmit = { layerName ->
                        val model = LayerViewModel(layerName)
                        if (layerName != null) {
                            viewModel.addLayer(model)
                        }
                    },
                )
            } else isAddDialogOpen = false
            if (it.isLayerListViewSheetOpen && !isLayerListViewSheetOpen) {
                isLayerListViewSheetOpen = true
                onLayerListUpdate = dialogUtils.layerListBottomSheet(
                    binding = RecyclerLayerListLayoutBinding.inflate(layoutInflater),
                    context = this,
                    layerList = it.layerList,
                    onSheetComplete = {
                        isLayerListViewSheetOpen = false
                        viewModel.bottomSheetDone()
                    },
                    onDelete = { model ->
                        viewModel.setCurrentModel(model)
                        viewModel.deleteClicked()
                        isDeleteDialogOpen = true
                    },
                    onEdit = { model ->
                        viewModel.setCurrentModel(model)
                        viewModel.addOrEditClicked()
                        isEditDialogOpen = true
                    })
            }
            if (it.isLayerListViewSheetOpen && it.isDeleteDialogOpen && !isDeleteDialogOpen) {
                isDeleteDialogOpen = true
                dialogUtils.confirmationDialog(context = this@DrawActivity,
                    title = this@DrawActivity.getString(R.string.deleteLayerDialogTitle),
                    message = this@DrawActivity.getString(
                        R.string.deleteLayerDialogBody, it.targetModel?.layerName
                    ),
                    onSubmit = { isConfirmed ->
                        it.targetModel?.let { targetModel ->
                            if (isConfirmed) {
                                viewModel.deleteLayer(
                                    targetModel, true
                                )
                                onLayerListUpdate?.invoke(
                                    it.targetModel, null, DialogOperation.DELETE
                                )
                            }
                        }
                        isDeleteDialogOpen = false
                        viewModel.dialogComplete()
                    })
            } else isDeleteDialogOpen = false
            if (it.isLayerListViewSheetOpen && it.isDialogOpen && !isEditDialogOpen) {
                isEditDialogOpen = true
                dialogUtils.showDialog(
                    binding = AddLayerDialogBinding.inflate(layoutInflater),
                    context = this,
                    layerList = it.layerList,
                    onEditText = { layerName -> viewModel.setCurrentText(layerName) },
                    previousInput = it.currentNewLayerName,
                    onSubmit = { newName ->
                        val newModel = LayerViewModel(newName)
                        if (newName != null) {
                            it.targetModel?.let { targetModel ->
                                viewModel.editLayer(
                                    newModel, targetModel
                                )
                            }
                            onLayerListUpdate?.invoke(
                                it.targetModel, newModel, DialogOperation.EDIT
                            )
                        }
                        isEditDialogOpen = false
                        viewModel.dialogComplete()
                    },
                )
            } else isEditDialogOpen = false
        }
    }
}