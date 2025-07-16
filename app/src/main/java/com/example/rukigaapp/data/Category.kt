package com.example.rukigaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Consider making it non-nullable if autoGenerate is true
    val name: String,
    val description: String?,
    val deleted: Boolean = false,
)
