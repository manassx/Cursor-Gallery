package com.cursorgallery.ui.screens.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cursorgallery.ai.AiActionBlueprints
import com.cursorgallery.ai.RunAnywhereManager
import com.cursorgallery.viewmodel.AiStudioViewModel
import com.runanywhere.sdk.models.ModelInfo

@Composable
internal fun AiStudioBlueprintScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel = viewModel<AiStudioViewModel>()
    val managerState by RunAnywhereManager.state.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val activeModelId by RunAnywhereManager.currentModelId.collectAsState()

    // Refresh models when screen is loaded or when initialization completes
    LaunchedEffect(managerState) {
        if (managerState is RunAnywhereManager.InitializationState.Initialized) {
            viewModel.refreshModels()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BACK", fontWeight = FontWeight.Bold)
                }

                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFE8E8E8),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title section
                Text(
                    text = "AI Studio",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black
                    ),
                    color = Color.White
                )

                Text(
                    text = "Privacy-first creative intelligence powered by on-device models",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA8A8A8)
                )

                // SDK Status Card
                StatusCard(managerState = managerState, activeModelId = activeModelId)

                HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 2.dp)

                // Available Models Section
                Text(
                    text = "Available Models",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                if (uiState.models.isEmpty()) {
                    Text(
                        text = "No models found. Make sure SDK is initialized.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF5555)
                    )
                } else {
                    uiState.models.forEach { model ->
                        ModelCard(
                            model = model,
                            isActive = model.id == activeModelId,
                            downloadProgress = uiState.downloadProgress[model.id],
                            onDownload = { viewModel.downloadModel(model.id) },
                            onLoad = { viewModel.loadModel(model.id) },
                            onUnload = { viewModel.unloadModel() }
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF2A2A2A), thickness = 2.dp)

                // AI Features Section
                Text(
                    text = "AI Features",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Text(
                    text = "Load a model above to unlock these tools in the Gallery Editor",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFA8A8A8)
                )

                AiActionBlueprints.actions.forEach { action ->
                    FeatureCard(
                        title = action.title,
                        description = action.description,
                        type = action.type.name
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    managerState: RunAnywhereManager.InitializationState,
    activeModelId: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (icon, color) = when (managerState) {
                    is RunAnywhereManager.InitializationState.Initialized -> 
                        Icons.Default.CheckCircle to Color(0xFF4CAF50)
                    is RunAnywhereManager.InitializationState.Initializing -> 
                        Icons.Default.CloudDownload to Color(0xFFFFA726)
                    is RunAnywhereManager.InitializationState.Failed -> 
                        Icons.Default.Error to Color(0xFFFF5555)
                    else -> Icons.Default.Error to Color(0xFFA8A8A8)
                }

                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = "SDK Status",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }

            Text(
                text = when (managerState) {
                    is RunAnywhereManager.InitializationState.Initialized -> "Ready"
                    is RunAnywhereManager.InitializationState.Initializing -> "Initializing..."
                    is RunAnywhereManager.InitializationState.Failed -> {
                        val errorMsg = managerState.cause.message ?: "Unknown error"
                        val stackTrace = managerState.cause.stackTraceToString().take(200)
                        "Failed: $errorMsg\n\n$stackTrace"
                    }

                    else -> "Not initialized - Check logs for errors"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when (managerState) {
                    is RunAnywhereManager.InitializationState.Failed -> Color(0xFFFF5555)
                    else -> Color(0xFFA8A8A8)
                }
            )

            if (activeModelId != null) {
                HorizontalDivider(color = Color(0xFF2A2A2A))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Active Model: $activeModelId",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: ModelInfo,
    isActive: Boolean,
    downloadProgress: Float?,
    onDownload: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1E3A1E) else Color(0xFF141414)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFF2A2A2A)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = model.category.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA8A8A8)
                    )
                }

                if (isActive) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Download progress
            if (downloadProgress != null) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE8E8E8),
                        trackColor = Color(0xFF2A2A2A)
                    )
                    Text(
                        text = "Downloading: ${(downloadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFA8A8A8)
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when {
                    !model.isDownloaded && downloadProgress == null -> {
                        Button(
                            onClick = onDownload,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE8E8E8),
                                contentColor = Color(0xFF0A0A0A)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download", fontWeight = FontWeight.Bold)
                        }
                    }
                    model.isDownloaded && !isActive -> {
                        Button(
                            onClick = onLoad,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Model", fontWeight = FontWeight.Bold)
                        }
                    }
                    isActive -> {
                        OutlinedButton(
                            onClick = onUnload,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF5555)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFF5555)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Stop,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Unload", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    type: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF141414),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA8A8A8)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFA8A8A8)
            )
        }
    }
}
