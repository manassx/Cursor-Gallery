package com.runanywhere.startup_hackathon20.data.local

import androidx.room.*
import com.runanywhere.startup_hackathon20.data.local.entities.Gallery
import com.runanywhere.startup_hackathon20.data.local.entities.GalleryImage
import com.runanywhere.startup_hackathon20.data.local.entities.GalleryWithImages
import kotlinx.coroutines.flow.Flow

@Dao
interface GalleryDao {
    // Gallery operations
    @Query("SELECT * FROM galleries ORDER BY createdAt DESC")
    fun getAllGalleries(): Flow<List<Gallery>>

    @Query("SELECT * FROM galleries WHERE id = :galleryId")
    suspend fun getGalleryById(galleryId: Long): Gallery?

    @Transaction
    @Query("SELECT * FROM galleries WHERE id = :galleryId")
    suspend fun getGalleryWithImages(galleryId: Long): GalleryWithImages?

    @Transaction
    @Query("SELECT * FROM galleries ORDER BY createdAt DESC")
    fun getAllGalleriesWithImages(): Flow<List<GalleryWithImages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGallery(gallery: Gallery): Long

    @Update
    suspend fun updateGallery(gallery: Gallery)

    @Delete
    suspend fun deleteGallery(gallery: Gallery)

    // Image operations
    @Query("SELECT * FROM gallery_images WHERE galleryId = :galleryId ORDER BY `order`")
    suspend fun getImagesForGallery(galleryId: Long): List<GalleryImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: GalleryImage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<GalleryImage>)

    @Delete
    suspend fun deleteImage(image: GalleryImage)

    @Query("DELETE FROM gallery_images WHERE galleryId = :galleryId")
    suspend fun deleteImagesForGallery(galleryId: Long)

    @Query("SELECT COUNT(*) FROM gallery_images WHERE galleryId = :galleryId")
    suspend fun getImageCount(galleryId: Long): Int
}
