package com.example.data.model

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val isVideo: Boolean,
    val durationMs: Long = 0,
    val dateAddedSec: Long = 0,
    val bucketName: String = "Gallery",
    val sizeBytes: Long = 0,
    val width: Int = 0,
    val height: Int = 0
) {
    val durationFormatted: String
        get() {
            if (!isVideo || durationMs <= 0) return ""
            val totalSec = durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            return "%d:%02d".format(min, sec)
        }
}

data class InstagramPost(
    val id: Long,
    val media: MediaItem,
    val authorUsername: String,
    val authorAvatarUri: Uri?,
    val location: String,
    val caption: String,
    val timestampSec: Long,
    val likesCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean,
    val commentsCount: Int = 0
)

data class InstagramStory(
    val id: String,
    val title: String,
    val coverUri: Uri?,
    val items: List<MediaItem>,
    val isSeen: Boolean = false,
    val isUserStory: Boolean = false
)

data class MediaComment(
    val id: Long,
    val mediaId: Long,
    val authorName: String,
    val text: String,
    val timestamp: Long
)
