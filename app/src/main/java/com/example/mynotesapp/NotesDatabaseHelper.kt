package com.example.mynotesapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.mynotesapp.models.Note

class NotesDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 1
        
        // Table name
        private const val TABLE_NOTES = "notes"
        
        // Column names
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_CONTENT = "content"
        private const val COLUMN_TAGS = "tags"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
        
        private const val TAG = "NotesDatabaseHelper"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        try {
            val createTableQuery = """
                CREATE TABLE $TABLE_NOTES (
                    $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TITLE TEXT NOT NULL,
                    $COLUMN_CONTENT TEXT NOT NULL,
                    $COLUMN_TAGS TEXT,
                    $COLUMN_CREATED_AT INTEGER NOT NULL,
                    $COLUMN_UPDATED_AT INTEGER NOT NULL
                )
            """.trimIndent()
            
            db?.execSQL(createTableQuery)
            Log.d(TAG, "Table $TABLE_NOTES created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating table: ${e.message}", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
            onCreate(db)
            Log.d(TAG, "Database upgraded from version $oldVersion to $newVersion")
        } catch (e: Exception) {
            Log.e(TAG, "Error upgrading database: ${e.message}", e)
        }
    }

    fun insertNote(note: Note): Long {
        val db = writableDatabase
        var result: Long = -1
        
        try {
            val values = ContentValues().apply {
                put(COLUMN_TITLE, note.title)
                put(COLUMN_CONTENT, note.content)
                put(COLUMN_TAGS, note.tags)
                put(COLUMN_CREATED_AT, note.createdAt)
                put(COLUMN_UPDATED_AT, note.updatedAt)
            }
            
            result = db.insert(TABLE_NOTES, null, values)
            Log.d(TAG, "Note inserted with ID: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting note: ${e.message}", e)
        } finally {
            db.close()
        }
        
        return result
    }

    fun updateNote(note: Note): Int {
        val db = writableDatabase
        var result = 0
        
        try {
            val values = ContentValues().apply {
                put(COLUMN_TITLE, note.title)
                put(COLUMN_CONTENT, note.content)
                put(COLUMN_TAGS, note.tags)
                put(COLUMN_UPDATED_AT, System.currentTimeMillis())
            }
            
            result = db.update(
                TABLE_NOTES,
                values,
                "$COLUMN_ID = ?",
                arrayOf(note.id.toString())
            )
            Log.d(TAG, "Note updated, rows affected: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note: ${e.message}", e)
        } finally {
            db.close()
        }
        
        return result
    }

    fun deleteNote(noteId: Int): Int {
        val db = writableDatabase
        var result = 0
        
        try {
            result = db.delete(
                TABLE_NOTES,
                "$COLUMN_ID = ?",
                arrayOf(noteId.toString())
            )
            Log.d(TAG, "Note deleted, rows affected: $result")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note: ${e.message}", e)
        } finally {
            db.close()
        }
        
        return result
    }

    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        var cursor: Cursor? = null
        
        try {
            cursor = db.query(
                TABLE_NOTES,
                null,
                null,
                null,
                null,
                null,
                "$COLUMN_UPDATED_AT DESC"
            )
            
            if (cursor.moveToFirst()) {
                do {
                    val note = cursorToNote(cursor)
                    notes.add(note)
                } while (cursor.moveToNext())
            }
            
            Log.d(TAG, "Retrieved ${notes.size} notes from database")
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving notes: ${e.message}", e)
        } finally {
            cursor?.close()
            db.close()
        }
        
        return notes
    }

    fun getNoteById(noteId: Int): Note? {
        val db = readableDatabase
        var cursor: Cursor? = null
        var note: Note? = null
        
        try {
            cursor = db.query(
                TABLE_NOTES,
                null,
                "$COLUMN_ID = ?",
                arrayOf(noteId.toString()),
                null,
                null,
                null
            )
            
            if (cursor.moveToFirst()) {
                note = cursorToNote(cursor)
                Log.d(TAG, "Retrieved note with ID: $noteId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving note by ID: ${e.message}", e)
        } finally {
            cursor?.close()
            db.close()
        }
        
        return note
    }

    fun searchNotes(query: String): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        var cursor: Cursor? = null
        
        try {
            val searchQuery = "%$query%"
            cursor = db.query(
                TABLE_NOTES,
                null,
                "$COLUMN_TITLE LIKE ? OR $COLUMN_TAGS LIKE ? OR $COLUMN_CONTENT LIKE ?",
                arrayOf(searchQuery, searchQuery, searchQuery),
                null,
                null,
                "$COLUMN_UPDATED_AT DESC"
            )
            
            if (cursor.moveToFirst()) {
                do {
                    val note = cursorToNote(cursor)
                    notes.add(note)
                } while (cursor.moveToNext())
            }
            
            Log.d(TAG, "Search for '$query' returned ${notes.size} notes")
        } catch (e: Exception) {
            Log.e(TAG, "Error searching notes: ${e.message}", e)
        } finally {
            cursor?.close()
            db.close()
        }
        
        return notes
    }

    fun getNotesCount(): Int {
        val db = readableDatabase
        var cursor: Cursor? = null
        var count = 0
        
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NOTES", null)
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notes count: ${e.message}", e)
        } finally {
            cursor?.close()
            db.close()
        }
        
        return count
    }

    private fun cursorToNote(cursor: Cursor): Note {
        return Note(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
            tags = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAGS)) ?: "",
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT))
        )
    }
}
