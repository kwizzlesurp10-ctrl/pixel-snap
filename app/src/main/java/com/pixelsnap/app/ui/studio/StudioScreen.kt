package com.pixelsnap.app.ui.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pixelsnap.app.MainViewModel

@Composable
fun StudioScreen(viewModel: MainViewModel) {
    var brightness by remember { mutableFloatStateOf(0f) }

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
            Text(
                "Studio",
                style = MaterialTheme.typography.headlineMedium
            )
            TextButton(onClick = { /* Save or dismiss */ }) {
                Text("Done")
            }
        }

        // Canvas / Image Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.DarkGray)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Drawing logic would go here
            }
            Text("Select a photo from Gallery to edit", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }

        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Text("PIXEL AI ENHANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                val magicFilters = listOf("Auto Magic", "Portrait Pop", "Night Sight", "Magic Eraser")
                items(magicFilters) { filter ->
                    FilterChip(
                        selected = false,
                        onClick = { /* Apply */ },
                        label = { Text(filter) }
                    )
                }
            }

            Text("CREATIVE TOOLS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                val filters = listOf("Original", "Vibrant", "Warm", "Dramatic", "Noir", "Pastel", "Cinema", "Pixel Art")
                items(filters) { filter ->
                    FilterChip(
                        selected = filter == "Original",
                        onClick = { /* Apply */ },
                        label = { Text(filter) }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Brightness", style = MaterialTheme.typography.labelSmall)
                Text("${brightness.toInt()}", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = brightness,
                onValueChange = { brightness = it },
                valueRange = -50f..50f
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* Add text */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Text")
                }
                OutlinedButton(onClick = { /* Add sticker */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.EmojiEmotions, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sticker")
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* Crop */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Crop")
                }
                Button(onClick = { /* Save Edit */ }, modifier = Modifier.weight(1f)) {
                    Text("Save Edit")
                }
            }
        }
    }
}
