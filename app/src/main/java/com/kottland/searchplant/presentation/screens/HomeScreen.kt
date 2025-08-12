package com.kottland.searchplant.presentation.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.kottland.searchplant.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.kottland.searchplant.presentation.components.LoadingAnimation
import com.kottland.searchplant.presentation.components.ErrorDialog
import com.kottland.searchplant.presentation.viewmodel.PlantScannerViewModel
import com.kottland.searchplant.presentation.viewmodel.ScanState
import com.kottland.searchplant.utils.CameraUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Home screen with camera preview and scanning functionality
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onScanComplete: (Long) -> Unit,
    viewModel: PlantScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Camera permission
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // ViewModel states
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val currentScanResult by viewModel.currentScanResult.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val recentItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val favoriteItems by viewModel.favoriteItems.collectAsStateWithLifecycle()
    
    // Camera setup
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var showCameraView by remember { mutableStateOf(false) }
    
    // Handle scan completion
    LaunchedEffect(currentScanResult) {
        currentScanResult?.let { result ->
            onScanComplete(result.id)
            // Clear the scan result to prevent re-navigation
            viewModel.clearCurrentScanResult()
        }
    }
    
    // Reset camera view when scan is completed or when returning from scan result
    LaunchedEffect(scanState) {
        if (scanState == ScanState.Success || scanState == ScanState.Error) {
            showCameraView = false
        }
    }
    
    // Reset scan state only when we have a completed scan and are back on home
    LaunchedEffect(scanState, showCameraView) {
        if ((scanState == ScanState.Success || scanState == ScanState.Error) && !showCameraView) {
            // Small delay to ensure navigation has completed
            kotlinx.coroutines.delay(100)
            viewModel.resetScanState()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (showCameraView && cameraPermissionState.status.isGranted) {
            // Full screen camera view
            CameraView(
                onBack = { showCameraView = false },
                onImageCaptured = { bitmap, uri ->
                    viewModel.scanPlant(bitmap, uri)
                    showCameraView = false
                },
                scanState = scanState
            )
        } else {
            // Main grid layout
            MainGridLayout(
                onScanClick = {
                    if (cameraPermissionState.status.isGranted) {
                        showCameraView = true
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                },
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToFavorites = onNavigateToFavorites,
                onNavigateToSettings = onNavigateToSettings,
                recentItemsCount = recentItems.size,
                favoriteItemsCount = favoriteItems.size
            )
        }
        
        // Loading overlay
        if (scanState == ScanState.Scanning) {
            LoadingOverlay()
        }
        
        // Error dialog
        errorMessage?.let { message ->
            ErrorDialog(
                isVisible = true,
                errorMessage = message,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun MainGridLayout(
    onScanClick: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    recentItemsCount: Int,
    favoriteItemsCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp).padding(top = 48.dp)
    ) {
        // App Header
        AppHeader()
        
        Spacer(modifier = Modifier.height(24.dp))

        // Main Features Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            // Scan Feature - Primary
            item {
                FeatureCard(
                    title = "Scan Plant",
                    subtitle = "Identify plants with AI",
                    iconPainter = painterResource(R.drawable.ic_camera),
                    onClick = onScanClick,
                    isPrimary = true,
                    modifier = Modifier.aspectRatio(1f)
                )
            }
            
            // History Feature
            item {
                FeatureCard(
                    title = "History",
                    subtitle = "$recentItemsCount scanned items",
                    icon = Icons.Default.List,
                    onClick = onNavigateToHistory,
                    modifier = Modifier.aspectRatio(1f)
                )
            }
            
            // Favorites Feature
            item {
                FeatureCard(
                    title = "Favorites",
                    subtitle = "$favoriteItemsCount saved plants",
                    icon = Icons.Default.Favorite,
                    onClick = onNavigateToFavorites,
                    modifier = Modifier.aspectRatio(1f)
                )
            }
            
            // Settings Feature
            item {
                FeatureCard(
                    title = "Settings",
                    subtitle = "App preferences",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToSettings,
                    modifier = Modifier.aspectRatio(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp),
          )
        
        // Quick Tips
        QuickTipsCard()
    }
}

@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera),
            contentDescription = "SearchPlant Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "SearchPlant",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "Discover the world of plants",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
    icon: ImageVector? = null,
    iconPainter: Painter? = null
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPrimary) 8.dp else 4.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when {
                icon != null -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(if (isPrimary) 40.dp else 32.dp),
                        tint = if (isPrimary) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
                iconPainter != null -> {
                    Icon(
                        painter = iconPainter,
                        contentDescription = title,
                        modifier = Modifier.size(if (isPrimary) 40.dp else 32.dp),
                        tint = if (isPrimary) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickTipsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tips",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Quick Tips",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• Point your camera at leaves or flowers for best results\n• Ensure good lighting for accurate identification\n• Hold steady while scanning",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    onBack: () -> Unit,
    onImageCaptured: (Bitmap, String) -> Unit,
    scanState: ScanState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Camera preview
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onImageCaptureReady = { capture ->
                imageCapture = capture
            }
        )
        
        // Top bar with back button
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            shape = CircleShape
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        // Bottom capture controls
        BottomControls(
            modifier = Modifier.align(Alignment.BottomCenter),
            scanState = scanState,
            onCaptureClick = {
                imageCapture?.let { capture ->
                    captureImage(
                        imageCapture = capture,
                        context = context,
                        onImageCaptured = onImageCaptured,
                        onError = { exception ->
                            // Handle capture error
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
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
                    
                } catch (exc: Exception) {
                    // Handle camera binding error
                }
                
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    modifier: Modifier = Modifier,
    onNavigateToHistory: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SearchPlant",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onNavigateToHistory) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onNavigateToFavorites) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorites",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun BottomControls(
    modifier: Modifier = Modifier,
    scanState: ScanState,
    onCaptureClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (scanState) {
                    ScanState.Idle -> "Point camera at a plant and tap to scan"
                    ScanState.Scanning -> "Identifying plant..."
                    ScanState.Success -> "Plant identified successfully!"
                    ScanState.Error -> "Tap to try again"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            FloatingActionButton(
                onClick = onCaptureClick,
                modifier = Modifier.size(72.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    painter = when (scanState) {
                        ScanState.Scanning -> painterResource(R.drawable.ic_hourglass)
                        else -> painterResource(R.drawable.ic_camera)
                    },
                    contentDescription = "Capture",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun PermissionDeniedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "SearchPlant needs camera access to scan and identify plants",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoadingAnimation()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Identifying plant...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Capture image and convert to bitmap
 */
private fun captureImage(
    imageCapture: ImageCapture,
    context: Context,
    onImageCaptured: (Bitmap, String) -> Unit,
    onError: (Exception) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
    }
    
    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()
    
    imageCapture.takePicture(
        outputFileOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                output.savedUri?.let { uri ->
                    try {
                        val bitmap = CameraUtils.uriToBitmap(context, uri)
                        onImageCaptured(bitmap, uri.toString())
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
            }
            
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}