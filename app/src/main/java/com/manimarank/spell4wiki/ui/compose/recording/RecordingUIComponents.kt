package com.manimarank.spell4wiki.ui.compose.recording

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manimarank.spell4wiki.R
import java.util.*

/**
 * Centralized recording UI state
 */
data class RecordingUIState(
    val isRecording: Boolean = false,
    val isPlaying: Boolean = false,
    val isRecorded: Boolean = false,
    val recordedDuration: Long = 0,
    val currentPlayPosition: Int = 0,
    val maxPlayDuration: Int = 0,
    val recordingHint: String = "",
    val word: String = "",
    val language: String = "",
    val fileName: String = "",
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f
)

/**
 * Main recording UI component that can be reused across different screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingUIComponent(
    state: RecordingUIState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onUpload: () -> Unit,
    onClose: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with word and language info
            RecordingHeader(
                word = state.word,
                language = state.language,
                onSettingsClick = onSettingsClick,
                onClose = onClose
            )
            
            // Recording hint text
            Text(
                text = state.recordingHint,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Main recording button
            RecordingButton(
                isRecording = state.isRecording,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording
            )
            
            // Audio playback controls (shown when audio is recorded)
            if (state.isRecorded) {
                AudioPlaybackControls(
                    isPlaying = state.isPlaying,
                    currentPosition = state.currentPlayPosition,
                    maxDuration = state.maxPlayDuration,
                    recordedDuration = state.recordedDuration,
                    onPlayPause = onPlayPause,
                    onSeekTo = onSeekTo
                )
            }
            
            // File name preview
            if (state.fileName.isNotEmpty()) {
                FileNamePreview(fileName = state.fileName)
            }
            
            // Upload button (shown when audio is recorded)
            if (state.isRecorded) {
                UploadButton(
                    isUploading = state.isUploading,
                    uploadProgress = state.uploadProgress,
                    onUpload = onUpload
                )
            }
        }
    }
}

/**
 * Header component with word, language, and action buttons
 */
@Composable
private fun RecordingHeader(
    word: String,
    language: String,
    onSettingsClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = word,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = language,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Animated recording button with press-and-hold functionality
 */
@Composable
private fun RecordingButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.4f else 1.0f,
        animationSpec = tween(durationMillis = 200),
        label = "recording_scale"
    )
    
    val color by animateColorAsState(
        targetValue = if (isRecording) 
            MaterialTheme.colorScheme.error else 
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 200),
        label = "recording_color"
    )
    
    Box(
        modifier = modifier
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(color = color)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onStartRecording()
                        tryAwaitRelease()
                        onStopRecording()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.record),
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
    }
}

/**
 * Audio playback controls with seek bar
 */
@Composable
private fun AudioPlaybackControls(
    isPlaying: Boolean,
    currentPosition: Int,
    maxDuration: Int,
    recordedDuration: Long,
    onPlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Clear else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Slider(
                value = currentPosition.toFloat(),
                onValueChange = { onSeekTo(it.toInt()) },
                valueRange = 0f..maxDuration.toFloat(),
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Text(
                text = formatDuration(recordedDuration),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * File name preview component
 */
@Composable
private fun FileNamePreview(
    fileName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.upload_file_will_be),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Upload button with progress indicator
 */
@Composable
private fun UploadButton(
    isUploading: Boolean,
    uploadProgress: Float,
    onUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onUpload,
        enabled = !isUploading,
        modifier = modifier.fillMaxWidth()
    ) {
        if (isUploading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.uploading),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            Text(
                text = stringResource(R.string.upload),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

/**
 * Utility function to format duration
 */
private fun formatDuration(seconds: Long): String {
    return String.format(Locale.ENGLISH, "00:%02d", seconds)
}
