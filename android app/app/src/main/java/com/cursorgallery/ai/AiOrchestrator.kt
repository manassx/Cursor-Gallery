package com.cursorgallery.ai

import android.util.Log
import com.cursorgallery.data.models.Gallery
import com.cursorgallery.data.models.GalleryImage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.runanywhere.sdk.public.RunAnywhere
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.withContext

internal object AiOrchestrator {

    private const val TAG = "AiOrchestrator"
    private val gson = Gson()

    suspend fun generateMoodPreset(
        userPrompt: String,
        gallery: Gallery
    ): Result<MoodPresetSuggestion> {
        val prompt = AiBlueprintDocumentation.moodDjPromptTemplate(userPrompt, gallery)
        return executeJsonRequest(prompt) { json ->
            gson.fromJson(json, MoodPresetSuggestion::class.java)
        }
    }

    suspend fun generateSequencePlan(
        gallery: Gallery,
        images: List<GalleryImage>
    ): Result<ImageSequencePlan> {
        if (images.isEmpty()) {
            return Result.failure(IllegalArgumentException("No images available for sequencing"))
        }

        val prompt = AiBlueprintDocumentation.sequencingPromptTemplate(gallery, images)
        return executeJsonRequest(prompt) { json ->
            gson.fromJson(json, ImageSequencePlan::class.java)
        }
    }

    suspend fun generateCritique(
        gallery: Gallery,
        images: List<GalleryImage>
    ): Result<CritiqueReport> {
        if (images.isEmpty()) {
            return Result.failure(IllegalArgumentException("No images available for critique"))
        }

        val prompt = AiBlueprintDocumentation.critiquePromptTemplate(gallery, images)
        return executeJsonRequest(prompt) { json ->
            gson.fromJson(json, CritiqueReport::class.java)
        }
    }

    private suspend fun <T> executeJsonRequest(
        prompt: String,
        parse: (String) -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        runCatching {
            Log.d(TAG, "Calling RunAnywhere.generateStream with prompt length: ${prompt.length}")
            System.err.println("=== Starting generation with generateStream ===")

            // Use generateStream exactly like ChatViewModel does, collecting all tokens
            var fullResponse = ""
            RunAnywhere.generateStream(prompt).fold(fullResponse) { acc, token ->
                acc + token
            }

            Log.d(TAG, "Received full response length: ${fullResponse.length}")
            Log.d(TAG, "Raw response: $fullResponse")
            System.err.println("=== Received full response ===")
            System.err.println("Raw: ${fullResponse.take(500)}")

            val jsonPayload = sanitizeResponse(fullResponse)
            Log.d(TAG, "Sanitized JSON: $jsonPayload")
            System.err.println("Sanitized JSON: ${jsonPayload.take(500)}")

            val parsed = parse(jsonPayload)

            // Check if parsing resulted in null
            if (parsed == null) {
                throw IllegalStateException(
                    "JSON parsing returned null. Raw response: ${
                        fullResponse.take(
                            200
                        )
                    }"
                )
            }

            parsed
        }.onFailure { throwable ->
            Log.e(TAG, "AI execution failed", throwable)
            System.err.println("=== Generation failed: ${throwable.message} ===")
            throwable.printStackTrace()
        }
    }

    private fun sanitizeResponse(rawResponse: String): String {
        val trimmed = rawResponse.trim()
        val withoutFences = trimmed
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val startIndex = withoutFences.indexOf('{')
        val endIndex = withoutFences.lastIndexOf('}')
        return if (startIndex >= 0 && endIndex > startIndex) {
            withoutFences.substring(startIndex, endIndex + 1)
        } else {
            withoutFences
        }
    }
}
