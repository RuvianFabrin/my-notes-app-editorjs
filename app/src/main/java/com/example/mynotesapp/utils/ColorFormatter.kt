package com.example.mynotesapp.utils

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.EditText

class ColorFormatter {
    
    companion object {
        private const val TAG = "ColorFormatter"
        
        // Cores predefinidas para texto
        val TEXT_COLORS = mapOf(
            "black" to Color.BLACK,
            "red" to Color.RED,
            "blue" to Color.BLUE,
            "green" to Color.GREEN,
            "purple" to Color.parseColor("#800080"),
            "orange" to Color.parseColor("#FFA500"),
            "brown" to Color.parseColor("#A52A2A"),
            "gray" to Color.GRAY
        )
        
        // Cores predefinidas para fundo
        val BACKGROUND_COLORS = mapOf(
            "yellow" to Color.YELLOW,
            "lightgreen" to Color.parseColor("#90EE90"),
            "lightblue" to Color.parseColor("#ADD8E6"),
            "pink" to Color.parseColor("#FFC0CB"),
            "orange" to Color.parseColor("#FFE4B5"),
            "lightgray" to Color.parseColor("#D3D3D3"),
            "cyan" to Color.parseColor("#E0FFFF"),
            "lavender" to Color.parseColor("#E6E6FA")
        )
        
        fun applyTextColor(editText: EditText, colorName: String): Boolean {
            return try {
                val color = TEXT_COLORS[colorName] ?: return false
                applyTextColor(editText, color)
            } catch (e: Exception) {
                Log.e(TAG, "Error applying text color: ${e.message}", e)
                false
            }
        }
        
        fun applyTextColor(editText: EditText, color: Int): Boolean {
            return try {
                val start = editText.selectionStart
                val end = editText.selectionEnd
                
                if (start == end) {
                    // Nenhum texto selecionado
                    return false
                }
                
                val spannable = editText.text as? Spannable ?: SpannableString(editText.text)
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                editText.setText(spannable)
                editText.setSelection(end) // Manter cursor no final da seleção
                
                Log.d(TAG, "Applied text color to selection from $start to $end")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error applying text color: ${e.message}", e)
                false
            }
        }
        
        fun applyBackgroundColor(editText: EditText, colorName: String): Boolean {
            return try {
                val color = BACKGROUND_COLORS[colorName] ?: return false
                applyBackgroundColor(editText, color)
            } catch (e: Exception) {
                Log.e(TAG, "Error applying background color: ${e.message}", e)
                false
            }
        }
        
        fun applyBackgroundColor(editText: EditText, color: Int): Boolean {
            return try {
                val start = editText.selectionStart
                val end = editText.selectionEnd
                
                if (start == end) {
                    // Nenhum texto selecionado
                    return false
                }
                
                val spannable = editText.text as? Spannable ?: SpannableString(editText.text)
                spannable.setSpan(
                    BackgroundColorSpan(color),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                editText.setText(spannable)
                editText.setSelection(end) // Manter cursor no final da seleção
                
                Log.d(TAG, "Applied background color to selection from $start to $end")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error applying background color: ${e.message}", e)
                false
            }
        }
        
        fun removeTextColor(editText: EditText): Boolean {
            return try {
                val start = editText.selectionStart
                val end = editText.selectionEnd
                
                if (start == end) {
                    return false
                }
                
                val spannable = editText.text as? Spannable ?: SpannableString(editText.text)
                val spans = spannable.getSpans(start, end, ForegroundColorSpan::class.java)
                
                for (span in spans) {
                    spannable.removeSpan(span)
                }
                
                editText.setText(spannable)
                editText.setSelection(end)
                
                Log.d(TAG, "Removed text color from selection from $start to $end")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error removing text color: ${e.message}", e)
                false
            }
        }
        
        fun removeBackgroundColor(editText: EditText): Boolean {
            return try {
                val start = editText.selectionStart
                val end = editText.selectionEnd
                
                if (start == end) {
                    return false
                }
                
                val spannable = editText.text as? Spannable ?: SpannableString(editText.text)
                val spans = spannable.getSpans(start, end, BackgroundColorSpan::class.java)
                
                for (span in spans) {
                    spannable.removeSpan(span)
                }
                
                editText.setText(spannable)
                editText.setSelection(end)
                
                Log.d(TAG, "Removed background color from selection from $start to $end")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error removing background color: ${e.message}", e)
                false
            }
        }
        
        fun removeAllFormatting(editText: EditText): Boolean {
            return try {
                val start = editText.selectionStart
                val end = editText.selectionEnd
                
                if (start == end) {
                    return false
                }
                
                val spannable = editText.text as? Spannable ?: SpannableString(editText.text)
                
                // Remove text color spans
                val textColorSpans = spannable.getSpans(start, end, ForegroundColorSpan::class.java)
                for (span in textColorSpans) {
                    spannable.removeSpan(span)
                }
                
                // Remove background color spans
                val backgroundColorSpans = spannable.getSpans(start, end, BackgroundColorSpan::class.java)
                for (span in backgroundColorSpans) {
                    spannable.removeSpan(span)
                }
                
                editText.setText(spannable)
                editText.setSelection(end)
                
                Log.d(TAG, "Removed all formatting from selection from $start to $end")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error removing all formatting: ${e.message}", e)
                false
            }
        }
        
        fun getFormattedText(editText: EditText): SpannableString {
            return try {
                SpannableString(editText.text)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting formatted text: ${e.message}", e)
                SpannableString("")
            }
        }
        
        fun setFormattedText(editText: EditText, spannableText: SpannableString) {
            try {
                editText.setText(spannableText)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting formatted text: ${e.message}", e)
                editText.setText(spannableText.toString())
            }
        }
        
        fun hasSelection(editText: EditText): Boolean {
            return editText.selectionStart != editText.selectionEnd
        }
        
        fun getSelectedText(editText: EditText): String {
            return try {
                val start = editText.selectionStart
                val end = editText.selectionEnd
                
                if (start == end) {
                    ""
                } else {
                    editText.text.substring(start, end)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting selected text: ${e.message}", e)
                ""
            }
        }
        
        fun selectWord(editText: EditText, position: Int): Boolean {
            return try {
                val text = editText.text.toString()
                if (position < 0 || position >= text.length) {
                    return false
                }
                
                var start = position
                var end = position
                
                // Encontrar início da palavra
                while (start > 0 && !Character.isWhitespace(text[start - 1])) {
                    start--
                }
                
                // Encontrar fim da palavra
                while (end < text.length && !Character.isWhitespace(text[end])) {
                    end++
                }
                
                if (start < end) {
                    editText.setSelection(start, end)
                    Log.d(TAG, "Selected word from $start to $end")
                    return true
                }
                
                false
            } catch (e: Exception) {
                Log.e(TAG, "Error selecting word: ${e.message}", e)
                false
            }
        }
        
        fun copyFormattingToClipboard(editText: EditText): String {
            return try {
                val spannable = editText.text as? Spannable ?: return ""
                val start = editText.selectionStart
                val end = editText.selectionEnd
                
                if (start == end) return ""
                
                val selectedText = spannable.subSequence(start, end) as SpannableString
                
                // Aqui você poderia implementar uma serialização mais complexa
                // Por simplicidade, retornamos apenas o texto
                selectedText.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Error copying formatting: ${e.message}", e)
                ""
            }
        }
        
        fun getAvailableTextColors(): List<Pair<String, Int>> {
            return TEXT_COLORS.map { (name, color) -> name to color }
        }
        
        fun getAvailableBackgroundColors(): List<Pair<String, Int>> {
            return BACKGROUND_COLORS.map { (name, color) -> name to color }
        }
        
        fun parseColorFromHex(hexColor: String): Int? {
            return try {
                Color.parseColor(hexColor)
            } catch (e: Exception) {
                Log.w(TAG, "Invalid hex color: $hexColor")
                null
            }
        }
        
        fun colorToHex(color: Int): String {
            return String.format("#%06X", 0xFFFFFF and color)
        }
    }
}
