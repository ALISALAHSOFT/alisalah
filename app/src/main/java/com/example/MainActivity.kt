package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InstagramPost
import com.example.data.model.MediaItem
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.InstagramBottomNavBar
import com.example.ui.components.InstagramTopBar
import com.example.ui.components.MediaDetailModal
import com.example.ui.components.StoryViewerModal
import com.example.ui.screens.CreatePostScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PermissionScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.checkHasMediaPermission
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Check initial permission
                LaunchedEffect(Unit) {
                    val hasPerm = checkHasMediaPermission(this@MainActivity)
                    viewModel.setPermissionGranted(hasPerm)
                }

                if (!uiState.hasPermission) {
                    PermissionScreen(
                        onPermissionGranted = {
                            viewModel.setPermissionGranted(true)
                        }
                    )
                } else {
                    InstagramAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramAppContent(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val commentsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeCommentsPost = uiState.activeCommentsPost
    val activeComments = if (activeCommentsPost != null) {
        val commentsFlow = viewModel.getCommentsForPost(activeCommentsPost.id)
        val commentsList by commentsFlow.collectAsStateWithLifecycle()
        commentsList
    } else {
        emptyList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (uiState.activeTab == MainTab.HOME) {
                    InstagramTopBar(
                        onNewPostClick = { viewModel.selectTab(MainTab.CREATE) },
                        onNotificationsClick = { /* show notifications */ },
                        onDirectMessagesClick = { /* show direct messages */ }
                    )
                }
            },
            bottomBar = {
                if (uiState.activeStory == null && uiState.activeDetailMedia == null) {
                    InstagramBottomNavBar(
                        selectedTab = uiState.activeTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        userAvatarUri = uiState.mediaList.firstOrNull()?.uri
                    )
                }
            }
        ) { innerPadding ->
            AnimatedContent(
                targetState = uiState.activeTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition",
                modifier = Modifier.padding(innerPadding)
            ) { currentTab ->
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            posts = uiState.posts,
                            stories = uiState.stories,
                            isLoading = uiState.isLoading,
                            onStoryClick = { viewModel.openStory(it) },
                            onAddStoryClick = { viewModel.selectTab(MainTab.CREATE) },
                            onLikeClick = { viewModel.toggleLike(it) },
                            onCommentClick = { viewModel.openComments(it) },
                            onShareClick = { /* share */ },
                            onSaveClick = { viewModel.toggleSave(it) },
                            onMediaClick = { viewModel.openMediaDetail(it) },
                            onAuthorClick = { viewModel.selectTab(MainTab.PROFILE) },
                            onRefresh = { viewModel.loadDeviceMedia() }
                        )
                    }
                    MainTab.EXPLORE -> {
                        ExploreScreen(
                            mediaList = uiState.mediaList,
                            albums = uiState.albums,
                            selectedAlbum = uiState.selectedAlbum,
                            searchQuery = uiState.searchQuery,
                            onAlbumSelected = { viewModel.selectAlbum(it) },
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            onMediaClick = { viewModel.openMediaDetail(it) }
                        )
                    }
                    MainTab.CREATE -> {
                        CreatePostScreen(
                            mediaList = uiState.mediaList,
                            onPublish = { uri, isVideo, caption, filter ->
                                viewModel.publishNewPost(uri, isVideo, caption, filter)
                            },
                            onCancel = { viewModel.selectTab(MainTab.HOME) }
                        )
                    }
                    MainTab.REELS -> {
                        ReelsScreen(
                            posts = uiState.posts,
                            onLikeClick = { viewModel.toggleLike(it) },
                            onCommentClick = { viewModel.openComments(it) },
                            onShareClick = { /* share */ },
                            onSaveClick = { viewModel.toggleSave(it) },
                            onCameraClick = { viewModel.selectTab(MainTab.CREATE) }
                        )
                    }
                    MainTab.PROFILE -> {
                        ProfileScreen(
                            mediaList = uiState.mediaList,
                            savedPosts = uiState.savedPosts,
                            handle = uiState.userProfileHandle,
                            name = uiState.userProfileName,
                            bio = uiState.userBio,
                            onMediaClick = { viewModel.openMediaDetail(it) }
                        )
                    }
                }
            }
        }

        // Fullscreen Story Viewer Modal
        uiState.activeStory?.let { story ->
            StoryViewerModal(
                story = story,
                onClose = { viewModel.closeStory() },
                onNextStory = { viewModel.closeStory() }
            )
        }

        // Fullscreen Media Detail Modal
        uiState.activeDetailMedia?.let { media ->
            MediaDetailModal(
                media = media,
                onClose = { viewModel.closeMediaDetail() }
            )
        }

        // Comments Bottom Sheet
        if (activeCommentsPost != null) {
            CommentsBottomSheet(
                post = activeCommentsPost,
                comments = activeComments,
                onAddComment = { text ->
                    viewModel.addComment(activeCommentsPost.id, text)
                },
                onDismiss = { viewModel.closeComments() },
                sheetState = commentsSheetState
            )
        }
    }
}
