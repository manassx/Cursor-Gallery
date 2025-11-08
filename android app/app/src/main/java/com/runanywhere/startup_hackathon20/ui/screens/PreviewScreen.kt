package com.runanywhere.startup_hackathon20.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var threshold by remember { mutableIntStateOf(80) }
    var lightboxOpen by remember { mutableStateOf(false) }
    var lightboxImageUri by remember { mutableStateOf<String?>(null) }
    var lightboxImageIndex by remember { mutableIntStateOf(0) }

    // Max images based on threshold (web version mobile logic)
    val maxImages = when {
        threshold <= 20 -> 8
        threshold <= 40 -> 5
        else -> 3
    }

    LaunchedEffect(galleryId) {
        viewModel.loadGallery(galleryId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    canvasSize = coordinates.size
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val position = change.position
                        change.consume()

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
                                        val screenWidth = canvasSize.width.toFloat()
                                        val screenHeight = canvasSize.height.toFloat()

                                        // Larger images: min 250px, max 70vw/60vh
                                        val minWidth = with(density) { 250.dp.toPx() }
                                        val maxWidth = screenWidth * 0.7f
                                        val maxHeight = screenHeight * 0.6f

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

                                                // Ensure minimum size
                                                if (displayWidth < minWidth) {
                                                    displayWidth = minWidth
                                                    displayHeight = displayWidth / aspectRatio
                                                }

                                                // Constrain to max width
                                                if (displayWidth > maxWidth) {
                                                    displayWidth = maxWidth
                                                    displayHeight = displayWidth / aspectRatio
                                                }

                                                // Constrain to max height
                                                if (displayHeight > maxHeight) {
                                                    displayHeight = maxHeight
                                                    displayWidth = displayHeight * aspectRatio
                                                }

                                                // Use exact touch position
                                                placedImages = (placedImages + PlacedImage(
                                                    bitmap = it,
                                                    position = position,
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
                        placedImages.reversed().forEach { placedImage ->
                            val left = placedImage.position.x - placedImage.size.width / 2
                            val top = placedImage.position.y - placedImage.size.height / 2
                            val right = left + placedImage.size.width
                            val bottom = top + placedImage.size.height

                            if (offset.x >= left && offset.x <= right &&
                                offset.y >= top && offset.y <= bottom
                            ) {
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
                        )
                    )
                }
            }
        }

        // Bottom controls bar (matching web)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                // Threshold control
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Threshold:",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    IconButton(
                        onClick = {
                            threshold = when {
                                threshold > 140 -> threshold - 60
                                threshold > 80 -> threshold - 40
                                threshold > 40 -> threshold - 20
                                threshold > 20 -> threshold - 20
                                else -> 20
                            }.coerceIn(20, 200)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "−",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }

                    Text(
                        text = threshold.toString(),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.widthIn(min = 40.dp)
                    )

                    IconButton(
                        onClick = {
                            threshold = when {
                                threshold < 40 -> threshold + 20
                                threshold < 80 -> threshold + 40
                                threshold < 140 -> threshold + 60
                                threshold < 200 -> threshold + 60
                                else -> 200
                            }.coerceIn(20, 200)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                }

                // Spacer for balance
                Spacer(modifier = Modifier.width(40.dp))
            }
        }

        // Instructions
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

    // Lightbox
    if (lightboxOpen && lightboxImageUri != null && currentGallery != null) {
        Dialog(
            onDismissRequest = { lightboxOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { lightboxOpen = false }
            ) {
                SubcomposeAsyncImage(
                    model = File(currentGallery!!.images[lightboxImageIndex].imagePath),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.85f)
                        .clickable(enabled = false) { },
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { lightboxOpen = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (lightboxImageIndex > 0) {
                    IconButton(
                        onClick = { lightboxImageIndex-- },
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                if (lightboxImageIndex < currentGallery!!.images.size - 1) {
                    IconButton(
                        onClick = { lightboxImageIndex++ },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
        }
    }
}
