package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.InstagramPost
import com.example.ui.theme.IgLikeRed
import com.example.ui.theme.InstagramStoryGradient
import kotlinx.coroutines.launch

@Composable
fun PostCard(
    post: InstagramPost,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveClick: () -> Unit,
    onMediaClick: () -> Unit,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBigHeart by remember { mutableStateOf(false) }
    var isExpandedCaption by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("post_card_${post.id}")
    ) {
        // 1. Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Gradient Ring
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(InstagramStoryGradient)
                    .padding(2.dp)
                    .clickable(onClick = onAuthorClick)
            ) {
                AsyncImage(
                    model = post.authorAvatarUri,
                    contentDescription = post.authorUsername,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onAuthorClick)
            ) {
                Text(
                    text = post.authorUsername,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (post.location.isNotBlank()) {
                    Text(
                        text = post.location,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "خيارات",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Media View with Double-Tap to Like
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (!post.isLiked) {
                                onLikeClick()
                            }
                            showBigHeart = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(800)
                                showBigHeart = false
                            }
                        },
                        onTap = { onMediaClick() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = post.media.uri,
                contentDescription = post.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Video Indicator Badge
            if (post.media.isVideo) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = "فيديو",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = post.media.durationFormatted,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Big Heart Animation on Double Tap
            androidx.compose.animation.AnimatedVisibility(
                visible = showBigHeart,
                enter = scaleIn(animationSpec = tween(150, easing = LinearOutSlowInEasing)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.size(100.dp)
                )
            }
        }

        // 3. Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.testTag("like_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "إعجاب",
                    tint = if (post.isLiked) IgLikeRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Comment
            IconButton(
                onClick = onCommentClick,
                modifier = Modifier.testTag("comment_button_${post.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "تعليق",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Share
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "مشاركة",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save / Bookmark
            IconButton(
                onClick = onSaveClick,
                modifier = Modifier.testTag("save_button_${post.id}")
            ) {
                Icon(
                    imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "حفظ",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(25.dp)
                )
            }
        }

        // 4. Likes Text
        Text(
            text = "${post.likesCount} إعجاب",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
        )

        // 5. Caption with Username
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            val fullText = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append(post.authorUsername)
                    append(" ")
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(post.caption)
                }
            }

            if (isExpandedCaption || post.caption.length <= 60) {
                Text(
                    text = fullText,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = fullText,
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = " ...المزيد",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { isExpandedCaption = true }
                    )
                }
            }
        }

        // 6. Comments Link
        if (post.commentsCount > 0) {
            Text(
                text = "عرض كل التعليقات (${post.commentsCount})",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 2.dp)
                    .clickable(onClick = onCommentClick)
            )
        } else {
            Text(
                text = "أضف تعليقاً...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 2.dp)
                    .clickable(onClick = onCommentClick)
            )
        }
    }
}
