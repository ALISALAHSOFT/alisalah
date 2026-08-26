package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.InstagramStory
import com.example.ui.theme.InstagramStoryGradient
import com.example.ui.theme.InstagramStorySeenGradient

@Composable
fun StoriesTray(
    stories: List<InstagramStory>,
    onStoryClick: (InstagramStory) -> Unit,
    onAddStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("stories_tray"),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Story
        item {
            UserStoryItem(
                story = stories.firstOrNull { it.isUserStory },
                onStoryClick = {
                    val userStory = stories.firstOrNull { it.isUserStory }
                    if (userStory != null) onStoryClick(userStory) else onAddStoryClick()
                },
                onAddClick = onAddStoryClick
            )
        }

        // Other Albums / Stories
        items(stories.filter { !it.isUserStory }, key = { it.id }) { story ->
            StoryCircleItem(
                story = story,
                onClick = { onStoryClick(story) }
            )
        }
    }
}

@Composable
private fun UserStoryItem(
    story: InstagramStory?,
    onStoryClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onStoryClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Story Ring if has story
            if (story != null) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(InstagramStoryGradient)
                        .padding(2.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(65.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(2.dp)
                    ) {
                        AsyncImage(
                            model = story.coverUri,
                            contentDescription = "قصتك",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(61.dp)
                                .clip(CircleShape)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("أنت", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Blue Plus Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onAddClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة قصة",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "قصتك",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StoryCircleItem(
    story: InstagramStory,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        val ringGradient = if (story.isSeen) InstagramStorySeenGradient else InstagramStoryGradient

        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(ringGradient)
                .padding(2.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
            ) {
                AsyncImage(
                    model = story.coverUri,
                    contentDescription = story.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(61.dp)
                        .clip(CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
