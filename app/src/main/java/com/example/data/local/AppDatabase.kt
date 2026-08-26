package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "post_interactions")
data class PostInteractionEntity(
    @PrimaryKey val mediaId: Long,
    val isLiked: Boolean = false,
    val likesCount: Int = 0,
    val isSaved: Boolean = false,
    val customCaption: String? = null
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaId: Long,
    val authorName: String,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_posts")
data class CustomPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaUri: String,
    val isVideo: Boolean,
    val caption: String,
    val filterName: String = "Normal",
    val location: String = "Local Gallery",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface MediaInteractionDao {
    @Query("SELECT * FROM post_interactions")
    fun getAllInteractions(): Flow<List<PostInteractionEntity>>

    @Query("SELECT * FROM post_interactions WHERE mediaId = :id")
    suspend fun getInteraction(id: Long): PostInteractionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInteraction(interaction: PostInteractionEntity)

    @Query("SELECT * FROM comments WHERE mediaId = :mediaId ORDER BY timestamp ASC")
    fun getCommentsForMedia(mediaId: Long): Flow<List<CommentEntity>>

    @Insert
    suspend fun insertComment(comment: CommentEntity)

    @Query("SELECT * FROM custom_posts ORDER BY timestamp DESC")
    fun getCustomPosts(): Flow<List<CustomPostEntity>>

    @Insert
    suspend fun insertCustomPost(post: CustomPostEntity)
}

@Database(
    entities = [PostInteractionEntity::class, CommentEntity::class, CustomPostEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun interactionDao(): MediaInteractionDao
}
