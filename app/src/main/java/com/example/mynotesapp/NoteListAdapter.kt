package com.example.mynotesapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesapp.models.Note
import com.example.mynotesapp.utils.EditorJSJsonConverter
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class NoteListAdapter(
    private var notes: List<Note> = emptyList(),
    private val onNoteClick: (Note) -> Unit,
    private val onEditClick: (Note) -> Unit,
    private val onDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val contentPreviewTextView: TextView = itemView.findViewById(R.id.contentPreviewTextView)
        val tagsTextView: TextView = itemView.findViewById(R.id.tagsTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val editButton: MaterialButton = itemView.findViewById(R.id.editButton)
        val deleteButton: MaterialButton = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        
        // Título
        holder.titleTextView.text = note.title
        
        // Preview do conteúdo
        val contentPreview = getContentPreview(note)
        holder.contentPreviewTextView.text = contentPreview
        holder.contentPreviewTextView.visibility = if (contentPreview.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Tags
        val tagsText = note.getTagsList().joinToString(", ")
        holder.tagsTextView.text = tagsText
        holder.tagsTextView.visibility = if (tagsText.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Data de atualização
        holder.dateTextView.text = dateFormat.format(Date(note.updatedAt))
        
        // Click listeners
        holder.itemView.setOnClickListener {
            onNoteClick(note)
        }
        
        holder.editButton.setOnClickListener {
            onEditClick(note)
        }
        
        holder.deleteButton.setOnClickListener {
            onDeleteClick(note)
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    fun addNote(note: Note) {
        val newNotes = notes.toMutableList()
        newNotes.add(0, note) // Adiciona no início da lista
        notes = newNotes
        notifyItemInserted(0)
    }

    fun updateNote(updatedNote: Note) {
        val index = notes.indexOfFirst { it.id == updatedNote.id }
        if (index != -1) {
            val newNotes = notes.toMutableList()
            newNotes[index] = updatedNote
            notes = newNotes
            notifyItemChanged(index)
        }
    }

    fun removeNote(noteId: Int) {
        val index = notes.indexOfFirst { it.id == noteId }
        if (index != -1) {
            val newNotes = notes.toMutableList()
            newNotes.removeAt(index)
            notes = newNotes
            notifyItemRemoved(index)
        }
    }

    fun getNote(position: Int): Note? {
        return if (position >= 0 && position < notes.size) {
            notes[position]
        } else {
            null
        }
    }

    fun isEmpty(): Boolean = notes.isEmpty()

    private fun getContentPreview(note: Note): String {
        return try {
            val plainText = EditorJSJsonConverter.extractPlainText(note.content)
            if (plainText.length > 150) {
                "${plainText.take(150)}..."
            } else {
                plainText
            }
        } catch (e: Exception) {
            // Se houver erro na conversão, tenta extrair texto simples
            if (note.content.length > 150) {
                "${note.content.take(150)}..."
            } else {
                note.content
            }
        }
    }

    fun filter(query: String) {
        // Este método pode ser usado para filtrar localmente se necessário
        // Por enquanto, a filtragem é feita no repositório
    }

    fun getItemPosition(noteId: Int): Int {
        return notes.indexOfFirst { it.id == noteId }
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(notes as MutableList<Note>, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(notes as MutableList<Note>, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getCurrentNotes(): List<Note> = notes

    fun clearNotes() {
        val size = notes.size
        notes = emptyList()
        notifyItemRangeRemoved(0, size)
    }
}
