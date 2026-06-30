package com.pixelsnap.app.ui.gallery

import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.pixelsnap.app.MainViewModel
import com.pixelsnap.app.data.Snap
import java.io.File
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onSnapClick: (String) -> Unit,
    onNewSnapClick: () -> Unit
) {
    val snaps by viewModel.snaps.collectAsState(initial = emptyList())
    var isFractalView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val filteredSnaps = snaps.filter { snap ->
        val matchesSearch = if (searchQuery.isBlank()) true else {
            snap.caption.contains(searchQuery, ignoreCase = true) || 
            snap.tags.any { it.contains(searchQuery, ignoreCase = true) }
        }
        val isFav = snap.tags.contains("favorite")
        val matchesFav = if (showFavoritesOnly) isFav else true
        matchesSearch && matchesFav
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Gallery",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text("${filteredSnaps.size} moments • 3 albums", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { /* Open Albums Modal */ },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(Icons.Default.Folder, contentDescription = "Albums")
                }
                
                FilledTonalIconButton(
                    onClick = { isFractalView = !isFractalView },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    if (isFractalView) {
                        Icon(Icons.Default.GridView, contentDescription = "Switch to Grid View")
                    } else {
                        Icon(Icons.Default.AccountTree, contentDescription = "Switch to Fractal Web View")
                    }
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search moments...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            
            IconButton(
                onClick = { showFavoritesOnly = !showFavoritesOnly },
                modifier = Modifier.align(Alignment.CenterVertically).clip(RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorites",
                    tint = if (showFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        if (filteredSnaps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))
                    Text(if (snaps.isEmpty()) "Your story starts here" else "No matching snaps", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (snaps.isEmpty()) "Capture with Pixel 9 and let PixelSnap organize the magic" else "Try a different search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (snaps.isEmpty()) {
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onNewSnapClick) {
                            Text("Open Camera")
                        }
                    }
                }
            }
        } else if (isFractalView) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                allowFileAccess = true
                                allowContentAccess = true
                            }
                            webViewClient = WebViewClient()
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun getSnapsJson(): String {
                                    return buildJsonArray {
                                        filteredSnaps.forEach { snap ->
                                            add(buildJsonObject {
                                                put("id", snap.id)
                                                put("imagePath", snap.imagePath)
                                                put("caption", snap.caption)
                                                putJsonArray("tags") {
                                                    snap.tags.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
                                                }
                                                put("timestamp", snap.timestamp)
                                            })
                                        }
                                    }.toString()
                                }

                                @JavascriptInterface
                                fun updateSnap(id: String, title: String, desc: String) {
                                    val snap = filteredSnaps.firstOrNull { it.id == id } ?: return
                                    val updated = snap.copy(caption = title)
                                    viewModel.updateSnap(updated)
                                }

                                @JavascriptInterface
                                fun shareSnap(id: String) {
                                    val snap = filteredSnaps.firstOrNull { it.id == id } ?: return
                                    com.pixelsnap.app.ui.gallery.shareSnap(context, snap)
                                }

                                @JavascriptInterface
                                fun openSnapDetail(id: String) {
                                    onSnapClick(id)
                                }
                            }, "AndroidBridge")
                            loadUrl("file:///android_asset/fractal_gallery.html")
                        }
                    },
                    update = { webView ->
                        val snapsJson = buildJsonArray {
                            filteredSnaps.forEach { snap ->
                                add(buildJsonObject {
                                    put("id", snap.id)
                                    put("imagePath", snap.imagePath)
                                    put("caption", snap.caption)
                                    putJsonArray("tags") {
                                        snap.tags.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
                                    }
                                    put("timestamp", snap.timestamp)
                                })
                            }
                        }.toString()
                        webView.evaluateJavascript("if (window.loadAndroidSnaps) { window.loadAndroidSnaps('$snapsJson'); }", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredSnaps, key = { it.id }) { snap ->
                    val file = File(snap.imagePath)
                    Box(modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(14.dp))) {
                        AsyncImage(
                            model = if (file.exists()) file else null,
                            contentDescription = snap.caption,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onSnapClick(snap.id) }
                        )
                        if (snap.tags.contains("favorite")) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun shareSnap(context: android.content.Context, snap: Snap) {
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
