package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

enum class VideoScaleMode {
    FIT,
    CROP
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    isLooping: Boolean = true,
    isMuted: Boolean = false,
    scaleMode: VideoScaleMode = VideoScaleMode.CROP,
    onTap: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPlayingState by remember { mutableStateOf(autoPlay) }

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(videoUri)
            setMediaItem(mediaItem)
            repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            playWhenReady = autoPlay
            volume = if (isMuted) 0f else 1f
            prepare()
        }
    }

    LaunchedEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
    }

    LaunchedEffect(autoPlay) {
        exoPlayer.playWhenReady = autoPlay
        isPlayingState = autoPlay
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (isPlayingState) {
                        exoPlayer.play()
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.stop()
                }
                else -> {}
            }
        }
        val lifecycle = lifecycleOwner.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (onTap != null) {
                    onTap()
                } else {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        isPlayingState = false
                    } else {
                        exoPlayer.play()
                        isPlayingState = true
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = when (scaleMode) {
                        VideoScaleMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                        VideoScaleMode.CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = when (scaleMode) {
                    VideoScaleMode.FIT -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    VideoScaleMode.CROP -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Play icon overlay when paused
        if (!isPlayingState) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "تشغيل",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
