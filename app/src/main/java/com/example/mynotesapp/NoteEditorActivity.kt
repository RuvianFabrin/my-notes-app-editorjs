package com.example.mynotesapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesapp.models.EditorBlock
import com.example.mynotesapp.models.Note
import com.example.mynotesapp.utils.ColorFormatter
import com.example.mynotesapp.utils.EditorJSJsonConverter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var titleEditText: TextInputEditText
    private lateinit var tagsEditText: TextInputEditText
    private lateinit var addBlockButton: MaterialButton
    private lateinit var textColorButton: MaterialButton
    private lateinit var backgroundColorButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var cancelButton: MaterialButton
    private lateinit var editorBlocksRecyclerView: RecyclerView
    private lateinit var colorPickerCard: MaterialCardView
    private lateinit var colorPickerRecyclerView: RecyclerView
    private lateinit var closeColorPickerButton: MaterialButton
    
    private lateinit var noteRepository: NoteRepository
    private lateinit var editorAdapter: EditorRecyclerViewAdapter
    private lateinit var colorPickerAdapter: ColorPickerAdapter
    
    private var currentNote: Note? = null
    private var isNewNote = false
    private var noteId = 0
    private var currentFocusedEditText: EditText? = null
    private var isSelectingTextColor = true
    
    private val editorBlocks = mutableListOf<EditorBlock>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_editor)
        
        initializeViews()
        setupRepository()
        setupToolbar()
        setupEditor()
        setupColorPicker()
        setupButtons()
        
        loadNoteData()
    }

    private fun initializeViews() {
        titleEditText = findViewById(R.id.titleEditText)
        tagsEditText = findViewById(R.id.tagsEditText)
        addBlockButton = findViewById(R.id.addBlockButton)
        textColorButton = findViewById(R.id.textColorButton)
        backgroundColorButton = findViewById(R.id.backgroundColorButton)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        editorBlocksRecyclerView = findViewById(R.id.editorBlocksRecyclerView)
        colorPickerCard = findViewById(R.id.colorPickerCard)
        colorPickerRecyclerView = findViewById(R.id.colorPickerRecyclerView)
        closeColorPickerButton = findViewById(R.id.closeColorPickerButton)
    }

    private fun setupRepository() {
        noteRepository = NoteRepository.getInstance(this)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupEditor() {
        editorAdapter = EditorRecyclerViewAdapter(
            blocks = editorBlocks,
            onBlockChanged = { position, block ->
                if (position < editorBlocks.size) {
                    editorBlocks[position] = block
                }
            },
            onBlockDeleted = { position ->
                if (editorBlocks.size > 1 && position < editorBlocks.size) {
                    editorBlocks.removeAt(position)
                    editorAdapter.notifyItemRemoved(position)
                }
            },
            onAddBlock = { position ->
                addNewBlock(position + 1)
            },
            onTextSelectionChanged = { editText ->
                currentFocusedEditText = editText
            }
        )
        
        editorBlocksRecyclerView.layoutManager = LinearLayoutManager(this)
        editorBlocksRecyclerView.adapter = editorAdapter
    }

    private fun setupColorPicker() {
        colorPickerAdapter = ColorPickerAdapter(
            colors = emptyList(),
            onColorSelected = { color ->
                applyColorToSelection(color)
                hideColorPicker()
            }
        )
        
        colorPickerRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        colorPickerRecyclerView.adapter = colorPickerAdapter
    }

    private fun setupButtons() {
        addBlockButton.setOnClickListener {
            showAddBlockDialog()
        }
        
        textColorButton.setOnClickListener {
            if (currentFocusedEditText != null && ColorFormatter.hasSelection(currentFocusedEditText!!)) {
                isSelectingTextColor = true
                showColorPicker(ColorFormatter.getAvailableTextColors())
            } else {
                Toast.makeText(this, "Selecione um texto primeiro", Toast.LENGTH_SHORT).show()
            }
        }
        
        backgroundColorButton.setOnClickListener {
            if (currentFocusedEditText != null && ColorFormatter.hasSelection(currentFocusedEditText!!)) {
                isSelectingTextColor = false
                showColorPicker(ColorFormatter.getAvailableBackgroundColors())
            } else {
                Toast.makeText(this, "Selecione um texto primeiro", Toast.LENGTH_SHORT).show()
            }
        }
        
        saveButton.setOnClickListener {
            saveNote()
        }
        
        cancelButton.setOnClickListener {
            onBackPressed()
        }
        
        closeColorPickerButton.setOnClickListener {
            hideColorPicker()
        }
    }

    private fun loadNoteData() {
        noteId = intent.getIntExtra(MainActivity.EXTRA_NOTE_ID, 0)
        isNewNote = intent.getBooleanExtra(MainActivity.EXTRA_IS_NEW_NOTE, false)
        
        if (isNewNote || noteId == 0) {
            // New note
            setupNewNote()
        } else {
            // Edit existing note
            loadExistingNote(noteId)
        }
    }

    private fun setupNewNote() {
        supportActionBar?.title = getString(R.string.new_note)
        
        // Initialize with empty paragraph block
        editorBlocks.clear()
        editorBlocks.add(EditorBlock.createParagraph())
        editorAdapter.updateBlocks(editorBlocks)
    }

    private fun loadExistingNote(noteId: Int) {
        supportActionBar?.title = getString(R.string.edit_note)
        
        lifecycleScope.launch {
            try {
                val result = noteRepository.getNoteById(noteId)
                result.onSuccess { note ->
                    if (note != null) {
                        currentNote = note
                        runOnUiThread {
                            populateNoteData(note)
                        }
                    } else {
                        runOnUiThread {
                            showError("Nota não encontrada")
                            finish()
                        }
                    }
                }.onFailure { exception ->
                    runOnUiThread {
                        showError("Erro ao carregar nota: ${exception.message}")
                        finish()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError("Erro inesperado: ${e.message}")
                    finish()
                }
            }
        }
    }

    private fun populateNoteData(note: Note) {
        titleEditText.setText(note.title)
        tagsEditText.setText(note.tags)
        
        // Load blocks from note content
        val blocks = note.getContentAsBlocks()
        editorBlocks.clear()
        editorBlocks.addAll(blocks)
        editorAdapter.updateBlocks(editorBlocks)
    }

    private fun showAddBlockDialog() {
        val blockTypes = arrayOf(
            "Parágrafo",
            "Cabeçalho",
            "Citação",
            "Lista",
            "Código",
            "Aviso",
            "Delimitador"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Adicionar Bloco")
            .setItems(blockTypes) { _, which ->
                val newBlock = when (which) {
                    0 -> EditorBlock.createParagraph()
                    1 -> EditorBlock.createHeader()
                    2 -> EditorBlock.createQuote()
                    3 -> EditorBlock.createList()
                    4 -> EditorBlock.createCode()
                    5 -> EditorBlock.createWarning()
                    6 -> EditorBlock.createDelimiter()
                    else -> EditorBlock.createParagraph()
                }
                addNewBlock(editorBlocks.size, newBlock)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addNewBlock(position: Int, block: EditorBlock = EditorBlock.createParagraph()) {
        editorBlocks.add(position, block)
        editorAdapter.notifyItemInserted(position)
        
        // Scroll to the new block
        editorBlocksRecyclerView.scrollToPosition(position)
    }

    private fun showColorPicker(colors: List<Pair<String, Int>>) {
        colorPickerAdapter.updateColors(colors)
        colorPickerCard.visibility = View.VISIBLE
        
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).findViewById<android.widget.TextView>(R.id.colorPickerTitle)?.text = 
            if (isSelectingTextColor) "Selecionar Cor do Texto" else "Selecionar Cor de Fundo"
    }

    private fun hideColorPicker() {
        colorPickerCard.visibility = View.GONE
    }

    private fun applyColorToSelection(color: Int) {
        currentFocusedEditText?.let { editText ->
            val success = if (isSelectingTextColor) {
                ColorFormatter.applyTextColor(editText, color)
            } else {
                ColorFormatter.applyBackgroundColor(editText, color)
            }
            
            if (success) {
                Toast.makeText(this, "Cor aplicada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao aplicar cor", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNote() {
        val title = titleEditText.text?.toString()?.trim() ?: ""
        val tags = tagsEditText.text?.toString()?.trim() ?: ""
        
        if (title.isEmpty()) {
            Toast.makeText(this, "Título é obrigatório", Toast.LENGTH_SHORT).show()
            titleEditText.requestFocus()
            return
        }
        
        // Convert blocks to JSON
        val contentJson = EditorJSJsonConverter.blocksToJson(editorBlocks)
        
        lifecycleScope.launch {
            try {
                val result = if (isNewNote || currentNote == null) {
                    // Create new note
                    val newNote = Note(
                        title = title,
                        content = contentJson,
                        tags = tags,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    noteRepository.insertNote(newNote)
                } else {
                    // Update existing note
                    val updatedNote = currentNote!!.copy(
                        title = title,
                        content = contentJson,
                        tags = tags,
                        updatedAt = System.currentTimeMillis()
                    )
                    noteRepository.updateNote(updatedNote)
                }
                
                result.onSuccess {
                    runOnUiThread {
                        Toast.makeText(this@NoteEditorActivity, getString(R.string.note_saved), Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                }.onFailure { exception ->
                    runOnUiThread {
                        showError("Erro ao salvar nota: ${exception.message}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError("Erro inesperado ao salvar: ${e.message}")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Descartar alterações?")
                .setMessage("Você tem alterações não salvas. Deseja descartá-las?")
                .setPositiveButton("Descartar") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("Continuar editando", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val currentTitle = titleEditText.text?.toString()?.trim() ?: ""
        val currentTags = tagsEditText.text?.toString()?.trim() ?: ""
        val currentContent = EditorJSJsonConverter.blocksToJson(editorBlocks)
        
        return if (isNewNote || currentNote == null) {
            // For new notes, check if there's any content
            currentTitle.isNotEmpty() || currentTags.isNotEmpty() || 
            editorBlocks.any { !it.isEmpty() }
        } else {
            // For existing notes, check if anything changed
            currentTitle != currentNote!!.title ||
            currentTags != currentNote!!.tags ||
            currentContent != currentNote!!.content
        }
    }

    private fun exportCurrentNote() {
        val contentJson = EditorJSJsonConverter.blocksToJson(editorBlocks)
        // Here you could implement export functionality
        Toast.makeText(this, "Exportar funcionalidade não implementada", Toast.LENGTH_SHORT).show()
    }

    private fun importFromJson() {
        // Here you could implement import functionality
        Toast.makeText(this, "Importar funcionalidade não implementada", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentFocusedEditText = null
    }
}
