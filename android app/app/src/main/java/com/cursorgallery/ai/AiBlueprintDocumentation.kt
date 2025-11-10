package com.cursorgallery.ai

import com.cursorgallery.data.models.Gallery
import com.cursorgallery.data.models.GalleryImage

internal object AiBlueprintDocumentation {

    private val heroDescription = buildString {
        appendLine("CursorGallery AI elevates galleries without cloud calls.")
        appendLine("All intelligence runs on-device using RunAnywhere models.")
    }

    val architectureOverview: String = heroDescription + "\n" + buildString {
        appendLine("1. RunAnywhereManager bootstraps SDK state on app launch.")
        appendLine("2. AiActionBlueprints catalog UI actions and payload contracts.")
        appendLine("3. ViewModels orchestrate streaming replies via RunAnywhere APIs.")
        appendLine("4. Results sync to Supabase using existing ApiClient endpoints.")
    }

    fun moodDjPromptTemplate(userPrompt: String, gallery: Gallery): String {
        // ULTRA SIMPLE TEST - just ask directly without any formatting
        return "Generate a JSON object with these fields: title (string), description (string), primaryColor (hex code like #FF5733), secondaryColor (hex code). The title should be: $userPrompt"
    }

    fun critiquePromptTemplate(gallery: Gallery, images: List<GalleryImage>): String {
        return buildString {
            appendLine("You are a professional art curator reviewing a creative portfolio.")
            appendLine("Provide constructive feedback on this gallery.")
            appendLine()
            appendLine("Gallery Context:")
            appendLine("- Title: ${gallery.name}")
            appendLine("- Description: ${gallery.description ?: "No description provided"}")
            appendLine("- Number of images: ${images.size}")
            appendLine()
            appendLine("Evaluate this portfolio on three dimensions:")
            appendLine("1. Composition: Technical quality, framing, visual balance")
            appendLine("2. Emotional Resonance: Impact, mood, viewer connection")
            appendLine("3. Storytelling: Narrative flow, thematic coherence")
            appendLine()
            appendLine("Generate a single JSON object with these exact fields:")
            appendLine("{")
            appendLine("  \"overallScore\": <0-100>,")
            appendLine("  \"compositionScore\": <0-100>,")
            appendLine("  \"emotionScore\": <0-100>,")
            appendLine("  \"storytellingScore\": <0-100>,")
            appendLine("  \"highlights\": [\"<strength 1>\", \"<strength 2>\"],")
            appendLine("  \"recommendations\": [\"<improvement 1>\", \"<improvement 2>\"]")
            appendLine("}")
            appendLine()
            appendLine("Be specific and constructive. Respond ONLY with valid JSON.")
        }
    }

    fun sequencingPromptTemplate(gallery: Gallery, images: List<GalleryImage>): String {
        return buildString {
            appendLine("You are a visual storytelling expert arranging images for maximum impact.")
            appendLine("Analyze these images and suggest an optimal viewing order.")
            appendLine()
            appendLine("Gallery: ${gallery.name}")
            appendLine("Description: ${gallery.description ?: "No description"}")
            appendLine()
            appendLine("Images to sequence:")
            images.forEachIndexed { index, image ->
                val dimensions = if (image.metadata?.width != null && image.metadata?.height != null) {
                    "${image.metadata.width}x${image.metadata.height}"
                } else {
                    "unknown size"
                }
                appendLine("${index + 1}. ID: ${image.id.take(12)} | Dimensions: $dimensions | Current Position: ${image.orderIndex}")
            }
            appendLine()
            appendLine("Consider:")
            appendLine("- Color harmony and visual transitions")
            appendLine("- Emotional progression (build tension, create resolution)")
            appendLine("- Composition flow (varied vs consistent)")
            appendLine()
            appendLine("Generate a single JSON object with these exact fields:")
            appendLine("{")
            appendLine("  \"orderedImageIds\": [\"<image_id_1>\", \"<image_id_2>\", ...],")
            appendLine("  \"rationale\": [\"<why position 1>\", \"<why position 2>\", ...]")
            appendLine("}")
            appendLine()
            appendLine("The orderedImageIds array must contain ALL ${images.size} image IDs from the list above.")
            appendLine("Each rationale should be 1 short sentence explaining that image's position.")
            appendLine()
            appendLine("Respond ONLY with valid JSON. No markdown, no explanations outside the JSON.")
        }
    }
}
