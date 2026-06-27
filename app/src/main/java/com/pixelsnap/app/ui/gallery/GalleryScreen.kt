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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
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

@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onSnapClick: (String) -> Unit,
    onNewSnapClick: () -> Unit
) {
    val snaps by viewModel.snaps.collectAsState(initial = emptyList())
    var isFractalView by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Gallery",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View toggle: Grid list vs. Interactive Fractal Web View
                FilledTonalIconButton(
                    onClick = { isFractalView = !isFractalView },
                    shape = RoundedCornerShape(999.dp)
                ) {
                    if (isFractalView) {
                        Icon(Icons.Default.List, contentDescription = "Switch to Grid View")
                    } else {
                        Icon(Icons.Default.Info, contentDescription = "Switch to Fractal Web View")
                    }
                }
                
                FilledTonalButton(
                    onClick = onNewSnapClick,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("New Snap")
                }
            }
        }
        
        if (snaps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No snaps yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Capture something beautiful with your Pixel 9",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onNewSnapClick) {
                        Text("Open Camera")
                    }
                }
            }
        } else if (isFractalView) {
            // Interactive Concentric / Fractal Web Gallery WebView
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
                                        snaps.forEach { snap ->
                                            add(buildJsonObject {
                                                put("id", snap.id)
                                                put("imagePath", snap.imagePath)
                                                put("caption", snap.caption)
                                                putJsonArray("tags") {
                                                    snap.tags.forEach { add(it) }
                                                }
                                                put("timestamp", snap.timestamp)
                                            })
                                        }
                                    }.toString()
                                }

                                @JavascriptInterface
                                fun updateSnap(id: String, title: String, desc: String) {
                                    val snap = snaps.firstOrNull { it.id == id } ?: return
                                    val updated = snap.copy(caption = title) // Map title to caption
                                    viewModel.updateSnap(updated)
                                }

                                @JavascriptInterface
                                fun shareSnap(id: String) {
                                    val snap = snaps.firstOrNull { it.id == id } ?: return
                                    shareSnap(context, snap)
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
                            snaps.forEach { snap ->
                                add(buildJsonObject {
                                    put("id", snap.id)
                                    put("imagePath", snap.imagePath)
                                    put("caption", snap.caption)
                                    putJsonArray("tags") {
                                        snap.tags.forEach { add(it) }
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
                items(snaps, key = { it.id }) { snap ->
                    val file = File(snap.imagePath)
                    AsyncImage(
                        model = if (file.exists()) file else null,
                        contentDescription = snap.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSnapClick(snap.id) }
                    )
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

