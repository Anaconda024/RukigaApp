package com.example.rukigaapp.services.utils

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.example.rukigaapp.data.KigaWord
import com.example.rukigaapp.services.dao.KigaWordDao
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for importing dictionary data from the asset database
 * into the Room database.
 */
object DatabaseImporter {

    private const val TAG = "DatabaseImporter"
    private const val ASSET_DB_NAME = "rukiga_dictionary.db"
    private const val BATCH_SIZE = 500 // Import in batches for efficiency

    /**
     * Imports all words from the asset database into Room.
     *
     * @param context Application context
     * @param kigaWordDao DAO for inserting words
     * @return Number of words imported, or -1 on error
     */
    suspend fun importDictionaryFromAssets(
        context: Context,
        kigaWordDao: KigaWordDao
    ): Int {
        Log.d(TAG, "Starting dictionary import from assets...")

        var database: SQLiteDatabase? = null
        var cursor: Cursor? = null
        var totalImported = 0

        try {
            // Step 1: Copy database from assets to internal storage
            val dbFile = copyDatabaseFromAssets(context)

            if (dbFile == null || !dbFile.exists()) {
                Log.e(TAG, "Failed to copy database from assets")
                return -1
            }

            // Step 2: Open the copied database
            database = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            // Step 3: Query all words from the dictionary table
            cursor = database.rawQuery(
                "SELECT id, word, part_of_speech, definition, see, example, " +
                        "is_bookmarked, bookmark_cat_id, is_deleted, date_deleted " +
                        "FROM dictionary",
                null
            )

            val totalCount = cursor.count
            Log.d(TAG, "Found $totalCount words to import")

            // Step 4: Import in batches for better performance
            val batch = mutableListOf<KigaWord>()

            if (cursor.moveToFirst()) {
                do {
                    val word = KigaWord(
                        id = 0, // Let Room auto-generate IDs
                        word = cursor.getString(1) ?: "",
                        partOfSpeech = cursor.getString(2),
                        definition = cursor.getString(3),
                        see = cursor.getString(4),
                        example = cursor.getString(5),
                        isBookmarked = cursor.getInt(6) == 1,
                        bookmarkCatId = cursor.getInt(7),
                        isDeleted = cursor.getInt(8) == 1,
                        dateDeleted = cursor.getString(9)
                    )

                    batch.add(word)

                    // Insert batch when it reaches BATCH_SIZE
                    if (batch.size >= BATCH_SIZE) {
                        kigaWordDao.insertAll(batch)
                        totalImported += batch.size
                        batch.clear()
                        Log.d(TAG, "Imported $totalImported / $totalCount words...")
                    }
                } while (cursor.moveToNext())
            }

            // Insert remaining words
            if (batch.isNotEmpty()) {
                kigaWordDao.insertAll(batch)
                totalImported += batch.size
            }

            Log.d(TAG, "Dictionary import completed: $totalImported words imported")

            // Step 5: Clean up the temporary database file
            dbFile.delete()

            return totalImported

        } catch (e: Exception) {
            Log.e(TAG, "Error importing dictionary", e)
            return -1
        } finally {
            cursor?.close()
            database?.close()
        }
    }

    /**
     * Copies the database file from assets to internal storage
     * so we can read it with SQLiteDatabase.
     */
    private fun copyDatabaseFromAssets(context: Context): File? {
        return try {
            val assetManager = context.assets
            val inputStream = assetManager.open(ASSET_DB_NAME)

            // Create a temporary file in internal storage
            val outputFile = File(context.cacheDir, "temp_$ASSET_DB_NAME")

            if (outputFile.exists()) {
                outputFile.delete()
            }

            val outputStream = FileOutputStream(outputFile)

            // Copy bytes
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d(TAG, "Database copied to: ${outputFile.absolutePath}")
            outputFile

        } catch (e: Exception) {
            Log.e(TAG, "Error copying database from assets", e)
            null
        }
    }
}