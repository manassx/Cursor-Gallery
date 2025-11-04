package com.runanywhere.startup_hackathon20.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.runanywhere.startup_hackathon20.viewmodel.GalleryViewModel
import java.io.File
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

data class PlacedImage(
    val bitmap: Bitmap,
    val position: Offset,
    val size: Size,
    val uri: String
)

@Composable
fun PreviewScreen(
    galleryId: Long,
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit
) {
    val currentGallery by viewModel.currentGallery.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var placedImages by remember { mutableStateOf<List<PlacedImage>>(emptyList()) }
    var nextImageIndex by remember { mutableIntStateOf(0) }
    var lastPosition by remember { mutableStateOf<Offset?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var threshold by remember { mutableIntStateOf(40) }
    var lightboxOpen by remember { mutableStateOf(false) }
    var lightboxImageUri by remember { mutableStateOf<String?>(null) }
    var lightboxImageIndex by remember { mutableIntStateOf(0) }

    // Calculate max images based on threshold (matching web app)
    val maxImages = when {
        threshold <= 20 -> 15
        threshold <= 40 -> 10
        else -> 6
    }

    LaunchedEffect(galleryId) {
        viewModel.loadGallery(galleryId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Canvas for drawing images
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    canvasSize = coordinates.size
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val position = change.position

                        val shouldPlace = lastPosition?.let { last ->
                            val distanceX = abs(position.x - last.x)
                            val distanceY = abs(position.y - last.y)
                            distanceX > threshold || distanceY > threshold
                        } ?: true

                        if (shouldPlace && currentGallery != null) {
                            lastPosition = position
                            val images = currentGallery!!.images

                            if (images.isNotEmpty()) {
                                val imageUri = images[nextImageIndex % images.size].imagePath

                                scope.launch {
                                    try {
                                        val screenWidth = canvasSize.width
                                        val screenHeight = canvasSize.height

                                        // INCREASED: min 200px, max 80vw/55vh
                                        val minWidth = with(density) { 200.dp.toPx() }
                                        val maxWidth = screenWidth * 0.8f
                                        val maxHeight = screenHeight * 0.55f

                                        val targetSize = min(maxWidth, maxHeight).toInt()

                                        val request = ImageRequest.Builder(context)
                                            .data(File(imageUri))
                                            .size(targetSize)
                                            .build()

                                        val result = context.imageLoader.execute(request)
                                        if (result is SuccessResult) {
                                            val bitmap =
                                                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                                            bitmap?.let {
                                                val aspectRatio =
                                                    it.width.toFloat() / it.height.toFloat()
                                                var displayWidth = it.width.toFloat()
                                                var displayHeight = it.height.toFloat()

                                                if (displayWidth < minWidth) {
                                                    displayWidth = minWidth
                                                    displayHeight = displayWidth / aspectRatio
                                                }

                                                if (displayWidth > maxWidth) {
                                                    displayWidth = maxWidth
                                                    displayHeight = displayWidth / aspectRatio
                                                }
                                                if (displayHeight > maxHeight) {
                                                    displayHeight = maxHeight
                                                    displayWidth = displayHeight * aspectRatio
                                                }

                                                // Offset images UPWARD so finger doesn't hide them
                                                val offsetY = with(density) { 120.dp.toPx() }
                                                val adjustedPosition = Offset(
                                                    position.x,
                                                    position.y - offsetY // Move up by 120dp
                                                )

                                                placedImages = (placedImages + PlacedImage(
                                                    bitmap = it,
                                                    position = adjustedPosition, // Use offset position
                                                    size = Size(displayWidth, displayHeight),
                                                    uri = imageUri
                                                )).takeLast(maxImages)
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                nextImageIndex++
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Check if tapped on an image
                        placedImages.reversed().forEach { placedImage ->
                            val left = placedImage.position.x - placedImage.size.width / 2
                            val top = placedImage.position.y - placedImage.size.height / 2
                            val right = left + placedImage.size.width
                            val bottom = top + placedImage.size.height

                            if (offset.x >= left && offset.x <= right &&
                                offset.y >= top && offset.y <= bottom
                            ) {
                                // Find index of this image in gallery
                                currentGallery?.images?.indexOfFirst { it.imagePath == placedImage.uri }
                                    ?.let { index ->
                                        lightboxImageUri = placedImage.uri
                                        lightboxImageIndex = index
                                        lightboxOpen = true
                                    }
                                return@detectTapGestures
                            }
                        }
                    }
                }
        ) {
            // Draw all images with FULL opacity (no fade)
            placedImages.forEach { placedImage ->
                translate(
                    left = placedImage.position.x - placedImage.size.width / 2,
                    top = placedImage.position.y - placedImage.size.height / 2
                ) {
                    drawImage(
                        image = placedImage.bitmap.asImageBitmap(),
                        dstSize = androidx.compose.ui.unit.IntSize(
                            placedImage.size.width.toInt(),
                            placedImage.size.height.toInt()
                        ),
                        alpha = 1f // FULL OPACITY
                    )
                }
            }
        }

        // Top-left: Close button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Top-right: Threshold control (floating, compact)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        threshold = when {
                            threshold > 140 -> threshold - 60
                            threshold > 80 -> threshold - 40
                            threshold > 40 -> threshold - 20
                            threshold > 20 -> threshold - 20
                            else -> 20
                        }.coerceIn(20, 80)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = "−",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                Text(
                    text = threshold.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.widthIn(min = 32.dp)
                )

                IconButton(
                    onClick = {
                        if (threshold >= 80) return@IconButton
                        threshold = when {
                            threshold < 40 -> threshold + 20
                            threshold < 80 -> threshold + 40
                            else -> 80
                        }.coerceIn(20, 80)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(
                        text = "+",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }

        // Top-center: Gallery name
        if (currentGallery != null && placedImages.isEmpty()) {
            Text(
                text = currentGallery?.gallery?.name ?: "",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            )
        }

        // Instructions (only show when no images)
        if (placedImages.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Drag your finger",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "across the screen",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }

    // Fullscreen Lightbox
    if (lightboxOpen && lightboxImageUri != null && currentGallery != null) {
        Dialog(
            onDismissRequest = { lightboxOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { lightboxOpen = false }
            ) {
                // Main image
                SubcomposeAsyncImage(
                    model = File(currentGallery!!.images[lightboxImageIndex].imagePath),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.9f)
                        .clickable(enabled = false) { },
                    contentScale = ContentScale.Fit
                )

                // Close button
                IconButton(
                    onClick = { lightboxOpen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous button
                if (lightboxImageIndex > 0) {
                    IconButton(
                        onClick = {
                            lightboxImageIndex--
                        },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Next button
                if (lightboxImageIndex < currentGallery!!.images.size - 1) {
                    IconButton(
                        onClick = {
                            lightboxImageIndex++
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Image counter
                Text(
                    text = "${lightboxImageIndex + 1} / ${currentGallery!!.images.size}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp)
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}
