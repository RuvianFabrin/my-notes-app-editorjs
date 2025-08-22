package com.example.mynotesapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesapp.models.Note
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var sortButton: MaterialButton
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var fabNewNote: FloatingActionButton
    private lateinit var emptyStateText: TextView
    
    private lateinit var noteRepository: NoteRepository
    private lateinit var noteListAdapter: NoteListAdapter
    
    private var currentSortOption = NoteRepository.SortOption.DATE_DESC
    private var currentSearchQuery = ""
    private var allNotes = listOf<Note>()

    companion object {
        const val REQUEST_CODE_EDIT_NOTE = 1001
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_IS_NEW_NOTE = "extra_is_new_note"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        setupRepository()
        setupRecyclerView()
        setupSearchFunctionality()
        setupSortFunctionality()
        setupFab()
        
        loadNotes()
    }

    override fun onResume() {
        super.onResume()
        // Reload notes when returning from editor
        loadNotes()
    }

    private fun initializeViews() {
        searchEditText = findViewById(R.id.searchEditText)
        sortButton = findViewById(R.id.sortButton)
        notesRecyclerView = findViewById(R.id.notesRecyclerView)
        fabNewNote = findViewById(R.id.fabNewNote)
        emptyStateText = findViewById(R.id.emptyStateText)
    }

    private fun setupRepository() {
        noteRepository = NoteRepository.getInstance(this)
    }

    private fun setupRecyclerView() {
        noteListAdapter = NoteListAdapter(
            notes = emptyList(),
            onNoteClick = { note ->
                openNoteEditor(note.id, false)
            },
            onEditClick = { note ->
                openNoteEditor(note.id, false)
            },
            onDeleteClick = { note ->
                showDeleteConfirmation(note)
            }
        )
        
        notesRecyclerView.layoutManager = LinearLayoutManager(this)
        notesRecyclerView.adapter = noteListAdapter
    }

    private fun setupSearchFunctionality() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                filterAndSortNotes()
            }
        })
    }

    private fun setupSortFunctionality() {
        sortButton.setOnClickListener {
            showSortOptionsDialog()
        }
        
        updateSortButtonText()
    }

    private fun setupFab() {
        fabNewNote.setOnClickListener {
            openNoteEditor(0, true)
        }
    }

    private fun loadNotes() {
        lifecycleScope.launch {
            try {
                val result = noteRepository.getAllNotes()
                result.onSuccess { notes ->
                    allNotes = notes
                    filterAndSortNotes()
                }.onFailure { exception ->
                    showError("Erro ao carregar notas: ${exception.message}")
                }
            } catch (e: Exception) {
                showError("Erro inesperado: ${e.message}")
            }
        }
    }

    private fun filterAndSortNotes() {
        lifecycleScope.launch {
            try {
                val filteredNotes = if (currentSearchQuery.isEmpty()) {
                    allNotes
                } else {
                    allNotes.filter { note ->
                        note.matchesSearch(currentSearchQuery)
                    }
                }
                
                val sortedNotes = noteRepository.sortNotes(filteredNotes, currentSortOption)
                
                runOnUiThread {
                    noteListAdapter.updateNotes(sortedNotes)
                    updateEmptyState(sortedNotes.isEmpty())
                }
            } catch (e: Exception) {
                showError("Erro ao filtrar notas: ${e.message}")
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyStateText.visibility = View.VISIBLE
            notesRecyclerView.visibility = View.GONE
            
            emptyStateText.text = if (currentSearchQuery.isEmpty()) {
                getString(R.string.no_notes_found)
            } else {
                "Nenhuma nota encontrada para '$currentSearchQuery'"
            }
        } else {
            emptyStateText.visibility = View.GONE
            notesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showSortOptionsDialog() {
        val sortOptions = arrayOf(
            "Data de atualização (mais recente)",
            "Data de atualização (mais antiga)",
            "Título (A-Z)",
            "Título (Z-A)",
            "Data de criação (mais recente)",
            "Data de criação (mais antiga)"
        )
        
        val currentSelection = when (currentSortOption) {
            NoteRepository.SortOption.DATE_DESC -> 0
            NoteRepository.SortOption.DATE_ASC -> 1
            NoteRepository.SortOption.TITLE_ASC -> 2
            NoteRepository.SortOption.TITLE_DESC -> 3
            NoteRepository.SortOption.CREATED_DESC -> 4
            NoteRepository.SortOption.CREATED_ASC -> 5
        }
        
        AlertDialog.Builder(this)
            .setTitle("Ordenar por")
            .setSingleChoiceItems(sortOptions, currentSelection) { dialog, which ->
                currentSortOption = when (which) {
                    0 -> NoteRepository.SortOption.DATE_DESC
                    1 -> NoteRepository.SortOption.DATE_ASC
                    2 -> NoteRepository.SortOption.TITLE_ASC
                    3 -> NoteRepository.SortOption.TITLE_DESC
                    4 -> NoteRepository.SortOption.CREATED_DESC
                    5 -> NoteRepository.SortOption.CREATED_ASC
                    else -> NoteRepository.SortOption.DATE_DESC
                }
                
                updateSortButtonText()
                filterAndSortNotes()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateSortButtonText() {
        sortButton.text = when (currentSortOption) {
            NoteRepository.SortOption.DATE_DESC -> "Data ↓"
            NoteRepository.SortOption.DATE_ASC -> "Data ↑"
            NoteRepository.SortOption.TITLE_ASC -> "Título ↑"
            NoteRepository.SortOption.TITLE_DESC -> "Título ↓"
            NoteRepository.SortOption.CREATED_DESC -> "Criação ↓"
            NoteRepository.SortOption.CREATED_ASC -> "Criação ↑"
        }
    }

    private fun openNoteEditor(noteId: Int, isNewNote: Boolean) {
        val intent = Intent(this, NoteEditorActivity::class.java).apply {
            putExtra(EXTRA_NOTE_ID, noteId)
            putExtra(EXTRA_IS_NEW_NOTE, isNewNote)
        }
        startActivityForResult(intent, REQUEST_CODE_EDIT_NOTE)
    }

    private fun showDeleteConfirmation(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Nota")
            .setMessage("Tem certeza que deseja excluir a nota '${note.title}'?")
            .setPositiveButton("Excluir") { _, _ ->
                deleteNote(note)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteNote(note: Note) {
        lifecycleScope.launch {
            try {
                val result = noteRepository.deleteNote(note.id)
                result.onSuccess {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show()
                        loadNotes() // Reload the list
                    }
                }.onFailure { exception ->
                    runOnUiThread {
                        showError("Erro ao excluir nota: ${exception.message}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError("Erro inesperado ao excluir: ${e.message}")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_EDIT_NOTE && resultCode == RESULT_OK) {
            // Note was saved successfully, reload the list
            loadNotes()
        }
    }

    private fun exportNote(note: Note) {
        lifecycleScope.launch {
            try {
                val result = noteRepository.exportNoteAsJson(note.id)
                result.onSuccess { jsonContent ->
                    // Here you could implement sharing or saving to file
                    // For now, we'll just show a toast
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Nota exportada (JSON copiado)", Toast.LENGTH_SHORT).show()
                        // You could copy to clipboard here
                    }
                }.onFailure { exception ->
                    runOnUiThread {
                        showError("Erro ao exportar nota: ${exception.message}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError("Erro inesperado na exportação: ${e.message}")
                }
            }
        }
    }

    private fun getNotesCount(): Int {
        return noteListAdapter.getCurrentNotes().size
    }

    private fun refreshNotes() {
        loadNotes()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }
}
