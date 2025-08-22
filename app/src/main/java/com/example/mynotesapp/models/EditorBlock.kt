package com.example.mynotesapp.models

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class EditorBlock(
    val type: String,
    val data: Map<String, Any>
) {
    companion object {
        const val TYPE_PARAGRAPH = "paragraph"
        const val TYPE_HEADER = "header"
        const val TYPE_QUOTE = "quote"
        const val TYPE_LIST = "list"
        const val TYPE_CODE = "code"
        const val TYPE_DELIMITER = "delimiter"
        const val TYPE_WARNING = "warning"

        fun createParagraph(text: String = ""): EditorBlock {
            return EditorBlock(TYPE_PARAGRAPH, mapOf("text" to text))
        }

        fun createHeader(text: String = "", level: Int = 2): EditorBlock {
            return EditorBlock(TYPE_HEADER, mapOf(
                "text" to text,
                "level" to level
            ))
        }

        fun createQuote(text: String = "", caption: String = ""): EditorBlock {
            return EditorBlock(TYPE_QUOTE, mapOf(
                "text" to text,
                "caption" to caption,
                "alignment" to "left"
            ))
        }

        fun createList(items: List<String> = listOf(""), style: String = "unordered"): EditorBlock {
            return EditorBlock(TYPE_LIST, mapOf(
                "style" to style,
                "items" to items
            ))
        }

        fun createCode(code: String = ""): EditorBlock {
            return EditorBlock(TYPE_CODE, mapOf("code" to code))
        }

        fun createDelimiter(): EditorBlock {
            return EditorBlock(TYPE_DELIMITER, emptyMap())
        }

        fun createWarning(title: String = "", message: String = ""): EditorBlock {
            return EditorBlock(TYPE_WARNING, mapOf(
                "title" to title,
                "message" to message
            ))
        }
    }

    fun getText(): String {
        return when (val text = data["text"]) {
            is String -> text
            else -> ""
        }
    }

    fun setText(newText: String): EditorBlock {
        val newData = data.toMutableMap()
        newData["text"] = newText
        return this.copy(data = newData)
    }

    fun getLevel(): Int {
        return when (val level = data["level"]) {
            is Int -> level
            is Double -> level.toInt()
            is String -> level.toIntOrNull() ?: 2
            else -> 2
        }
    }

    fun setLevel(newLevel: Int): EditorBlock {
        val newData = data.toMutableMap()
        newData["level"] = newLevel
        return this.copy(data = newData)
    }

    fun getItems(): List<String> {
        return when (val items = data["items"]) {
            is List<*> -> items.filterIsInstance<String>()
            else -> listOf("")
        }
    }

    fun setItems(newItems: List<String>): EditorBlock {
        val newData = data.toMutableMap()
        newData["items"] = newItems
        return this.copy(data = newData)
    }

    fun getCode(): String {
        return when (val code = data["code"]) {
            is String -> code
            else -> ""
        }
    }

    fun setCode(newCode: String): EditorBlock {
        val newData = data.toMutableMap()
        newData["code"] = newCode
        return this.copy(data = newData)
    }

    fun getCaption(): String {
        return when (val caption = data["caption"]) {
            is String -> caption
            else -> ""
        }
    }

    fun setCaption(newCaption: String): EditorBlock {
        val newData = data.toMutableMap()
        newData["caption"] = newCaption
        return this.copy(data = newData)
    }

    fun getTitle(): String {
        return when (val title = data["title"]) {
            is String -> title
            else -> ""
        }
    }

    fun setTitle(newTitle: String): EditorBlock {
        val newData = data.toMutableMap()
        newData["title"] = newTitle
        return this.copy(data = newData)
    }

    fun getMessage(): String {
        return when (val message = data["message"]) {
            is String -> message
            else -> ""
        }
    }

    fun setMessage(newMessage: String): EditorBlock {
        val newData = data.toMutableMap()
        newData["message"] = newMessage
        return this.copy(data = newData)
    }

    fun getStyle(): String {
        return when (val style = data["style"]) {
            is String -> style
            else -> "unordered"
        }
    }

    fun setStyle(newStyle: String): EditorBlock {
        val newData = data.toMutableMap()
        newData["style"] = newStyle
        return this.copy(data = newData)
    }

    fun isEmpty(): Boolean {
        return when (type) {
            TYPE_PARAGRAPH, TYPE_HEADER -> getText().isBlank()
            TYPE_QUOTE -> getText().isBlank() && getCaption().isBlank()
            TYPE_LIST -> getItems().all { it.isBlank() }
            TYPE_CODE -> getCode().isBlank()
            TYPE_WARNING -> getTitle().isBlank() && getMessage().isBlank()
            TYPE_DELIMITER -> false
            else -> true
        }
    }

    fun getDisplayText(): String {
        return when (type) {
            TYPE_PARAGRAPH -> getText()
            TYPE_HEADER -> "H${getLevel()}: ${getText()}"
            TYPE_QUOTE -> "\"${getText()}\""
            TYPE_LIST -> getItems().joinToString(", ")
            TYPE_CODE -> "Code: ${getCode().take(50)}${if (getCode().length > 50) "..." else ""}"
            TYPE_WARNING -> "${getTitle()}: ${getMessage()}"
            TYPE_DELIMITER -> "---"
            else -> getText()
        }
    }
}
