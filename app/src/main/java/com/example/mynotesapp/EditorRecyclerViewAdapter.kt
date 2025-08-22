package com.example.mynotesapp

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotesapp.models.EditorBlock
import com.example.mynotesapp.utils.ColorFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EditorRecyclerViewAdapter(
    private var blocks: MutableList<EditorBlock> = mutableListOf(),
    private val onBlockChanged: (Int, EditorBlock) -> Unit,
    private val onBlockDeleted: (Int) -> Unit,
    private val onAddBlock: (Int) -> Unit,
    private val onTextSelectionChanged: (EditText) -> Unit
) : RecyclerView.Adapter<EditorRecyclerViewAdapter.BlockViewHolder>() {

    class BlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val blockTypeTextView: TextView = itemView.findViewById(R.id.blockTypeTextView)
        val blockOptionsButton: MaterialButton = itemView.findViewById(R.id.blockOptionsButton)
        val deleteBlockButton: MaterialButton = itemView.findViewById(R.id.deleteBlockButton)
        val blockContentContainer: FrameLayout = itemView.findViewById(R.id.blockContentContainer)
        
        // Text input components
        val textInputLayout: View = itemView.findViewById(R.id.textInputLayout)
        val blockEditText: TextInputEditText = itemView.findViewById(R.id.blockEditText)
        
        // Quote components
        val quoteContainer: LinearLayout = itemView.findViewById(R.id.quoteContainer)
        val quoteEditText: TextInputEditText = itemView.findViewById(R.id.quoteEditText)
        val quoteCaptionEditText: TextInputEditText = itemView.findViewById(R.id.quoteCaptionEditText)
        
        // List components
        val listContainer: LinearLayout = itemView.findViewById(R.id.listContainer)
        val listStyleButton: MaterialButton = itemView.findViewById(R.id.listStyleButton)
        val listItemsRecyclerView: RecyclerView = itemView.findViewById(R.id.listItemsRecyclerView)
        val addListItemButton: MaterialButton = itemView.findViewById(R.id.addListItemButton)
        
        // Code components
        val codeInputLayout: View = itemView.findViewById(R.id.codeInputLayout)
        val codeEditText: TextInputEditText = itemView.findViewById(R.id.codeEditText)
        
        // Warning components
        val warningContainer: LinearLayout = itemView.findViewById(R.id.warningContainer)
        val warningTitleEditText: TextInputEditText = itemView.findViewById(R.id.warningTitleEditText)
        val warningMessageEditText: TextInputEditText = itemView.findViewById(R.id.warningMessageEditText)
        
        // Delimiter
        val delimiterView: View = itemView.findViewById(R.id.delimiterView)
        
        // Header level controls
        val headerLevelContainer: LinearLayout = itemView.findViewById(R.id.headerLevelContainer)
        val headerLevel1Button: MaterialButton = itemView.findViewById(R.id.headerLevel1Button)
        val headerLevel2Button: MaterialButton = itemView.findViewById(R.id.headerLevel2Button)
        val headerLevel3Button: MaterialButton = itemView.findViewById(R.id.headerLevel3Button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_editor_block, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val block = blocks[position]
        
        // Set block type display
        holder.blockTypeTextView.text = getBlockTypeDisplayName(block.type)
        
        // Hide all containers first
        hideAllContainers(holder)
        
        // Show appropriate container and bind data
        when (block.type) {
            EditorBlock.TYPE_PARAGRAPH -> {
                setupParagraphBlock(holder, block, position)
            }
            EditorBlock.TYPE_HEADER -> {
                setupHeaderBlock(holder, block, position)
            }
            EditorBlock.TYPE_QUOTE -> {
                setupQuoteBlock(holder, block, position)
            }
            EditorBlock.TYPE_LIST -> {
                setupListBlock(holder, block, position)
            }
            EditorBlock.TYPE_CODE -> {
                setupCodeBlock(holder, block, position)
            }
            EditorBlock.TYPE_WARNING -> {
                setupWarningBlock(holder, block, position)
            }
            EditorBlock.TYPE_DELIMITER -> {
                setupDelimiterBlock(holder, block, position)
            }
        }
        
        // Setup common buttons
        setupCommonButtons(holder, block, position)
    }

    override fun getItemCount(): Int = blocks.size

    private fun hideAllContainers(holder: BlockViewHolder) {
        holder.textInputLayout.visibility = View.GONE
        holder.quoteContainer.visibility = View.GONE
        holder.listContainer.visibility = View.GONE
        holder.codeInputLayout.visibility = View.GONE
        holder.warningContainer.visibility = View.GONE
        holder.delimiterView.visibility = View.GONE
        holder.headerLevelContainer.visibility = View.GONE
    }

    private fun setupParagraphBlock(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.textInputLayout.visibility = View.VISIBLE
        holder.blockEditText.setText(block.getText())
        
        setupTextWatcher(holder.blockEditText, position) { newText ->
            updateBlock(position, block.setText(newText))
        }
        
        setupSelectionListener(holder.blockEditText)
    }

    private fun setupHeaderBlock(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.textInputLayout.visibility = View.VISIBLE
        holder.headerLevelContainer.visibility = View.VISIBLE
        
        holder.blockEditText.setText(block.getText())
        
        // Update text size based on level
        val textSize = when (block.getLevel()) {
            1 -> 24f
            2 -> 20f
            3 -> 18f
            else -> 16f
        }
        holder.blockEditText.textSize = textSize
        
        // Update level buttons
        updateHeaderLevelButtons(holder, block.getLevel())
        
        setupTextWatcher(holder.blockEditText, position) { newText ->
            updateBlock(position, block.setText(newText))
        }
        
        setupHeaderLevelButtons(holder, block, position)
        setupSelectionListener(holder.blockEditText)
    }

    private fun setupQuoteBlock(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.quoteContainer.visibility = View.VISIBLE
        
        holder.quoteEditText.setText(block.getText())
        holder.quoteCaptionEditText.setText(block.getCaption())
        
        setupTextWatcher(holder.quoteEditText, position) { newText ->
            updateBlock(position, block.setText(newText))
        }
        
        setupTextWatcher(holder.quoteCaptionEditText, position) { newCaption ->
            updateBlock(position, block.setCaption(newCaption))
        }
        
        setupSelectionListener(holder.quoteEditText)
        setupSelectionListener(holder.quoteCaptionEditText)
    }

    private fun setupListBlock(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.listContainer.visibility = View.VISIBLE
        
        // Setup list style button
        val style = block.getStyle()
        holder.listStyleButton.text = if (style == "ordered") "1. Lista" else "• Lista"
        
        holder.listStyleButton.setOnClickListener {
            val newStyle = if (style == "ordered") "unordered" else "ordered"
            val updatedBlock = block.setStyle(newStyle)
            updateBlock(position, updatedBlock)
            notifyItemChanged(position)
        }
        
        // Setup list items RecyclerView
        val listAdapter = ListItemAdapter(
            items = block.getItems().toMutableList(),
            style = style,
            onItemChanged = { itemIndex, newText ->
                val items = block.getItems().toMutableList()
                if (itemIndex < items.size) {
                    items[itemIndex] = newText
                    updateBlock(position, block.setItems(items))
                }
            },
            onItemDeleted = { itemIndex ->
                val items = block.getItems().toMutableList()
                if (itemIndex < items.size && items.size > 1) {
                    items.removeAt(itemIndex)
                    updateBlock(position, block.setItems(items))
                    notifyItemChanged(position)
                }
            }
        )
        
        holder.listItemsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.listItemsRecyclerView.adapter = listAdapter
        
        holder.addListItemButton.setOnClickListener {
            val items = block.getItems().toMutableList()
            items.add("")
            updateBlock(position, block.setItems(items))
            notifyItemChanged(position)
        }
    }

    private fun setupCodeBlock(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.codeInputLayout.visibility = View.VISIBLE
        holder.codeEditText.setText(block.getCode())
        
        setupTextWatcher(holder.codeEditText, position) { newCode ->
            updateBlock(position, block.setCode(newCode))
        }
        
        setupSelectionListener(holder.codeEditText)
    }

    private fun setupWarningBlock(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.warningContainer.visibility = View.VISIBLE
        
        holder.warningTitleEditText.setText(block.getTitle())
        holder.warningMessageEditText.setText(block.getMessage())
        
        setupTextWatcher(holder.warningTitleEditText, position) { newTitle ->
            updateBlock(position, block.setTitle(newTitle))
        }
        
        setupTextWatcher(holder.warningMessageEditText, position) { newMessage ->
            updateBlock(position, block.setMessage(newMessage))
        }
        
        setupSelectionListener(holder.warningTitleEditText)
        setupSelectionListener(holder.warningMessageEditText)
    }

    private fun setupDelimiterBlock(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.delimiterView.visibility = View.VISIBLE
    }

    private fun setupCommonButtons(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.blockOptionsButton.setOnClickListener {
            // Show block type selection menu
            showBlockTypeMenu(holder, position)
        }
        
        holder.deleteBlockButton.setOnClickListener {
            if (blocks.size > 1) { // Don't allow deleting the last block
                onBlockDeleted(position)
            }
        }
    }

    private fun setupHeaderLevelButtons(holder: BlockViewHolder, block: EditorBlock, position: Int) {
        holder.headerLevel1Button.setOnClickListener {
            updateBlock(position, block.setLevel(1))
            notifyItemChanged(position)
        }
        
        holder.headerLevel2Button.setOnClickListener {
            updateBlock(position, block.setLevel(2))
            notifyItemChanged(position)
        }
        
        holder.headerLevel3Button.setOnClickListener {
            updateBlock(position, block.setLevel(3))
            notifyItemChanged(position)
        }
    }

    private fun updateHeaderLevelButtons(holder: BlockViewHolder, level: Int) {
        // Reset all buttons
        holder.headerLevel1Button.setBackgroundColor(holder.itemView.context.getColor(R.color.gray_light))
        holder.headerLevel2Button.setBackgroundColor(holder.itemView.context.getColor(R.color.gray_light))
        holder.headerLevel3Button.setBackgroundColor(holder.itemView.context.getColor(R.color.gray_light))
        
        // Highlight selected level
        when (level) {
            1 -> holder.headerLevel1Button.setBackgroundColor(holder.itemView.context.getColor(R.color.primary))
            2 -> holder.headerLevel2Button.setBackgroundColor(holder.itemView.context.getColor(R.color.primary))
            3 -> holder.headerLevel3Button.setBackgroundColor(holder.itemView.context.getColor(R.color.primary))
        }
    }

    private fun setupTextWatcher(editText: EditText, position: Int, onTextChanged: (String) -> Unit) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                onTextChanged(s?.toString() ?: "")
            }
        })
    }

    private fun setupSelectionListener(editText: EditText) {
        editText.setOnClickListener {
            onTextSelectionChanged(editText)
        }
        
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                onTextSelectionChanged(editText)
            }
        }
    }

    private fun showBlockTypeMenu(holder: BlockViewHolder, position: Int) {
        // This would show a popup menu with block type options
        // For now, we'll cycle through types
        val currentBlock = blocks[position]
        val newType = when (currentBlock.type) {
            EditorBlock.TYPE_PARAGRAPH -> EditorBlock.TYPE_HEADER
            EditorBlock.TYPE_HEADER -> EditorBlock.TYPE_QUOTE
            EditorBlock.TYPE_QUOTE -> EditorBlock.TYPE_LIST
            EditorBlock.TYPE_LIST -> EditorBlock.TYPE_CODE
            EditorBlock.TYPE_CODE -> EditorBlock.TYPE_WARNING
            EditorBlock.TYPE_WARNING -> EditorBlock.TYPE_DELIMITER
            EditorBlock.TYPE_DELIMITER -> EditorBlock.TYPE_PARAGRAPH
            else -> EditorBlock.TYPE_PARAGRAPH
        }
        
        val newBlock = when (newType) {
            EditorBlock.TYPE_PARAGRAPH -> EditorBlock.createParagraph(currentBlock.getText())
            EditorBlock.TYPE_HEADER -> EditorBlock.createHeader(currentBlock.getText())
            EditorBlock.TYPE_QUOTE -> EditorBlock.createQuote(currentBlock.getText())
            EditorBlock.TYPE_LIST -> EditorBlock.createList(listOf(currentBlock.getText()))
            EditorBlock.TYPE_CODE -> EditorBlock.createCode(currentBlock.getText())
            EditorBlock.TYPE_WARNING -> EditorBlock.createWarning(currentBlock.getText())
            EditorBlock.TYPE_DELIMITER -> EditorBlock.createDelimiter()
            else -> EditorBlock.createParagraph()
        }
        
        updateBlock(position, newBlock)
        notifyItemChanged(position)
    }

    private fun updateBlock(position: Int, newBlock: EditorBlock) {
        if (position >= 0 && position < blocks.size) {
            blocks[position] = newBlock
            onBlockChanged(position, newBlock)
        }
    }

    private fun getBlockTypeDisplayName(type: String): String {
        return when (type) {
            EditorBlock.TYPE_PARAGRAPH -> "Parágrafo"
            EditorBlock.TYPE_HEADER -> "Cabeçalho"
            EditorBlock.TYPE_QUOTE -> "Citação"
            EditorBlock.TYPE_LIST -> "Lista"
            EditorBlock.TYPE_CODE -> "Código"
            EditorBlock.TYPE_WARNING -> "Aviso"
            EditorBlock.TYPE_DELIMITER -> "Delimitador"
            else -> "Desconhecido"
        }
    }

    fun updateBlocks(newBlocks: List<EditorBlock>) {
        blocks.clear()
        blocks.addAll(newBlocks)
        notifyDataSetChanged()
    }

    fun addBlock(block: EditorBlock, position: Int = blocks.size) {
        blocks.add(position, block)
        notifyItemInserted(position)
    }

    fun removeBlock(position: Int) {
        if (position >= 0 && position < blocks.size && blocks.size > 1) {
            blocks.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getBlocks(): List<EditorBlock> = blocks.toList()

    fun getCurrentFocusedEditText(): EditText? {
        // This would need to be implemented to track the currently focused EditText
        return null
    }
}
