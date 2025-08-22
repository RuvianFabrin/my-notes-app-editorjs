package com.example.mynotesapp

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ListItemAdapter(
    private val items: MutableList<String>,
    private val style: String,
    private val onItemChanged: (Int, String) -> Unit,
    private val onItemDeleted: (Int) -> Unit
) : RecyclerView.Adapter<ListItemAdapter.ListItemViewHolder>() {

    class ListItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNumberTextView: TextView = itemView.findViewById(R.id.itemNumberTextView)
        val itemEditText: TextInputEditText = itemView.findViewById(R.id.itemEditText)
        val deleteItemButton: MaterialButton = itemView.findViewById(R.id.deleteItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_item, parent, false)
        return ListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        val item = items[position]
        
        // Set item number/bullet
        holder.itemNumberTextView.text = if (style == "ordered") {
            "${position + 1}."
        } else {
            "•"
        }
        
        // Set item text
        holder.itemEditText.setText(item)
        
        // Setup text watcher
        holder.itemEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val newText = s?.toString() ?: ""
                if (position < items.size) {
                    items[position] = newText
                    onItemChanged(position, newText)
                }
            }
        })
        
        // Setup delete button
        holder.deleteItemButton.setOnClickListener {
            if (items.size > 1) { // Don't allow deleting the last item
                onItemDeleted(position)
            }
        }
        
        // Hide delete button if only one item
        holder.deleteItemButton.visibility = if (items.size > 1) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: String = "") {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int) {
        if (position >= 0 && position < items.size && items.size > 1) {
            items.removeAt(position)
            notifyItemRemoved(position)
            // Update numbers for ordered lists
            if (style == "ordered") {
                notifyItemRangeChanged(position, items.size - position)
            }
        }
    }

    fun getItems(): List<String> = items.toList()
}
