package com.pixelsnap.app.ui.camera

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.pixelsnap.app.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    onNavigateToGallery: () -> Unit,
    onSnapSaved: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }
    
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }
    
    var captureMode by remember { mutableStateOf("Photo") }
    var showAiMenu by remember { mutableStateOf(false) }
    var liveTags by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val previewView = remember { PreviewView(context) }
    
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasCameraPermission = granted
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    if (showAiMenu) {
        ModalBottomSheet(onDismissRequest = { showAiMenu = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Pixel AI Tools", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                val aiTools = listOf(
                    "Describe Scene" to "Full scene understanding + mood",
                    "Magic Editor" to "Remove objects, perfect lighting",
                    "Best Take" to "Pick the perfect frame (video)",
                    "Add Me" to "Insert yourself into group shots",
                    "Audio Magic Eraser" to "Clean background noise",
                    "Story Mode" to "Turn snaps into a short narrative"
                )
                aiTools.forEach { (title, subtitle) ->
                    ListItem(
                        headlineContent = { Text(title) },
                        supportingContent = { Text(subtitle) },
                        modifier = Modifier.clickable {
                            showAiMenu = false
                            // Trigger AI capture
                        }
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Capture",
                style = MaterialTheme.typography.headlineMedium
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    listOf("Photo", "Video", "Portrait").forEach { mode ->
                        val selected = captureMode == mode
                        Box(
                            modifier = Modifier
                                .clickable { captureMode = mode }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
        
        if (hasCameraPermission) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            "50 MP  •  Tensor G4",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    IconButton(onClick = { /* Toggle Flash */ }) {
                        Icon(Icons.Default.FlashOn, contentDescription = "Flash", tint = androidx.compose.ui.graphics.Color.White)
                    }
                }
                
                if (liveTags.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        liveTags.take(3).forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    tag.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { /* Retake logic */ }) {
                        Text("Retake")
                    }
                    
                    Button(
                        onClick = {
                            val capture = imageCapture ?: return@Button
                            isCapturing = true

                            val snapsDir = File(context.filesDir, "snaps").apply { if (!exists()) mkdirs() }
                            val photoFile = File(snapsDir, "${UUID.randomUUID()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                            capture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        scope.launch {
                                            val caption = listOf(
                                                "A quiet, perfectly timed moment.",
                                                "The light on the Pixel 9 really makes this one sing.",
                                                "Simple scene. Extraordinary detail."
                                            ).random()
                                            
                                            val savedSnap = viewModel.saveFromSavedFile(photoFile.absolutePath, caption, listOf("pixel9", captureMode.lowercase()))

                                            val vibrator = context.getSystemService(android.os.Vibrator::class.java)
                                            vibrator?.let {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    it.vibrate(android.os.VibrationEffect.createOneShot(40, android.os.VibrationEffect.EFFECT_HEAVY_CLICK))
                                                } else {
                                                    @Suppress("DEPRECATION")
                                                    it.vibrate(40)
                                                }
                                            }

                                            isCapturing = false
                                            onSnapSaved(savedSnap.id)
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                    }
                                }
                            )
                        },
                        shape = RoundedCornerShape(999.dp),
                        modifier = Modifier.size(width = 92.dp, height = 92.dp),
                        enabled = !isCapturing
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Snap", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                    
                    Button(
                        onClick = { showAiMenu = true },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Pixel AI")
                    }
                }
                
                Text(
                    "${captureMode.uppercase()} MODE • PIXEL 9 EXCLUSIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Camera permission needed", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Access")
                }
            }
        }
    }
    
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            bindCameraUseCases(context, lifecycleOwner, previewView, { tags -> liveTags = tags }) { capture ->
                imageCapture = capture
            }
        }
    }
}

private fun bindCameraUseCases(
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    onTagsUpdated: (List<String>) -> Unit,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
            
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        onTagsUpdated(labels.filter { it.confidence > 0.65f }.map { it.text })
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )
            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            // Error handling
        }
    }, ContextCompat.getMainExecutor(context))
}
