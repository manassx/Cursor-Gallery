package com.runanywhere.startup_hackathon20.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.remote.models.GalleryDetailResponse
import com.runanywhere.startup_hackathon20.data.remote.models.GalleryResponse
import com.runanywhere.startup_hackathon20.data.repository.CloudGalleryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CloudGalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CloudGalleryRepository(application.applicationContext)

    private val _galleries = MutableStateFlow<List<GalleryResponse>>(emptyList())
    val galleries: StateFlow<List<GalleryResponse>> = _galleries.asStateFlow()

    private val _currentGallery = MutableStateFlow<GalleryDetailResponse?>(null)
    val currentGallery: StateFlow<GalleryDetailResponse?> = _currentGallery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0 to 0) // current to total
    val downloadProgress: StateFlow<Pair<Int, Int>> = _downloadProgress.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadGalleries() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.getGalleries()

                if (result.isSuccess) {
                    _galleries.value = result.getOrThrow()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load galleries"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadGalleryDetail(galleryId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _downloadProgress.value = 0 to 0

            try {
                val result = repository.getGalleryWithImages(galleryId) { current, total ->
                    _downloadProgress.value = current to total
                }

                if (result.isSuccess) {
                    _currentGallery.value = result.getOrThrow()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load gallery"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
                _downloadProgress.value = 0 to 0
            }
        }
    }

    fun getCachedImagePath(galleryId: String, imageUrl: String): String {
        return repository.getCachedImagePath(galleryId, imageUrl) ?: imageUrl
    }

    fun isGalleryCached(galleryId: String): Boolean {
        return repository.isGalleryCached(galleryId)
    }

    fun clearError() {
        _error.value = null
    }
}
