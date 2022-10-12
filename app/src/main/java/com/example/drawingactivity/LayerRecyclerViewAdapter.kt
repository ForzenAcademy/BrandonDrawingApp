package com.example.drawingactivity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.drawingactivity.AdapterViewModel.Companion.LAYER

/**
 * This is a RecyclerView that manages a list and how it is displayed in views
 */
class RecyclerViewAdapter(
    private val data: List<AdapterViewModel>,
    val onDeleteLayer: ((Int) -> Unit)?,
    val onEditName: ((Int) -> Unit)?
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            LAYER -> LayerViewHolder(
                inflater.inflate(R.layout.layer_view, parent, false),
                onDeleteLayer,
                onEditName
            )
            else -> {
                throw RuntimeException("View Does Not Exist.")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
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

abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(model: AdapterViewModel, position: Int)
}

/**
 * This is a view that will display a name, a delete icon, and an edit icon.
 * Additionally, it will close any bottom sheet it is placed in when the icons are pressed
 */
class LayerViewHolder(
    view: View,
    private val onDeleteLayer: ((Int) -> Unit)?,
    private val onEditName: ((Int) -> Unit)?
) : ViewHolder(view) {
    private val layerView: TextView
    private val deleteLayerView: ImageView
    private val editLayerNameView: ImageView

    init {
        layerView = view.findViewById(R.id.layerName)
        deleteLayerView = view.findViewById(R.id.deleteLayer)
        editLayerNameView = view.findViewById(R.id.editName)
    }

    override fun bind(model: AdapterViewModel, position: Int) {
        (model as LayerViewModel).apply {
            layerView.text = model.layerName
            deleteLayerView.setOnClickListener {
                onDeleteLayer?.invoke(position)
            }
            editLayerNameView.setOnClickListener {
                onEditName?.invoke(position)
            }
        }
    }
}