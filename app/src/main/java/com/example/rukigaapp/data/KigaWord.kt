package com.example.rukigaapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a word in the Rukiga-English dictionary.
 * Maps to the 'libraries' table in the Room database.
 * Source data comes from the asset database 'dictionary' table.
 */
@Entity(tableName = "libraries")
data class KigaWord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "part_of_speech")
    val partOfSpeech: String?,

    @ColumnInfo(name = "definition")
    val definition: String?,

    @ColumnInfo(name = "see")
    val see: String?,

    @ColumnInfo(name = "example")
    val example: String?,

    @ColumnInfo(name = "is_bookmarked")
    val isBookmarked: Boolean = false,

    @ColumnInfo(name = "bookmark_cat_id")
    val bookmarkCatId: Int = 0,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "date_deleted")
    val dateDeleted: String? = null
)