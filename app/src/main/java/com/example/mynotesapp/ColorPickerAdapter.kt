package com.example.mynotesapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ColorPickerAdapter(
    private var colors: List<Pair<String, Int>> = emptyList(),
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorButton: MaterialButton = itemView.findViewById(R.id.colorButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_picker, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val (colorName, colorValue) = colors[position]
        
        holder.colorButton.apply {
            setBackgroundColor(colorValue)
            text = ""
            
            // Add border for better visibility
            strokeColor = Color.BLACK
            strokeWidth = 2
            
            setOnClickListener {
                onColorSelected(colorValue)
            }
            
            // Set content description for accessibility
            contentDescription = colorName
        }
    }

    override fun getItemCount(): Int = colors.size

    fun updateColors(newColors: List<Pair<String, Int>>) {
        colors = newColors
        notifyDataSetChanged()
    }
}
