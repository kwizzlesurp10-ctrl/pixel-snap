package com.pixelsnap.app.ui.memories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.pixelsnap.app.MainViewModel

@Composable
fun MemoriesScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                "Memories",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "AI-curated stories from your Pixel 9 life",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        val snaps by viewModel.snaps.collectAsState(initial = emptyList())
        val tagGroups = remember(snaps) {
            val groups = mutableMapOf<String, Int>()
            snaps.forEach { snap ->
                snap.tags.forEach { tag ->
                    if (tag != "favorite") {
                        groups[tag] = groups.getOrDefault(tag, 0) + 1
                    }
                }
            }
            groups.toList().sortedByDescending { it.second }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            item {
                MemoryCard(
                    title = "This Week on Pixel 9",
                    subtitle = "${snaps.size} moments",
                    description = "Auto-generated highlight reel with music & captions",
                    icon = Icons.Default.AutoAwesome
                )
            }
            
            items(tagGroups.take(5)) { (tag, count) ->
                val title = tag.replaceFirstChar { it.uppercase() } + " Collection"
                MemoryCard(
                    title = title,
                    subtitle = "$count moments",
                    description = "AI-curated Memory Garden based on '$tag'",
                    icon = Icons.Default.Book
                )
            }
            
            if (tagGroups.isEmpty()) {
                item {
                    Text("Capture more snaps to let the MemoryWeaver AI curate your collections!", 
                         style = MaterialTheme.typography.bodyMedium, 
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Button(
            onClick = { /* Export to PDF */ },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Export Memory Book (PDF)")
        }
    }
}

@Composable
fun MemoryCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Play memory */ },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        }
    }
}
