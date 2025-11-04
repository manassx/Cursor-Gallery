package com.runanywhere.startup_hackathon20.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "galleries")
data class Gallery(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val imageCount: Int = 0
)
