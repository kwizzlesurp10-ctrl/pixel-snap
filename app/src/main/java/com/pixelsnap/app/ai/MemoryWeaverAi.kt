package com.pixelsnap.app.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Memory Weaver AI Core.
 * Executes on-device analysis (ML Kit) to extract semantic tags
 * without compromising user privacy.
 */
class MemoryWeaverAi {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    suspend fun analyzeAndTag(filePath: String): List<String> {
        return try {
            val file = File(filePath)
            if (!file.exists()) return emptyList()

            // Load bitmap (downsampled for faster AI processing)
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4 // 1/4 size, plenty for general labeling
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) ?: return emptyList()
            
            val image = InputImage.fromBitmap(bitmap, 0)
            
            // Run on-device ML Kit image labeling
            val labels = labeler.process(image).await()
            
            // Filter high-confidence tags and map to strings
            labels.filter { it.confidence > 0.7f }
                  .map { it.text.lowercase() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
