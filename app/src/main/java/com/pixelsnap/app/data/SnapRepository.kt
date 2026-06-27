package com.pixelsnap.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SnapRepository(
    private val context: Context,
    private val snapDao: SnapDao
) {
    val allSnaps: Flow<List<Snap>> = snapDao.getAllSnaps()

    /**
     * Preferred path for live CameraX still capture.
     * Uses the file path produced by CameraX (guarantees valid JPEG + EXIF from the Pixel ISP).
     */
    suspend fun saveSnapFromSavedFile(savedPath: String, caption: String = "", tags: List<String> = emptyList()): Snap {
        return withContext(Dispatchers.IO) {
            val snapsDir = File(context.filesDir, "snaps")
            if (!snapsDir.exists()) snapsDir.mkdirs()

            // If CameraX gave us a temp/ different location, copy into our private snaps dir for consistency.
            val source = File(savedPath)
            val target = if (source.parentFile?.absolutePath == snapsDir.absolutePath) {
                source
            } else {
                val dest = File(snapsDir, "${UUID.randomUUID()}.jpg")
                source.copyTo(dest, overwrite = true)
                source.delete() // clean up CameraX temp if it was one
                dest
            }

            val snap = Snap(
                id = "snap_${System.currentTimeMillis()}",
                imagePath = target.absolutePath,
                caption = caption,
                tags = tags,
                timestamp = System.currentTimeMillis()
            )
            snapDao.insertSnap(snap)
            snap
        }
    }

    @Deprecated("Use saveSnapFromSavedFile for live CameraX captures to guarantee valid JPEGs")
    suspend fun saveSnapFromCamera(imageProxy: ImageProxy, caption: String = "", tags: List<String> = emptyList()): Snap {
        return withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "snaps")
            if (!file.exists()) file.mkdirs()
            
            val photoFile = File(file, "${UUID.randomUUID()}.jpg")
            
            // Convert ImageProxy to JPEG and save
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            
            FileOutputStream(photoFile).use { it.write(bytes) }
            imageProxy.close()
            
            val snap = Snap(
                id = "snap_${System.currentTimeMillis()}",
                imagePath = photoFile.absolutePath,
                caption = caption,
                tags = tags,
                timestamp = System.currentTimeMillis()
            )
            
            snapDao.insertSnap(snap)
            snap
        }
    }

    // Alternative path used by preview or manual imports
    suspend fun saveSnapFromBitmap(bitmap: Bitmap, caption: String = "", tags: List<String> = emptyList()): Snap {
        return withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "snaps")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            
            val snap = Snap(
                id = "snap_${System.currentTimeMillis()}",
                imagePath = file.absolutePath,
                caption = caption,
                tags = tags,
                timestamp = System.currentTimeMillis()
            )
            snapDao.insertSnap(snap)
            snap
        }
    }

    suspend fun updateSnap(snap: Snap) {
        snapDao.updateSnap(snap)
    }

    suspend fun deleteSnap(snap: Snap) {
        withContext(Dispatchers.IO) {
            // Delete image file
            try {
                File(snap.imagePath).delete()
            } catch (_: Exception) {}
            snapDao.deleteSnap(snap)
        }
    }

    suspend fun getSnap(id: String): Snap? = snapDao.getSnapById(id)
}
