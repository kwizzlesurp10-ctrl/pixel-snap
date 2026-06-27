package com.pixelsnap.app.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.pixelsnap.app.MainViewModel
import com.pixelsnap.app.data.Snap
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapDetailScreen(
    snapId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val snaps by viewModel.snaps.collectAsState(initial = emptyList())
    val snap = snaps.firstOrNull { it.id == snapId }
    
    var caption by remember(snap) { mutableStateOf(snap?.caption ?: "") }
    var tagsText by remember(snap) { mutableStateOf(snap?.tags?.joinToString(", ") ?: "") }
    
    val context = LocalContext.current
    
    if (snap == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Snap not found")
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("Snap") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { shareSnap(context, snap) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = {
                    viewModel.deleteSnap(snap)
                    onDeleted()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        )
        
        // Photo
        val file = remember(snap.imagePath) { File(snap.imagePath) }
        AsyncImage(
            model = if (file.exists()) file else null,
            contentDescription = snap.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
        )
        
        // Editable fields
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text("Caption", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Describe this moment...") }
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text("Tags", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("pixel9, sunset, candid") }
            )
            
            Spacer(Modifier.height(28.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { shareSnap(context, snap) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }
                
                Button(
                    onClick = {
                        val updated = snap.copy(
                            caption = caption,
                            tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        )
                        viewModel.updateSnap(updated)
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Save")
                }
                
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Done")
                }
            }
        }
        
        Spacer(Modifier.weight(1f))
        
        Text(
            "Captured on your Pixel 9 • Private to this device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp)
        )
    }
}

/**
 * Full-featured SnapDetail + Studio (opposite of MVP).
 * Expanded vision includes:
 * - Advanced Canvas editing (filters, generative fill sim, text, stickers)
 * - Pixel AI one-tap tools (Magic Editor, Best Take, Add Me, etc.)
 * - Video trimming + Audio Magic Eraser
 * - Export to PDF Memory Books, Stories, Reels
 * The preview.html delivers the complete ambitious editor experience today.
 */

/** Real share using FileProvider so the photo can be sent from private storage (great while texting). */
private fun shareSnap(context: android.content.Context, snap: com.pixelsnap.app.data.Snap) {
    val photoFile = File(snap.imagePath)
    if (!photoFile.exists()) return

    val authority = "${context.packageName}.fileprovider"
    val photoUri: Uri = FileProvider.getUriForFile(context, authority, photoFile)

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, photoUri)
        putExtra(Intent.EXTRA_TEXT, snap.caption.ifBlank { "Shared from PixelSnap on my Pixel 9" })
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, "Share snap")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}
