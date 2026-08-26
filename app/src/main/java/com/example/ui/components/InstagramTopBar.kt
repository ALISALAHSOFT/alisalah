package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IgLikeRed

@Composable
fun InstagramTopBar(
    onNewPostClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDirectMessagesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("instagram_top_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo Text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { /* no-op */ }
        ) {
            Text(
                text = "LocalGram",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Cursive,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "قائمة الحسابات",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // New Post (+)
        IconButton(
            onClick = onNewPostClick,
            modifier = Modifier.testTag("top_bar_new_post")
        ) {
            Icon(
                imageVector = Icons.Default.AddBox,
                contentDescription = "منشور جديد",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(26.dp)
            )
        }

        // Notifications (Heart with red badge)
        Box {
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("top_bar_notifications")
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "الإشعارات",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }
            // Red dot badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(IgLikeRed)
            )
        }

        // Direct Messages / Share
        IconButton(
            onClick = onDirectMessagesClick,
            modifier = Modifier.testTag("top_bar_direct_messages")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "الرسائل المباشرة",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
