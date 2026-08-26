package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.InstagramStory
import com.example.ui.theme.IgLikeRed
import kotlinx.coroutines.delay

@Composable
fun StoryViewerModal(
    story: InstagramStory,
    onClose: () -> Unit,
    onNextStory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember(story) { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }

    val items = story.items.ifEmpty { listOfNotNull(story.coverUri?.let { null }) }
    val currentMedia = items.getOrNull(currentIndex) ?: return

    val progress = remember(currentIndex, story) { Animatable(0f) }

    LaunchedEffect(currentIndex, isPaused, story) {
        if (!isPaused) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = ((1f - progress.value) * 4500).toInt(),
                    easing = LinearEasing
                )
            )
            // Auto advance
            if (currentIndex < items.size - 1) {
                currentIndex++
            } else {
                onClose()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("story_viewer_modal")
    ) {
        // Media Content
        if (currentMedia.isVideo) {
            VideoPlayer(
                videoUri = currentMedia.uri,
                autoPlay = !isPaused,
                isLooping = true,
                scaleMode = VideoScaleMode.FIT,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        } else {
            AsyncImage(
                model = currentMedia.uri,
                contentDescription = "قصة",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }

        // Top Gradient & Bottom Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Touch Control Areas (Left for prev, Right for next, Long press to pause)
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Tap Area (Prev)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (currentIndex > 0) {
                                    currentIndex--
                                }
                            }
                        )
                    }
            )

            // Right Tap Area (Next)
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = {
                                if (currentIndex < items.size - 1) {
                                    currentIndex++
                                } else {
                                    onClose()
                                }
                            }
                        )
                    }
            )
        }

        // Top Controls: Segmented Progress Bars & Author Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Progress Segments
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.indices.forEach { index ->
                    val segmentProgress = when {
                        index < currentIndex -> 1f
                        index == currentIndex -> progress.value
                        else -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { segmentProgress },
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.35f),
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Author Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = story.coverUri,
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = story.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "• ${currentMedia.bucketName}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Bottom Reply Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("إرسال رسالة...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f)
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(onClick = { isLiked = !isLiked }) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "إعجاب",
                    tint = if (isLiked) IgLikeRed else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            IconButton(onClick = {
                replyText = ""
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "إرسال",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
