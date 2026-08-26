package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.InstagramStoryGradient
import com.example.ui.viewmodel.MainTab

@Composable
fun InstagramBottomNavBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    userAvatarUri: Any?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .navigationBarsPadding()
            .testTag("instagram_bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Profile Tab (Left) with Notification Dot
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onTabSelected(MainTab.PROFILE) }
                    .testTag("nav_profile"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.5.dp,
                            color = if (selectedTab == MainTab.PROFILE) Color.White else Color.White.copy(alpha = 0.6f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (userAvatarUri != null) {
                        AsyncImage(
                            model = userAvatarUri,
                            contentDescription = "الملف الشخصي",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = if (selectedTab == MainTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "الملف الشخصي",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Small purple/pink story/notification dot on profile
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = 6.dp, y = (-6).dp)
                        .clip(CircleShape)
                        .background(InstagramStoryGradient)
                        .border(1.dp, Color.Black, CircleShape)
                )
            }

            // 2. Direct Messages / Share Tab (Middle)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onTabSelected(MainTab.CREATE) }
                    .testTag("nav_direct"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "الرسائل",
                    tint = if (selectedTab == MainTab.CREATE) Color.White else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. Reels Tab (Right - Active)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onTabSelected(MainTab.REELS) }
                    .testTag("nav_reels"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.VideoLibrary,
                    contentDescription = "ريلز",
                    tint = if (selectedTab == MainTab.REELS || selectedTab == MainTab.HOME) Color.White else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
