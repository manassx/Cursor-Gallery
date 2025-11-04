package com.runanywhere.startup_hackathon20.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gallery_images",
    foreignKeys = [
        ForeignKey(
            entity = Gallery::class,
            parentColumns = ["id"],
            childColumns = ["galleryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("galleryId")]
)
data class GalleryImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val galleryId: Long,
    val imagePath: String,
    val order: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
