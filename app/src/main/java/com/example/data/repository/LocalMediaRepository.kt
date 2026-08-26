package com.example.data.repository

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.AppDatabase
import com.example.data.local.CommentEntity
import com.example.data.local.CustomPostEntity
import com.example.data.local.PostInteractionEntity
import com.example.data.model.InstagramPost
import com.example.data.model.InstagramStory
import com.example.data.model.MediaComment
import com.example.data.model.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class LocalMediaRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val dao = database.interactionDao()

    /**
     * Query all images and videos from device MediaStore
     */
    suspend fun queryDeviceMedia(): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaList = mutableListOf<MediaItem>()

        // 1. Fetch Images
        val imageProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )
        val imageSortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                imageProjection,
                null,
                null,
                imageSortOrder
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val mimeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val bucketCol = c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthCol = c.getColumnIndex(MediaStore.Images.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Images.Media.HEIGHT)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val name = c.getString(nameCol) ?: "IMG_$id"
                    val mime = c.getString(mimeCol) ?: "image/jpeg"
                    val dateAdded = c.getLong(dateCol)
                    val bucket = if (bucketCol != -1) c.getString(bucketCol) ?: "Camera" else "Camera"
                    val size = c.getLong(sizeCol)
                    val width = if (widthCol != -1) c.getInt(widthCol) else 1080
                    val height = if (heightCol != -1) c.getInt(heightCol) else 1080

                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    mediaList.add(
                        MediaItem(
                            id = id,
                            uri = uri,
                            name = name,
                            mimeType = mime,
                            isVideo = false,
                            durationMs = 0,
                            dateAddedSec = dateAdded,
                            bucketName = bucket,
                            sizeBytes = size,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fetch Videos
        val videoProjection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT
        )
        val videoSortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                videoProjection,
                null,
                null,
                videoSortOrder
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val mimeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val bucketCol = c.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val durCol = c.getColumnIndex(MediaStore.Video.Media.DURATION)
                val widthCol = c.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Video.Media.HEIGHT)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val name = c.getString(nameCol) ?: "VID_$id"
                    val mime = c.getString(mimeCol) ?: "video/mp4"
                    val dateAdded = c.getLong(dateCol)
                    val bucket = if (bucketCol != -1) c.getString(bucketCol) ?: "Videos" else "Videos"
                    val size = c.getLong(sizeCol)
                    val duration = if (durCol != -1) c.getLong(durCol) else 0L
                    val width = if (widthCol != -1) c.getInt(widthCol) else 1080
                    val height = if (heightCol != -1) c.getInt(heightCol) else 1920

                    // Use negative id or offset for videos to ensure unique IDs across both tables
                    val uniqueId = -(id + 1000000L)
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    mediaList.add(
                        MediaItem(
                            id = uniqueId,
                            uri = uri,
                            name = name,
                            mimeType = mime,
                            isVideo = true,
                            durationMs = duration,
                            dateAddedSec = dateAdded,
                            bucketName = bucket,
                            sizeBytes = size,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort all mixed media chronologically descending
        mediaList.sortByDescending { it.dateAddedSec }
        mediaList
    }

    /**
     * Map device media into rich Instagram Feed Posts, combined with Room likes/bookmarks/captions
     */
    fun mapToInstagramPosts(
        mediaList: List<MediaItem>,
        interactions: Map<Long, PostInteractionEntity>
    ): List<InstagramPost> {
        val usernames = listOf(
            "gallery.official",
            "camera_shots",
            "local.lens",
            "creative_moments",
            "device_memories",
            "daily_visuals",
            "capture_life",
            "urban_explorer",
            "sunset_vibes",
            "frame_of_mind"
        )

        return mediaList.mapIndexed { index, media ->
            val interaction = interactions[media.id]
            val pseudoRandomLikes = (abs(media.id) % 150 + 12).toInt()
            val isLiked = interaction?.isLiked ?: false
            val isSaved = interaction?.isSaved ?: false
            val likesCount = if (isLiked) (interaction?.likesCount ?: (pseudoRandomLikes + 1)) else pseudoRandomLikes
            
            // Format nice caption
            val dateStr = formatTimestamp(media.dateAddedSec)
            val baseCaption = if (media.isVideo) {
                "🎬 Moment captured in ${media.bucketName} • ${media.durationFormatted} #video #gallery #moments"
            } else {
                "📸 Captured locally in ${media.bucketName} • ${media.name.substringBeforeLast(".")} #photography #memories #local"
            }
            val caption = interaction?.customCaption ?: baseCaption

            val author = if (media.bucketName.isNotBlank() && media.bucketName != "0") {
                media.bucketName.lowercase().replace(" ", "_") + ".lens"
            } else {
                usernames[index % usernames.size]
            }

            InstagramPost(
                id = media.id,
                media = media,
                authorUsername = author,
                authorAvatarUri = media.uri,
                location = "${media.bucketName} • $dateStr",
                caption = caption,
                timestampSec = media.dateAddedSec,
                likesCount = likesCount,
                isLiked = isLiked,
                isSaved = isSaved,
                commentsCount = (abs(media.id) % 8).toInt()
            )
        }
    }

    /**
     * Group media by bucket/album to create Instagram Story rings
     */
    fun buildStoriesFromMedia(mediaList: List<MediaItem>): List<InstagramStory> {
        val stories = mutableListOf<InstagramStory>()
        
        // 1. "Your Story" (latest device media)
        if (mediaList.isNotEmpty()) {
            stories.add(
                InstagramStory(
                    id = "user_story",
                    title = "قصتك",
                    coverUri = mediaList.first().uri,
                    items = mediaList.take(5),
                    isSeen = false,
                    isUserStory = true
                )
            )
        }

        // 2. Group by album / folder name
        val groupedByBucket = mediaList.groupBy { it.bucketName }
        groupedByBucket.forEach { (bucket, items) ->
            if (bucket.isNotBlank() && bucket != "0") {
                stories.add(
                    InstagramStory(
                        id = "story_$bucket",
                        title = bucket,
                        coverUri = items.firstOrNull()?.uri,
                        items = items.take(8),
                        isSeen = false,
                        isUserStory = false
                    )
                )
            }
        }

        return stories
    }

    fun getAllInteractionsFlow(): Flow<Map<Long, PostInteractionEntity>> {
        return dao.getAllInteractions().map { list ->
            list.associateBy { it.mediaId }
        }
    }

    suspend fun toggleLike(mediaId: Long, currentLikes: Int, isLiked: Boolean) {
        val existing = dao.getInteraction(mediaId)
        val newIsLiked = !isLiked
        val newCount = if (newIsLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)
        val updated = (existing ?: PostInteractionEntity(mediaId = mediaId)).copy(
            isLiked = newIsLiked,
            likesCount = newCount
        )
        dao.upsertInteraction(updated)
    }

    suspend fun toggleSave(mediaId: Long, isSaved: Boolean) {
        val existing = dao.getInteraction(mediaId)
        val updated = (existing ?: PostInteractionEntity(mediaId = mediaId)).copy(
            isSaved = !isSaved
        )
        dao.upsertInteraction(updated)
    }

    suspend fun updateCaption(mediaId: Long, caption: String) {
        val existing = dao.getInteraction(mediaId)
        val updated = (existing ?: PostInteractionEntity(mediaId = mediaId)).copy(
            customCaption = caption
        )
        dao.upsertInteraction(updated)
    }

    fun getCommentsForMedia(mediaId: Long): Flow<List<MediaComment>> {
        return dao.getCommentsForMedia(mediaId).map { entities ->
            entities.map {
                MediaComment(
                    id = it.id,
                    mediaId = it.mediaId,
                    authorName = it.authorName,
                    text = it.commentText,
                    timestamp = it.timestamp
                )
            }
        }
    }

    suspend fun addComment(mediaId: Long, authorName: String, text: String) {
        if (text.isBlank()) return
        dao.insertComment(
            CommentEntity(
                mediaId = mediaId,
                authorName = authorName,
                commentText = text
            )
        )
    }

    fun getCustomPosts(): Flow<List<CustomPostEntity>> = dao.getCustomPosts()

    suspend fun createCustomPost(uri: String, isVideo: Boolean, caption: String, filter: String) {
        dao.insertCustomPost(
            CustomPostEntity(
                mediaUri = uri,
                isVideo = isVideo,
                caption = caption,
                filterName = filter
            )
        )
    }

    private fun formatTimestamp(sec: Long): String {
        if (sec <= 0) return "الآن"
        val date = Date(sec * 1000)
        val now = System.currentTimeMillis()
        val diffHours = (now - date.time) / (1000 * 60 * 60)
        return when {
            diffHours < 1 -> "منذ قليل"
            diffHours < 24 -> "منذ $diffHours ساعة"
            diffHours < 48 -> "أمس"
            else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
        }
    }
}
