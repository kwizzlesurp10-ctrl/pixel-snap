package com.pixelsnap.app.ui.camera

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

/**
 * Full-featured CameraScreen (opposite of MVP).
 * In the complete version this will support:
 * - Photo, Video (with Audio Magic Eraser), Portrait, Night modes
 * - Real-time Pixel AI suggestions overlay
 * - Multiple lenses + computational modes
 * - Shake detection + haptics (already partially implemented)
 * See preview.html for the rich client-side demo of these features.
 */
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.pixelsnap.app.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.Executor

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
    
    val previewView = remember { PreviewView(context) }
    
    // Check / request permission
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        hasCameraPermission = granted
        if (!granted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Camera",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onNavigateToGallery) {
                Text("Gallery", color = MaterialTheme.colorScheme.primary)
            }
        }
        
        if (hasCameraPermission) {
            // Camera preview
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
                
                // Subtle overlay labels (Pixel feel)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
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
                }
            }
            
            // Capture controls
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
                    // Gallery shortcut
                    OutlinedButton(
                        onClick = onNavigateToGallery,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Gallery")
                    }
                    
                    // Big capture (uses proper file-based capture for valid JPEGs)
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
                                            val caption = generateLocalCaption()
                                            val tags = listOf("pixel9", "moment")

                                            // New preferred path returns the real Snap with correct ID
                                            val savedSnap = viewModel.saveFromSavedFile(photoFile.absolutePath, caption, tags)

                                            // Haptics (Pixel 9 excellent vibration)
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
                                            onSnapSaved(savedSnap.id)  // real ID for reliable navigation
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        isCapturing = false
                                        // TODO: show error snackbar (SnackbarHostState can be added to screen)
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
                    
                    // Analyze (live frame) button - also file-based
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
                                            val caption = generateLocalCaption()
                                            val tags = listOf("ai", "pixel9")

                                            val savedSnap = viewModel.saveFromSavedFile(photoFile.absolutePath, caption, tags)
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Text("Analyze")
                    }
                }
                
                Text(
                    "Capture uses your Pixel 9 camera pipeline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            // Permission request UI
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
                Text(
                    "PixelSnap needs the camera to capture beautiful moments on your Pixel 9.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Access")
                }
            }
        }
    }
    
    // Bind camera when we have permission
    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            bindCameraUseCases(context, lifecycleOwner, previewView) { capture ->
                imageCapture = capture
            }
        }
    }
}

private fun generateLocalCaption(): String {
    val options = listOf(
        "A quiet, perfectly timed moment.",
        "The light on the Pixel 9 really makes this one sing.",
        "Simple scene. Extraordinary detail.",
        "Captured the feeling perfectly.",
        "One of those frames that makes you smile later."
    )
    return options.random()
}

private fun bindCameraUseCases(
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
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
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            // Log or show error
        }
    }, ContextCompat.getMainExecutor(context))
}
