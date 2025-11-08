package com.runanywhere.startup_hackathon20.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.runanywhere.startup_hackathon20.viewmodel.CloudGalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudGalleryDetailScreen(
    galleryId: String,
    viewModel: CloudGalleryViewModel = viewModel(),
    onBackClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    val gallery by viewModel.currentGallery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(galleryId) {
        viewModel.loadGalleryDetail(galleryId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(gallery?.name ?: "Gallery", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            if (downloadProgress.second > 0) {
                                LinearProgressIndicator(
                                    progress = downloadProgress.first.toFloat() / downloadProgress.second.toFloat(),
                                    modifier = Modifier.width(200.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${downloadProgress.first} / ${downloadProgress.second}")
                            } else {
                                Text("Loading...")
                            }
                        }
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(error ?: "Unknown error")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadGalleryDetail(galleryId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                gallery != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(gallery!!.images) { image ->
                                AsyncImage(
                                    model = viewModel.getCachedImagePath(galleryId, image.url),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Button(
                            onClick = onPreviewClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                "PREVIEW GALLERY",
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
