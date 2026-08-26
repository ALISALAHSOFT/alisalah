package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.InstagramPost
import com.example.data.model.InstagramStory
import com.example.data.model.MediaComment
import com.example.data.model.MediaItem
import com.example.data.repository.LocalMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME, EXPLORE, CREATE, REELS, PROFILE
}

data class UiState(
    val hasPermission: Boolean = false,
    val isLoading: Boolean = false,
    val mediaList: List<MediaItem> = emptyList(),
    val posts: List<InstagramPost> = emptyList(),
    val stories: List<InstagramStory> = emptyList(),
    val savedPosts: List<InstagramPost> = emptyList(),
    val albums: List<String> = emptyList(),
    val selectedAlbum: String = "الكل",
    val searchQuery: String = "",
    val activeTab: MainTab = MainTab.HOME,
    val activeStory: InstagramStory? = null,
    val activeCommentsPost: InstagramPost? = null,
    val activeDetailMediaList: List<MediaItem> = emptyList(),
    val activeDetailIndex: Int = 0,
    val activeDetailMedia: MediaItem? = null,
    val isPostCreatedDialogVisible: Boolean = false,
    val userProfileHandle: String = "local.collector",
    val userProfileName: String = "معرض جهازي 📷",
    val userBio: String = "✨ استعراض وسائط الجهاز بأسلوب انستقرام الأصلي\n🔒 حفظ محلي وآمن 100% بدون سحابة"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            AppDatabase::class.java,
            "localgram_db"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository by lazy {
        LocalMediaRepository(application.applicationContext, database)
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _rawMediaList = MutableStateFlow<List<MediaItem>>(emptyList())

    init {
        // Collect interactions and combine with media list
        viewModelScope.launch {
            combine(_rawMediaList, repository.getAllInteractionsFlow()) { media, interactions ->
                val posts = repository.mapToInstagramPosts(media, interactions)
                val stories = repository.buildStoriesFromMedia(media)
                val savedPosts = posts.filter { it.isSaved }
                val albums = listOf("الكل") + media.map { it.bucketName }.distinct().filter { it.isNotBlank() }

                _uiState.value.copy(
                    mediaList = media,
                    posts = posts,
                    stories = stories,
                    savedPosts = savedPosts,
                    albums = albums
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasPermission = granted)
        if (granted) {
            loadDeviceMedia()
        }
    }

    fun loadDeviceMedia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val media = repository.queryDeviceMedia()
            _rawMediaList.value = media
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectTab(tab: MainTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun selectAlbum(album: String) {
        _uiState.value = _uiState.value.copy(selectedAlbum = album)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleLike(post: InstagramPost) {
        viewModelScope.launch {
            repository.toggleLike(post.id, post.likesCount, post.isLiked)
        }
    }

    fun toggleSave(post: InstagramPost) {
        viewModelScope.launch {
            repository.toggleSave(post.id, post.isSaved)
        }
    }

    fun openStory(story: InstagramStory) {
        _uiState.value = _uiState.value.copy(activeStory = story)
    }

    fun closeStory() {
        _uiState.value = _uiState.value.copy(activeStory = null)
    }

    fun openComments(post: InstagramPost) {
        _uiState.value = _uiState.value.copy(activeCommentsPost = post)
    }

    fun closeComments() {
        _uiState.value = _uiState.value.copy(activeCommentsPost = null)
    }

    fun getCommentsForPost(mediaId: Long): StateFlow<List<MediaComment>> {
        return repository.getCommentsForMedia(mediaId).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun addComment(mediaId: Long, text: String) {
        viewModelScope.launch {
            repository.addComment(mediaId, _uiState.value.userProfileHandle, text)
        }
    }

    fun openMediaDetail(media: MediaItem, list: List<MediaItem>? = null) {
        val targetList = list ?: _uiState.value.mediaList
        val index = targetList.indexOfFirst { it.id == media.id }.coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(
            activeDetailMedia = media,
            activeDetailMediaList = if (targetList.isNotEmpty()) targetList else listOf(media),
            activeDetailIndex = index
        )
    }

    fun closeMediaDetail() {
        _uiState.value = _uiState.value.copy(
            activeDetailMedia = null,
            activeDetailMediaList = emptyList(),
            activeDetailIndex = 0
        )
    }

    fun publishNewPost(uri: String, isVideo: Boolean, caption: String, filter: String) {
        viewModelScope.launch {
            repository.createCustomPost(uri, isVideo, caption, filter)
            loadDeviceMedia()
            selectTab(MainTab.HOME)
        }
    }
}
