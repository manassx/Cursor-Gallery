package com.runanywhere.startup_hackathon20.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class GalleryWithImages(
    @Embedded val gallery: Gallery,
    @Relation(
        parentColumn = "id",
        entityColumn = "galleryId"
    )
    val images: List<GalleryImage>
)
