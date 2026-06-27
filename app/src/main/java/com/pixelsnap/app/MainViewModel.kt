package com.pixelsnap.app

import android.app.Application
import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsnap.app.data.Snap
import com.pixelsnap.app.data.SnapDatabase
import com.pixelsnap.app.data.SnapRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: SnapRepository
    
    val snaps: StateFlow<List<Snap>>
    
    init {
        val db = SnapDatabase.getDatabase(application)
        repository = SnapRepository(application, db.snapDao())
        
        snaps = repository.allSnaps.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }
    
    fun saveFromCamera(imageProxy: ImageProxy, caption: String = "", tags: List<String> = emptyList()) {
        viewModelScope.launch {
            repository.saveSnapFromCamera(imageProxy, caption, tags)
        }
    }
    
    /** Preferred for live camera captures — returns the created Snap (with real ID) synchronously to caller. */
    suspend fun saveFromSavedFile(path: String, caption: String = "", tags: List<String> = emptyList()): com.pixelsnap.app.data.Snap {
        return repository.saveSnapFromSavedFile(path, caption, tags)
    }
    
    fun saveFromBitmap(bitmap: Bitmap, caption: String = "", tags: List<String> = emptyList()) {
        viewModelScope.launch {
            repository.saveSnapFromBitmap(bitmap, caption, tags)
        }
    }
    
    fun updateSnap(snap: Snap) {
        viewModelScope.launch {
            repository.updateSnap(snap)
        }
    }
    
    fun deleteSnap(snap: Snap) {
        viewModelScope.launch {
            repository.deleteSnap(snap)
        }
    }
    
    fun getSnap(id: String): Snap? {
        // For simple cases the screen can observe the flow, but this is a quick sync helper
        return null // Prefer observing the list flow in UI
    }
}
