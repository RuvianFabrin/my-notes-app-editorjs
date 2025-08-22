package com.example.mynotesapp

import android.content.Context
import android.util.Log
import com.example.mynotesapp.models.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(context: Context) {
    
    private val dbHelper = NotesDatabaseHelper(context)
    
    companion object {
        private const val TAG = "NoteRepository"
        
        @Volatile
        private var INSTANCE: NoteRepository? = null
        
        fun getInstance(context: Context): NoteRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NoteRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    suspend fun insertNote(note: Note): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (!note.isValid()) {
                return@withContext Result.failure(Exception("Nota inválida: título não pode estar vazio"))
            }
            
            val id = dbHelper.insertNote(note)
            if (id > 0) {
                Log.d(TAG, "Note inserted successfully with ID: $id")
                Result.success(id)
            } else {
                Result.failure(Exception("Falha ao inserir nota no banco de dados"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting note: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateNote(note: Note): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!note.isValid()) {
                return@withContext Result.failure(Exception("Nota inválida: título não pode estar vazio"))
            }
            
            val updatedNote = note.copy(updatedAt = System.currentTimeMillis())
            val rowsAffected = dbHelper.updateNote(updatedNote)
            
            if (rowsAffected > 0) {
                Log.d(TAG, "Note updated successfully")
                Result.success(true)
            } else {
                Result.failure(Exception("Nenhuma nota foi atualizada"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(noteId: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val rowsAffected = dbHelper.deleteNote(noteId)
            if (rowsAffected > 0) {
                Log.d(TAG, "Note deleted successfully")
                Result.success(true)
            } else {
                Result.failure(Exception("Nenhuma nota foi excluída"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllNotes(): Result<List<Note>> = withContext(Dispatchers.IO) {
        try {
            val notes = dbHelper.getAllNotes()
            Log.d(TAG, "Retrieved ${notes.size} notes")
            Result.success(notes)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving all notes: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getNoteById(noteId: Int): Result<Note?> = withContext(Dispatchers.IO) {
        try {
            val note = dbHelper.getNoteById(noteId)
            if (note != null) {
                Log.d(TAG, "Retrieved note with ID: $noteId")
                Result.success(note)
            } else {
                Result.failure(Exception("Nota não encontrada"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving note by ID: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun searchNotes(query: String): Result<List<Note>> = withContext(Dispatchers.IO) {
        try {
            val notes = if (query.isBlank()) {
                dbHelper.getAllNotes()
            } else {
                dbHelper.searchNotes(query)
            }
            Log.d(TAG, "Search for '$query' returned ${notes.size} notes")
            Result.success(notes)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching notes: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getNotesCount(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val count = dbHelper.getNotesCount()
            Log.d(TAG, "Total notes count: $count")
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notes count: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun sortNotes(notes: List<Note>, sortBy: SortOption): List<Note> {
        return try {
            when (sortBy) {
                SortOption.TITLE_ASC -> notes.sortedBy { it.title.lowercase() }
                SortOption.TITLE_DESC -> notes.sortedByDescending { it.title.lowercase() }
                SortOption.DATE_ASC -> notes.sortedBy { it.updatedAt }
                SortOption.DATE_DESC -> notes.sortedByDescending { it.updatedAt }
                SortOption.CREATED_ASC -> notes.sortedBy { it.createdAt }
                SortOption.CREATED_DESC -> notes.sortedByDescending { it.createdAt }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sorting notes: ${e.message}", e)
            notes // Retorna a lista original em caso de erro
        }
    }

    suspend fun exportNoteAsJson(noteId: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val note = dbHelper.getNoteById(noteId)
            if (note != null) {
                Result.success(note.content)
            } else {
                Result.failure(Exception("Nota não encontrada para exportação"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting note as JSON: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun importNoteFromJson(title: String, jsonContent: String, tags: String = ""): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val note = Note(
                title = title,
                content = jsonContent,
                tags = tags,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            insertNote(note)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing note from JSON: ${e.message}", e)
            Result.failure(e)
        }
    }

    enum class SortOption {
        TITLE_ASC,
        TITLE_DESC,
        DATE_ASC,
        DATE_DESC,
        CREATED_ASC,
        CREATED_DESC
    }
}
