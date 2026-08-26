package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .testTag("instagram_bottom_nav_bar")
    ) {
        Column {
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home Tab
                NavItem(
                    isSelected = selectedTab == MainTab.HOME,
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    contentDescription = "الرئيسية",
                    onClick = { onTabSelected(MainTab.HOME) },
                    testTag = "nav_home"
                )

                // 2. Explore / Search Tab
                NavItem(
                    isSelected = selectedTab == MainTab.EXPLORE,
                    selectedIcon = Icons.Filled.Search,
                    unselectedIcon = Icons.Outlined.Search,
                    contentDescription = "استكشاف",
                    onClick = { onTabSelected(MainTab.EXPLORE) },
                    testTag = "nav_explore"
                )

                // 3. New Post / Create Tab
                NavItem(
                    isSelected = selectedTab == MainTab.CREATE,
                    selectedIcon = Icons.Filled.AddBox,
                    unselectedIcon = Icons.Outlined.AddBox,
                    contentDescription = "نشر",
                    onClick = { onTabSelected(MainTab.CREATE) },
                    testTag = "nav_create"
                )

                // 4. Reels Tab
                NavItem(
                    isSelected = selectedTab == MainTab.REELS,
                    selectedIcon = Icons.Filled.VideoLibrary,
                    unselectedIcon = Icons.Outlined.VideoLibrary,
                    contentDescription = "ريلز",
                    onClick = { onTabSelected(MainTab.REELS) },
                    testTag = "nav_reels"
                )

                // 5. Profile Tab
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (selectedTab == MainTab.PROFILE) 2.dp else 0.dp,
                            color = if (selectedTab == MainTab.PROFILE) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                        .clickable { onTabSelected(MainTab.PROFILE) }
                        .testTag("nav_profile"),
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
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavItem(
    isSelected: Boolean,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp)
        )
    }
}
