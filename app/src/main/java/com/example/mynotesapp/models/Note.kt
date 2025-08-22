package com.example.mynotesapp.models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Note(
    val id: Int = 0,
    val title: String,
    val content: String, // JSON no formato EditorJS
    val tags: String, // Tags separadas por vírgula
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getTagsList(): List<String> {
        return if (tags.isBlank()) {
            emptyList()
        } else {
            tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    fun setTagsList(tagsList: List<String>): Note {
        return this.copy(tags = tagsList.joinToString(", "))
    }

    fun getContentAsBlocks(): List<EditorBlock> {
        return try {
            if (content.isBlank()) {
                listOf(EditorBlock("paragraph", mapOf("text" to "")))
            } else {
                val gson = Gson()
                val editorData = gson.fromJson(content, EditorJSData::class.java)
                editorData.blocks
            }
        } catch (e: Exception) {
            // Se houver erro no parsing, retorna um bloco padrão
            listOf(EditorBlock("paragraph", mapOf("text" to content)))
        }
    }

    fun setContentFromBlocks(blocks: List<EditorBlock>): Note {
        val editorData = EditorJSData(
            time = System.currentTimeMillis(),
            blocks = blocks,
            version = "2.28.2"
        )
        val gson = Gson()
        return this.copy(content = gson.toJson(editorData))
    }

    fun isValid(): Boolean {
        return title.isNotBlank()
    }

    fun matchesSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return title.lowercase().contains(lowerQuery) ||
                getTagsList().any { it.lowercase().contains(lowerQuery) } ||
                getPlainTextContent().lowercase().contains(lowerQuery)
    }

    private fun getPlainTextContent(): String {
        return try {
            getContentAsBlocks().joinToString(" ") { block ->
                when (val text = block.data["text"]) {
                    is String -> text
                    else -> ""
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}

data class EditorJSData(
    val time: Long,
    val blocks: List<EditorBlock>,
    val version: String
)
