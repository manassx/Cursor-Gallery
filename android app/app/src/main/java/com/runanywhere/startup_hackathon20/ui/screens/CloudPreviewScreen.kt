package com.runanywhere.startup_hackathon20.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.runanywhere.startup_hackathon20.viewmodel.CloudGalleryViewModel
import kotlin.math.sqrt
import kotlin.random.Random

data class CloudImagePosition(
    val imageUrl: String,
    val x: Float,
    val y: Float,
    val scale: Float,
    val rotation: Float
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CloudPreviewScreen(
    galleryId: String,
    viewModel: CloudGalleryViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val gallery by viewModel.currentGallery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var imagePositions by remember { mutableStateOf<List<CloudImagePosition>>(emptyList()) }
    var lastX by remember { mutableStateOf(0f) }
    var lastY by remember { mutableStateOf(0f) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()

    val placementThreshold = 50f
    val maxImages = 10

    LaunchedEffect(galleryId) {
        if (gallery == null) {
            viewModel.loadGalleryDetail(galleryId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gallery?.name ?: "Gallery Preview") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            when {
                isLoading || gallery == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                gallery!!.images.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No images in gallery", color = Color.White)
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInteropFilter { event ->
                                when (event.action) {
                                    MotionEvent.ACTION_DOWN,
                                    MotionEvent.ACTION_MOVE -> {
                                        val x = event.x
                                        val y = event.y

                                        val distance = sqrt(
                                            (x - lastX) * (x - lastX) +
                                                    (y - lastY) * (y - lastY)
                                        )

                                        if (distance > placementThreshold || imagePositions.isEmpty()) {
                                            val randomImage = gallery!!.images.random()
                                            val cachedPath = viewModel.getCachedImagePath(
                                                galleryId,
                                                randomImage.url
                                            )

                                            val newPosition = CloudImagePosition(
                                                imageUrl = cachedPath,
                                                x = x,
                                                y = y,
                                                scale = Random.nextFloat() * 0.5f + 0.5f,
                                                rotation = Random.nextFloat() * 30f - 15f
                                            )

                                            imagePositions = (imagePositions + newPosition)
                                                .takeLast(maxImages)

                                            lastX = x
                                            lastY = y
                                        }
                                        true
                                    }

                                    else -> false
                                }
                            }
                    ) {
                        imagePositions.forEach { position ->
                            AsyncImage(
                                model = position.imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .offset(
                                        x = (position.x / screenWidth * 100).dp - 75.dp,
                                        y = (position.y / screenHeight * 100).dp - 75.dp
                                    )
                                    .size(150.dp)
                                    .rotate(position.rotation)
                                    .graphicsLayer(
                                        scaleX = position.scale,
                                        scaleY = position.scale
                                    ),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (imagePositions.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Tap and drag to create cursor trail",
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
