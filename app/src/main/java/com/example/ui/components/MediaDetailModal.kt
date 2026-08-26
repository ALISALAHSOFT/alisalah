package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem

@Composable
fun MediaDetailModal(
    mediaList: List<MediaItem>,
    initialIndex: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember(mediaList) {
        if (mediaList.isNotEmpty()) mediaList else emptyList()
    }
    if (items.isEmpty()) return

    val validInitialIndex = initialIndex.coerceIn(0, items.size - 1)
    val pagerState = rememberPagerState(
        initialPage = validInitialIndex,
        pageCount = { items.size }
    )
    val context = LocalContext.current
    var isMuted by remember { mutableStateOf(false) }

    val currentMedia = items.getOrElse(pagerState.currentPage) { items[0] }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("media_detail_modal")
    ) {
        // Vertical Pager for swiping between photos/videos from top to bottom (Instagram style)
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val item = items[page]
            val isCurrentPage = pagerState.currentPage == page

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (item.isVideo) {
                    VideoPlayer(
                        videoUri = item.uri,
                        autoPlay = isCurrentPage,
                        isLooping = true,
                        isMuted = isMuted,
                        scaleMode = VideoScaleMode.FIT,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ZoomableImage(
                        model = item.uri,
                        contentDescription = item.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Top Navigation Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Counter indicator (e.g. 3 / 15)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${items.size}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Mute / Unmute for videos
                if (currentMedia.isVideo) {
                    IconButton(
                        onClick = { isMuted = !isMuted },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "كتم الصوت",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Share Button
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = if (currentMedia.isVideo) "video/*" else "image/*"
                            putExtra(Intent.EXTRA_STREAM, currentMedia.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الوسائط"))
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Bottom Info Card
        Card(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = currentMedia.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentMedia.isVideo) "فيديو • ${currentMedia.durationFormatted}" else "صورة • ${currentMedia.width}x${currentMedia.height}",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatFileSize(currentMedia.sizeBytes),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}
