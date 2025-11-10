package com.cursorgallery.ai

import android.content.Context
import android.util.Log
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import com.runanywhere.sdk.models.ModelInfo
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.public.extensions.listAvailableModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal object RunAnywhereManager {

    private const val TAG = "RunAnywhereManager"

    private val initializationState = MutableStateFlow<InitializationState>(InitializationState.Idle)
    val state: StateFlow<InitializationState> = initializationState.asStateFlow()

    private val availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = availableModels.asStateFlow()

    private val activeModelId = MutableStateFlow<String?>(null)
    val currentModelId: StateFlow<String?> = activeModelId.asStateFlow()

    private var initializationJob: Job? = null
    private val loadMutex = Mutex()

    fun initialize(context: Context) {
        System.err.println("========== RunAnywhereManager.initialize() CALLED ==========")
        Log.d(
            TAG,
            "Initialize called. Feature enabled: ${AiFeatureToggle.isEnabled}, Current state: ${initializationState.value}"
        )
        System.err.println("Feature enabled: ${AiFeatureToggle.isEnabled}, Current state: ${initializationState.value}")

        if (!AiFeatureToggle.isEnabled) {
            Log.w(TAG, "AI features disabled via toggle")
            System.err.println("ERROR: AI features disabled via toggle")
            initializationState.value =
                InitializationState.Failed(IllegalStateException("AI features disabled"))
            return
        }

        if (initializationState.value is InitializationState.Initialized) {
            Log.d(TAG, "Already initialized, skipping")
            System.err.println("Already initialized, skipping")
            return
        }

        initializationJob?.cancel()

        initializationJob = GlobalScope.launch(Dispatchers.IO) {
            initializationState.value = InitializationState.Initializing
            Log.d(TAG, "Starting SDK initialization...")
            System.err.println("Starting SDK initialization...")
            try {
                Log.d(TAG, "Calling RunAnywhere.initialize...")
                System.err.println("Calling RunAnywhere.initialize...")
                RunAnywhere.initialize(
                    context = context,
                    apiKey = AiConfig.apiKey,
                    environment = SDKEnvironment.DEVELOPMENT
                )
                Log.d(TAG, "SDK initialized successfully")
                System.err.println("✅ SDK initialized successfully")

                Log.d(TAG, "Registering LlamaCpp provider...")
                System.err.println("Registering LlamaCpp provider...")
                LlamaCppServiceProvider.register()
                Log.d(TAG, "LlamaCpp registered")
                System.err.println("✅ LlamaCpp registered")
                
                Log.d(TAG, "Registering models...")
                System.err.println("Registering models...")
                registerModelsSafely()
                Log.d(TAG, "Models registered")
                System.err.println("✅ Models registered")

                // Scan for previously downloaded models
                Log.d(TAG, "Scanning for downloaded models...")
                System.err.println("Scanning for downloaded models...")
                try {
                    RunAnywhere.scanForDownloadedModels()
                    Log.d(TAG, "Scan complete")
                    System.err.println("✅ Scan complete")
                } catch (e: Exception) {
                    Log.w(TAG, "Scan failed (non-fatal): ${e.message}")
                    System.err.println("⚠️ Scan failed: ${e.message}")
                }

                val modelsList = try {
                    listAvailableModels()
                } catch (e: Exception) {
                    Log.w(TAG, "Could not list models yet: ${e.message}")
                    System.err.println("WARNING: Could not list models yet: ${e.message}")
                    emptyList()
                }
                Log.d(TAG, "Found ${modelsList.size} models")
                System.err.println("✅ Found ${modelsList.size} models")
                availableModels.value = modelsList
                
                initializationState.value = InitializationState.Initialized
                Log.i(TAG, "✅ RunAnywhere initialization complete! Models: ${modelsList.size}")
                System.err.println("========== ✅ INITIALIZATION COMPLETE ==========")
            } catch (throwable: Throwable) {
                Log.e(TAG, "❌ RunAnywhere init failed", throwable)
                System.err.println("========== ❌ INITIALIZATION FAILED ==========")
                System.err.println("Error: ${throwable.message}")
                throwable.printStackTrace()
                initializationState.value = InitializationState.Failed(throwable)
            }
        }
    }

    private suspend fun registerModelsSafely() {
        AiConfig.models.forEach { model ->
            try {
                addModelFromURL(
                    url = model.url,
                    name = model.name,
                    type = model.type
                )
            } catch (throwable: Throwable) {
                Log.w(TAG, "Model registration failed for ${model.name}", throwable)
            }
        }
    }

    suspend fun refreshModels(): List<ModelInfo> {
        if (!AiFeatureToggle.isEnabled) return emptyList()
        
        // Don't try to scan if SDK isn't initialized yet
        if (initializationState.value !is InitializationState.Initialized) {
            Log.w(TAG, "Cannot refresh models - SDK not initialized yet. State: ${initializationState.value}")
            return availableModels.value
        }
        
        return try {
            val updated = listAvailableModels()
            availableModels.value = updated
            updated
        } catch (throwable: Throwable) {
            Log.e(TAG, "Model refresh failed", throwable)
            availableModels.value
        }
    }

    suspend fun downloadModel(modelId: String, onProgress: (Float) -> Unit) {
        if (!AiFeatureToggle.isEnabled) return
        runCatching {
            RunAnywhere.downloadModel(modelId).collect { progress ->
                onProgress(progress)
            }
        }.onFailure { throwable ->
            Log.e(TAG, "Model download failed", throwable)
            throw throwable
        }
        refreshModels()
    }

    suspend fun loadModel(modelId: String): Boolean {
        if (!AiFeatureToggle.isEnabled) return false
        return loadMutex.withLock {
            runCatching {
                val success = RunAnywhere.loadModel(modelId)
                if (success) {
                    activeModelId.value = modelId
                }
                success
            }.getOrElse { throwable ->
                Log.e(TAG, "Model load failed", throwable)
                false
            }
        }
    }

    suspend fun unloadModel() {
        if (!AiFeatureToggle.isEnabled) return
        loadMutex.withLock {
            runCatching {
                RunAnywhere.unloadModel()
            }.onFailure { throwable ->
                Log.e(TAG, "Model unload failed", throwable)
            }
            activeModelId.value = null
        }
    }

    sealed interface InitializationState {
        data object Idle : InitializationState
        data object Initializing : InitializationState
        data object Initialized : InitializationState
        data class Failed(val cause: Throwable) : InitializationState
    }
}
