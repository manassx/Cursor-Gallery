package com.cursorgallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cursorgallery.data.models.GalleryImage

@Composable
fun ImageLightbox(
    images: List<GalleryImage>,
    currentIndex: Int,
    onDismiss: () -> Unit,
    onIndexChange: (Int) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isMobile = configuration.screenWidthDp < 600

    // Full-screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { /* Prevent clicks from propagating */ }
                )
            }
    ) {
        // Close button (top-left)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(if (isMobile) 12.dp else 16.dp)
                .size(if (isMobile) 40.dp else 48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(if (isMobile) 28.dp else 32.dp)
            )
        }

        // Main image (center)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isMobile) 48.dp else 80.dp,
                    vertical = if (isMobile) 60.dp else 80.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = images[currentIndex].url,
                contentDescription = "Full screen image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = false) { /* Prevent clicks */ }
            )
        }

        // Previous button (left)
        IconButton(
            onClick = {
                val newIndex = if (currentIndex > 0) currentIndex - 1 else images.size - 1
                onIndexChange(newIndex)
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = if (isMobile) 8.dp else 16.dp)
                .size(if (isMobile) 44.dp else 56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous",
                tint = Color.White,
                modifier = Modifier.size(if (isMobile) 36.dp else 48.dp)
            )
        }

        // Next button (right)
        IconButton(
            onClick = {
                val newIndex = if (currentIndex < images.size - 1) currentIndex + 1 else 0
                onIndexChange(newIndex)
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = if (isMobile) 8.dp else 16.dp)
                .size(if (isMobile) 44.dp else 56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next",
                tint = Color.White,
                modifier = Modifier.size(if (isMobile) 36.dp else 48.dp)
            )
        }
    }
}
