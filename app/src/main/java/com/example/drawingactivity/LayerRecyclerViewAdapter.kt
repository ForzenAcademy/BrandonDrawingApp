package com.example.drawingactivity

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.drawingactivity.AdapterViewModel.Companion.LAYER
import com.example.drawingactivity.databinding.LayerViewBinding

/**
 * This is a RecyclerView that manages a list and how it is displayed in views
 */
class RecyclerViewAdapter(
    private val data: List<AdapterViewModel>,
    val onDeleteLayer: ((LayerViewModel) -> Unit)?,
    val onEditName: ((LayerViewModel) -> Unit)?
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            LAYER -> LayerViewHolder(
                LayerViewBinding.inflate(inflater, parent, false),
                onDeleteLayer,
                onEditName
            )
            else -> {
                throw RuntimeException("View Does Not Exist.")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = data[position].type
}

abstract class AdapterViewModel(val type: Int) {
    companion object {
        const val LAYER = 0
    }
}

class LayerViewModel(
    val layerName: String?,
) : AdapterViewModel(LAYER)

abstract class ViewHolder(view: ViewBinding) : RecyclerView.ViewHolder(view.root) {
    abstract fun bind(model: AdapterViewModel)
}

/**
 * This is a view that will display a name, a delete icon, and an edit icon.
 * Has a TextView, ImageView, and ImageView
 * onDeleteLayer is called when the delete icon is pressed and pass the position of the view.
 * onEditLayer is called when the edit icon is pressed and pass the position of the view.
 */
class LayerViewHolder(
    view: LayerViewBinding,
    private val onDeleteLayer: ((LayerViewModel) -> Unit)?,
    private val onEditName: ((LayerViewModel) -> Unit)?
) : ViewHolder(view) {
    private val layerView: TextView
    private val deleteLayerView: ImageView
    private val editLayerNameView: ImageView

    init {
        layerView = view.layerName
        deleteLayerView = view.deleteLayer
        editLayerNameView = view.editName
    }

    override fun bind(model: AdapterViewModel) {
        (model as LayerViewModel).apply {
            layerView.text = model.layerName
            deleteLayerView.setOnClickListener {
                onDeleteLayer?.invoke(model)
            }
            editLayerNameView.setOnClickListener {
                onEditName?.invoke(model)
            }
        }
    }
}