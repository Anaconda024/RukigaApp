package com.example.rukigaapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "dictionaries",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["categoryId"])])
data class Diction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryId: Int,
    val rukiga: String,
    val english: String,
    val pronunciation: String? = null,
    val description: String? = null,
    val deleted: Boolean = false,
)
