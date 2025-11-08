package com.runanywhere.startup_hackathon20.data.repository

import android.content.Context
import com.runanywhere.startup_hackathon20.data.remote.NetworkRepository
import com.runanywhere.startup_hackathon20.data.remote.models.GalleryDetailResponse
import com.runanywhere.startup_hackathon20.data.remote.models.GalleryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class CloudGalleryRepository(private val context: Context) {

    private val networkRepository = NetworkRepository(context)
    private val prefs = context.getSharedPreferences("cloud_sync", Context.MODE_PRIVATE)

    /**
     * Get all galleries (metadata only)
     */
    suspend fun getGalleries(): Result<List<GalleryResponse>> {
        return networkRepository.getGalleries()
    }

    /**
     * Get gallery with images - downloads and caches images
     */
    suspend fun getGalleryWithImages(
        galleryId: String,
        onProgress: (Int, Int) -> Unit
    ): Result<GalleryDetailResponse> = withContext(Dispatchers.IO) {
        try {
            // Fetch gallery details from backend
            val result = networkRepository.getGallery(galleryId)

            if (result.isFailure) {
                return@withContext Result.failure(
                    result.exceptionOrNull() ?: Exception("Failed to fetch gallery")
                )
            }

            val gallery = result.getOrThrow()
            val galleryDir = File(context.filesDir, "galleries/$galleryId")
            galleryDir.mkdirs()

            // Check which images are already cached
            val cachedImages = galleryDir.listFiles()?.map { it.name }?.toSet() ?: emptySet()

            // Download only new images
            val imagesToDownload = gallery.images.filter { image ->
                val fileName = image.url.substringAfterLast("/").substringBefore("?")
                !cachedImages.contains(fileName)
            }

            // Download images
            imagesToDownload.forEachIndexed { index, image ->
                try {
                    val fileName = image.url.substringAfterLast("/").substringBefore("?")
                    val file = File(galleryDir, fileName)

                    if (!file.exists()) {
                        val url = URL(image.url)
                        val connection = url.openConnection()
                        connection.connect()

                        connection.getInputStream().use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }

                    onProgress(index + 1, imagesToDownload.size)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Save sync timestamp
            prefs.edit().putLong("sync_$galleryId", System.currentTimeMillis()).apply()

            Result.success(gallery)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get cached image file path
     */
    fun getCachedImagePath(galleryId: String, imageUrl: String): String? {
        val fileName = imageUrl.substringAfterLast("/").substringBefore("?")
        val file = File(context.filesDir, "galleries/$galleryId/$fileName")
        return if (file.exists()) "file://${file.absolutePath}" else null
    }

    /**
     * Check if gallery is cached
     */
    fun isGalleryCached(galleryId: String): Boolean {
        val galleryDir = File(context.filesDir, "galleries/$galleryId")
        return galleryDir.exists() && galleryDir.listFiles()?.isNotEmpty() == true
    }

    /**
     * Clear cache for gallery
     */
    fun clearGalleryCache(galleryId: String) {
        val galleryDir = File(context.filesDir, "galleries/$galleryId")
        galleryDir.deleteRecursively()
        prefs.edit().remove("sync_$galleryId").apply()
    }
}
