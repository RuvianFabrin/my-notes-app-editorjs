package com.example.mynotesapp.utils

import android.util.Log
import com.example.mynotesapp.models.EditorBlock
import com.example.mynotesapp.models.EditorJSData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class EditorJSJsonConverter {
    
    companion object {
        private const val TAG = "EditorJSJsonConverter"
        private val gson = Gson()
        
        fun blocksToJson(blocks: List<EditorBlock>): String {
            return try {
                val editorData = EditorJSData(
                    time = System.currentTimeMillis(),
                    blocks = blocks,
                    version = "2.28.2"
                )
                gson.toJson(editorData)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting blocks to JSON: ${e.message}", e)
                createEmptyEditorJson()
            }
        }
        
        fun jsonToBlocks(json: String): List<EditorBlock> {
            return try {
                if (json.isBlank()) {
                    return listOf(EditorBlock.createParagraph())
                }
                
                val editorData = gson.fromJson(json, EditorJSData::class.java)
                
                if (editorData.blocks.isEmpty()) {
                    listOf(EditorBlock.createParagraph())
                } else {
                    editorData.blocks
                }
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "JSON syntax error: ${e.message}", e)
                // Tenta interpretar como texto simples
                parseAsPlainText(json)
            } catch (e: Exception) {
                Log.e(TAG, "Error converting JSON to blocks: ${e.message}", e)
                listOf(EditorBlock.createParagraph(json))
            }
        }
        
        fun validateEditorJson(json: String): Boolean {
            return try {
                if (json.isBlank()) return false
                
                val editorData = gson.fromJson(json, EditorJSData::class.java)
                editorData.blocks.isNotEmpty()
            } catch (e: Exception) {
                Log.w(TAG, "Invalid EditorJS JSON: ${e.message}")
                false
            }
        }
        
        fun createEmptyEditorJson(): String {
            val editorData = EditorJSData(
                time = System.currentTimeMillis(),
                blocks = listOf(EditorBlock.createParagraph()),
                version = "2.28.2"
            )
            return gson.toJson(editorData)
        }
        
        fun addBlockToJson(json: String, block: EditorBlock): String {
            return try {
                val blocks = jsonToBlocks(json).toMutableList()
                blocks.add(block)
                blocksToJson(blocks)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding block to JSON: ${e.message}", e)
                json
            }
        }
        
        fun removeBlockFromJson(json: String, blockIndex: Int): String {
            return try {
                val blocks = jsonToBlocks(json).toMutableList()
                if (blockIndex >= 0 && blockIndex < blocks.size) {
                    blocks.removeAt(blockIndex)
                    if (blocks.isEmpty()) {
                        blocks.add(EditorBlock.createParagraph())
                    }
                }
                blocksToJson(blocks)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing block from JSON: ${e.message}", e)
                json
            }
        }
        
        fun updateBlockInJson(json: String, blockIndex: Int, updatedBlock: EditorBlock): String {
            return try {
                val blocks = jsonToBlocks(json).toMutableList()
                if (blockIndex >= 0 && blockIndex < blocks.size) {
                    blocks[blockIndex] = updatedBlock
                }
                blocksToJson(blocks)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating block in JSON: ${e.message}", e)
                json
            }
        }
        
        fun moveBlock(json: String, fromIndex: Int, toIndex: Int): String {
            return try {
                val blocks = jsonToBlocks(json).toMutableList()
                if (fromIndex >= 0 && fromIndex < blocks.size && 
                    toIndex >= 0 && toIndex < blocks.size && 
                    fromIndex != toIndex) {
                    
                    val block = blocks.removeAt(fromIndex)
                    blocks.add(toIndex, block)
                }
                blocksToJson(blocks)
            } catch (e: Exception) {
                Log.e(TAG, "Error moving block in JSON: ${e.message}", e)
                json
            }
        }
        
        fun extractPlainText(json: String): String {
            return try {
                val blocks = jsonToBlocks(json)
                blocks.joinToString("\n\n") { block ->
                    when (block.type) {
                        EditorBlock.TYPE_PARAGRAPH -> block.getText()
                        EditorBlock.TYPE_HEADER -> "# ${block.getText()}"
                        EditorBlock.TYPE_QUOTE -> "\"${block.getText()}\""
                        EditorBlock.TYPE_LIST -> {
                            val items = block.getItems()
                            val style = block.getStyle()
                            items.mapIndexed { index, item ->
                                if (style == "ordered") "${index + 1}. $item" else "• $item"
                            }.joinToString("\n")
                        }
                        EditorBlock.TYPE_CODE -> "```\n${block.getCode()}\n```"
                        EditorBlock.TYPE_WARNING -> "${block.getTitle()}: ${block.getMessage()}"
                        EditorBlock.TYPE_DELIMITER -> "---"
                        else -> block.getText()
                    }
                }.trim()
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting plain text: ${e.message}", e)
                ""
            }
        }
        
        fun importFromPlainText(text: String): String {
            return try {
                val blocks = mutableListOf<EditorBlock>()
                val lines = text.split("\n")
                
                var currentParagraph = StringBuilder()
                
                for (line in lines) {
                    val trimmedLine = line.trim()
                    
                    when {
                        // Header
                        trimmedLine.startsWith("#") -> {
                            if (currentParagraph.isNotEmpty()) {
                                blocks.add(EditorBlock.createParagraph(currentParagraph.toString().trim()))
                                currentParagraph.clear()
                            }
                            val headerText = trimmedLine.removePrefix("#").trim()
                            blocks.add(EditorBlock.createHeader(headerText, 2))
                        }
                        
                        // Quote
                        trimmedLine.startsWith("\"") && trimmedLine.endsWith("\"") -> {
                            if (currentParagraph.isNotEmpty()) {
                                blocks.add(EditorBlock.createParagraph(currentParagraph.toString().trim()))
                                currentParagraph.clear()
                            }
                            val quoteText = trimmedLine.removeSurrounding("\"")
                            blocks.add(EditorBlock.createQuote(quoteText))
                        }
                        
                        // List item
                        trimmedLine.startsWith("•") || trimmedLine.startsWith("-") || 
                        trimmedLine.matches(Regex("^\\d+\\.\\s.*")) -> {
                            if (currentParagraph.isNotEmpty()) {
                                blocks.add(EditorBlock.createParagraph(currentParagraph.toString().trim()))
                                currentParagraph.clear()
                            }
                            val itemText = when {
                                trimmedLine.startsWith("•") -> trimmedLine.removePrefix("•").trim()
                                trimmedLine.startsWith("-") -> trimmedLine.removePrefix("-").trim()
                                else -> trimmedLine.replaceFirst(Regex("^\\d+\\.\\s"), "")
                            }
                            blocks.add(EditorBlock.createList(listOf(itemText)))
                        }
                        
                        // Delimiter
                        trimmedLine == "---" -> {
                            if (currentParagraph.isNotEmpty()) {
                                blocks.add(EditorBlock.createParagraph(currentParagraph.toString().trim()))
                                currentParagraph.clear()
                            }
                            blocks.add(EditorBlock.createDelimiter())
                        }
                        
                        // Empty line
                        trimmedLine.isEmpty() -> {
                            if (currentParagraph.isNotEmpty()) {
                                blocks.add(EditorBlock.createParagraph(currentParagraph.toString().trim()))
                                currentParagraph.clear()
                            }
                        }
                        
                        // Regular text
                        else -> {
                            if (currentParagraph.isNotEmpty()) {
                                currentParagraph.append(" ")
                            }
                            currentParagraph.append(trimmedLine)
                        }
                    }
                }
                
                // Add remaining paragraph
                if (currentParagraph.isNotEmpty()) {
                    blocks.add(EditorBlock.createParagraph(currentParagraph.toString().trim()))
                }
                
                if (blocks.isEmpty()) {
                    blocks.add(EditorBlock.createParagraph())
                }
                
                blocksToJson(blocks)
            } catch (e: Exception) {
                Log.e(TAG, "Error importing from plain text: ${e.message}", e)
                createEmptyEditorJson()
            }
        }
        
        private fun parseAsPlainText(text: String): List<EditorBlock> {
            return try {
                if (text.isBlank()) {
                    listOf(EditorBlock.createParagraph())
                } else {
                    listOf(EditorBlock.createParagraph(text))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing as plain text: ${e.message}", e)
                listOf(EditorBlock.createParagraph())
            }
        }
        
        fun getBlockCount(json: String): Int {
            return try {
                jsonToBlocks(json).size
            } catch (e: Exception) {
                Log.e(TAG, "Error getting block count: ${e.message}", e)
                0
            }
        }
        
        fun isValidBlockType(type: String): Boolean {
            return type in listOf(
                EditorBlock.TYPE_PARAGRAPH,
                EditorBlock.TYPE_HEADER,
                EditorBlock.TYPE_QUOTE,
                EditorBlock.TYPE_LIST,
                EditorBlock.TYPE_CODE,
                EditorBlock.TYPE_DELIMITER,
                EditorBlock.TYPE_WARNING
            )
        }
    }
}
