package com.runanywhere.startup_hackathon20

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.cursorgallery.ai.RunAnywhereManager

class CursorGalleryApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        Log.d("CursorGalleryApp", "========== APP STARTING ==========")
        instance = this

        // Initialize RunAnywhere SDK via manager (which uses GlobalScope internally)
        RunAnywhereManager.initialize(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .logger(DebugLogger())
            .respectCacheHeaders(false)
            .build()
    }

    companion object {
        lateinit var instance: CursorGalleryApp
            private set
    }
}
