package com.example.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Segment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.InstagramPost
import com.example.ui.components.FolderVideosBottomSheet
import com.example.ui.components.VideoPlayer
import com.example.ui.components.VideoScaleMode
import com.example.ui.theme.IgLikeRed
import com.example.ui.theme.InstagramStoryGradient
import kotlinx.coroutines.launch

@Composable
fun ReelsScreen(
    posts: List<InstagramPost>,
    onLikeClick: (InstagramPost) -> Unit,
    onCommentClick: (InstagramPost) -> Unit,
    onShareClick: (InstagramPost) -> Unit,
    onSaveClick: (InstagramPost) -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (posts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "جاري تحميل مقاطع الفيديو...",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { posts.size })
    val coroutineScope = rememberCoroutineScope()
    var activeFolderForSheet by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("reels_screen")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val post = posts[page]
            val isCurrentPage = pagerState.currentPage == page
            SingleReelPage(
                post = post,
                isCurrentPage = isCurrentPage,
                onLikeClick = { onLikeClick(post) },
                onCommentClick = { onCommentClick(post) },
                onShareClick = { onShareClick(post) },
                onSaveClick = { onSaveClick(post) },
                onOpenFolderClick = { folderName ->
                    activeFolderForSheet = folderName
                }
            )
        }

        // Folder Videos Bottom Sheet
        if (activeFolderForSheet != null) {
            val currentFolder = activeFolderForSheet ?: ""
            val folderVideos = remember(currentFolder, posts) {
                val filtered = posts.filter { it.media.bucketName.equals(currentFolder, ignoreCase = true) }
                if (filtered.isNotEmpty()) filtered else posts
            }
            val currentPostId = posts.getOrNull(pagerState.currentPage)?.id ?: -1L

            FolderVideosBottomSheet(
                folderName = currentFolder,
                videos = folderVideos,
                currentPostId = currentPostId,
                onSelectVideo = { selectedPost ->
                    val targetIndex = posts.indexOfFirst { it.id == selectedPost.id }
                    if (targetIndex != -1) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetIndex)
                        }
                    }
                },
                onDismiss = {
                    activeFolderForSheet = null
                }
            )
        }
    }
}

@Composable
private fun SingleReelPage(
    post: InstagramPost,
    isCurrentPage: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onOpenFolderClick: (String) -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var scaleMode by remember { mutableStateOf(VideoScaleMode.FIT) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var totalDurationMs by remember { mutableLongStateOf(0L) }
    var seekFraction by remember { mutableStateOf<Float?>(null) }
    var isUserSeeking by remember { mutableStateOf(false) }
    var dragProgressFraction by remember { mutableFloatStateOf(0f) }

    var showBigHeart by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }

    // Formatted counts
    val formattedLikes = remember(post.likesCount) {
        if (post.likesCount >= 1000) "${post.likesCount / 1000} ألف" else "167 ألف"
    }
    val formattedComments = remember(post.commentsCount) {
        if (post.commentsCount > 0) "${post.commentsCount * 50 + 67}" else "367"
    }
    val formattedShares = "68 ألف"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Fullscreen Video Player (FIT by default for original aspect ratio)
        VideoPlayer(
            videoUri = post.media.uri,
            autoPlay = isCurrentPage && isPlaying,
            isLooping = true,
            isMuted = isMuted,
            scaleMode = scaleMode,
            seekToFraction = seekFraction,
            onProgressUpdate = { cur, dur ->
                if (!isUserSeeking) {
                    currentPositionMs = cur
                    totalDurationMs = dur
                }
            },
            onTap = { isPlaying = !isPlaying },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Subtle Vignette Gradients for Text & UI Legibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // Big heart animation on double tap
        if (showBigHeart) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.Center)
                    .scale(heartScale.value)
            )
        }

        // Center Pause Indicator
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "تشغيل",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        // 3. Instagram Arabic Layout Overlay (LTR container to pin Left & Right sides precisely)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(modifier = Modifier.fillMaxSize()) {

                // LEFT SIDE: Action Buttons Column (Heart, Comment, Share, Scale toggle, Save)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Heart / Like
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onLikeClick,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "إعجاب",
                                tint = if (post.isLiked) IgLikeRed else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            text = formattedLikes,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Comment
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onCommentClick,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "تعليق",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Text(
                            text = formattedComments,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Share (Plane)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onShareClick,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "مشاركة",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = formattedShares,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Aspect Ratio / Fit-Fill Toggle Button
                    IconButton(
                        onClick = {
                            scaleMode = if (scaleMode == VideoScaleMode.FIT) VideoScaleMode.CROP else VideoScaleMode.FIT
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (scaleMode == VideoScaleMode.FIT) Icons.Default.AspectRatio else Icons.Default.FitScreen,
                            contentDescription = if (scaleMode == VideoScaleMode.FIT) "ملء الشاشة" else "الحجم الأصلي",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Bookmark / Save
                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // RIGHT SIDE: Folder Details, "فتح" button, Folder Avatar, Video Title
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 32.dp, start = 85.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val folderName = post.media.bucketName.ifBlank { "الفيديوهات" }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        // "فتح" (Open Folder) Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color.White),
                            modifier = Modifier
                                .height(32.dp)
                                .clickable { onOpenFolderClick(folderName) }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            ) {
                                Text(
                                    text = "فتح",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Folder Name
                        Text(
                            text = folderName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Folder Avatar with Instagram Story Gradient Ring
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(InstagramStoryGradient)
                                .padding(2.dp)
                                .clickable { onOpenFolderClick(folderName) },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = post.media.uri,
                                    contentDescription = "مجلد $folderName",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Video Title
                    val videoTitle = if (post.media.name.isNotBlank()) {
                        post.media.name
                    } else if (post.caption.isNotBlank() && !post.caption.contains("Moment captured")) {
                        post.caption
                    } else {
                        "فيديو بدون عنوان"
                    }

                    Text(
                        text = videoTitle,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Right,
                        style = androidx.compose.ui.text.TextStyle(
                            textDirection = TextDirection.Rtl
                        )
                    )
                }
            }
        }

        // 4. Interactive Real-Time Video Seekbar / Progress Line
        val currentProgress = if (isUserSeeking) {
            dragProgressFraction
        } else if (totalDurationMs > 0) {
            (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        InteractiveVideoProgressBar(
            progress = currentProgress,
            currentPositionMs = if (isUserSeeking) (dragProgressFraction * totalDurationMs).toLong() else currentPositionMs,
            totalDurationMs = totalDurationMs,
            isSeeking = isUserSeeking,
            onSeekStarted = {
                isUserSeeking = true
            },
            onSeekFraction = { fraction ->
                dragProgressFraction = fraction
            },
            onSeekFinished = { fraction ->
                isUserSeeking = false
                seekFraction = fraction
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun InteractiveVideoProgressBar(
    progress: Float,
    currentPositionMs: Long,
    totalDurationMs: Long,
    isSeeking: Boolean,
    onSeekStarted: () -> Unit,
    onSeekFraction: (Float) -> Unit,
    onSeekFinished: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Time Indicator when user is scrubbing / seeking
        if (isSeeking && totalDurationMs > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                val curSec = (currentPositionMs / 1000).coerceAtLeast(0)
                val totSec = (totalDurationMs / 1000).coerceAtLeast(1)
                val curText = String.format("%02d:%02d", curSec / 60, curSec % 60)
                val totText = String.format("%02d:%02d", totSec / 60, totSec % 60)

                Text(
                    text = "$curText / $totText",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Draggable & Tappable Progress Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .pointerInput(totalDurationMs) {
                    detectTapGestures { offset ->
                        if (size.width > 0) {
                            val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeekStarted()
                            onSeekFraction(fraction)
                            onSeekFinished(fraction)
                        }
                    }
                }
                .pointerInput(totalDurationMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (size.width > 0) {
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                onSeekStarted()
                                onSeekFraction(fraction)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (size.width > 0) {
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                onSeekFraction(fraction)
                            }
                        },
                        onDragEnd = {
                            onSeekFinished(progress)
                        },
                        onDragCancel = {
                            onSeekFinished(progress)
                        }
                    )
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Background line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSeeking) 5.dp else 2.5.dp)
                    .background(Color.White.copy(alpha = 0.25f))
            )

            // Active progress fill line
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                    .height(if (isSeeking) 5.dp else 2.5.dp)
                    .align(Alignment.BottomStart)
                    .background(if (isSeeking) Color(0xFFFFD700) else Color.White.copy(alpha = 0.9f))
            )
        }
    }
}
