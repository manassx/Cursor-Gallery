package com.runanywhere.startup_hackathon20.data.repository

import android.content.Context
import android.net.Uri
import com.runanywhere.startup_hackathon20.data.local.GalleryDao
import com.runanywhere.startup_hackathon20.data.local.entities.Gallery
import com.runanywhere.startup_hackathon20.data.local.entities.GalleryImage
import com.runanywhere.startup_hackathon20.data.local.entities.GalleryWithImages
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class GalleryRepository(
    private val galleryDao: GalleryDao,
    private val context: Context
) {

    fun getAllGalleries(): Flow<List<Gallery>> {
        return galleryDao.getAllGalleries()
    }

    fun getAllGalleriesWithImages(): Flow<List<GalleryWithImages>> {
        return galleryDao.getAllGalleriesWithImages()
    }

    suspend fun getGalleryById(id: Long): Gallery? {
        return galleryDao.getGalleryById(id)
    }

    suspend fun getGalleryWithImages(id: Long): GalleryWithImages? {
        return galleryDao.getGalleryWithImages(id)
    }

    private fun copyImageToInternalStorage(uri: Uri, galleryId: Long, index: Int): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Create gallery directory
            val galleryDir = File(context.filesDir, "galleries/$galleryId")
            if (!galleryDir.exists()) {
                galleryDir.mkdirs()
            }

            // Create unique filename
            val fileName = "image_${System.currentTimeMillis()}_$index.jpg"
            val outputFile = File(galleryDir, fileName)

            // Copy file
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createGallery(name: String, description: String, imageUris: List<String>): Long {
        val gallery = Gallery(
            name = name,
            description = description,
            imageCount = imageUris.size
        )
        val galleryId = galleryDao.insertGallery(gallery)

        // Copy images to internal storage
        val images = imageUris.mapIndexedNotNull { index, uriString ->
            val uri = Uri.parse(uriString)
            val savedPath = copyImageToInternalStorage(uri, galleryId, index)

            savedPath?.let {
                GalleryImage(
                    galleryId = galleryId,
                    imagePath = it, // Store file path instead of URI
                    order = index
                )
            }
        }

        if (images.isNotEmpty()) {
            galleryDao.insertImages(images)
        }

        return galleryId
    }

    suspend fun updateGallery(gallery: Gallery) {
        galleryDao.updateGallery(gallery)
    }

    suspend fun deleteGallery(gallery: Gallery) {
        // Delete gallery images from storage
        val galleryDir = File(context.filesDir, "galleries/${gallery.id}")
        if (galleryDir.exists()) {
            galleryDir.deleteRecursively()
        }

        galleryDao.deleteGallery(gallery)
    }

    suspend fun addImagesToGallery(galleryId: Long, imageUris: List<String>) {
        val currentImages = galleryDao.getImagesForGallery(galleryId)
        val startOrder = currentImages.maxOfOrNull { it.order }?.plus(1) ?: 0

        val newImages = imageUris.mapIndexedNotNull { index, uriString ->
            val uri = Uri.parse(uriString)
            val savedPath = copyImageToInternalStorage(uri, galleryId, startOrder + index)

            savedPath?.let {
                GalleryImage(
                    galleryId = galleryId,
                    imagePath = it,
                    order = startOrder + index
                )
            }
        }

        if (newImages.isNotEmpty()) {
            galleryDao.insertImages(newImages)
        }

        // Update image count
        val updatedCount = galleryDao.getImageCount(galleryId)
        val gallery = galleryDao.getGalleryById(galleryId)
        gallery?.let {
            galleryDao.updateGallery(it.copy(imageCount = updatedCount))
        }
    }

    suspend fun deleteImage(image: GalleryImage) {
        // Delete image file
        val file = File(image.imagePath)
        if (file.exists()) {
            file.delete()
        }

        galleryDao.deleteImage(image)

        // Update image count
        val gallery = galleryDao.getGalleryById(image.galleryId)
        gallery?.let {
            val updatedCount = galleryDao.getImageCount(image.galleryId)
            galleryDao.updateGallery(it.copy(imageCount = updatedCount))
        }
    }
}
