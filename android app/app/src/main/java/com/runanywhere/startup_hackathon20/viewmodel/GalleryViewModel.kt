package com.runanywhere.startup_hackathon20.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runanywhere.startup_hackathon20.data.local.AppDatabase
import com.runanywhere.startup_hackathon20.data.local.entities.Gallery
import com.runanywhere.startup_hackathon20.data.local.entities.GalleryWithImages
import com.runanywhere.startup_hackathon20.data.repository.GalleryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GalleryRepository
    private val _galleries = MutableStateFlow<List<GalleryWithImages>>(emptyList())
    val galleries: StateFlow<List<GalleryWithImages>> = _galleries.asStateFlow()

    private val _currentGallery = MutableStateFlow<GalleryWithImages?>(null)
    val currentGallery: StateFlow<GalleryWithImages?> = _currentGallery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GalleryRepository(database.galleryDao(), application.applicationContext)
        loadGalleries()
    }

    private fun loadGalleries() {
        viewModelScope.launch {
            repository.getAllGalleriesWithImages().collect { galleryList ->
                _galleries.value = galleryList
            }
        }
    }

    fun loadGallery(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val gallery = repository.getGalleryWithImages(id)
                _currentGallery.value = gallery
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createGallery(
        name: String,
        description: String,
        imagePaths: List<String>,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val galleryId = repository.createGallery(name, description, imagePaths)
                _error.value = null
                onSuccess(galleryId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGallery(gallery: Gallery) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateGallery(gallery)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteGallery(gallery: Gallery, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteGallery(gallery)
                _currentGallery.value = null
                _error.value = null
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
