package com.example.data.local

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey val token: String,
    val userId: String,
    val username: String,
    val name: String?,
    val email: String?,
    val avatar: String?,
    val isActive: Boolean,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface TokenDao {
    @Query("SELECT * FROM tokens WHERE isActive = 1 LIMIT 1")
    fun getActiveToken(): Flow<TokenEntity?>

    @Query("SELECT * FROM tokens WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveTokenSync(): TokenEntity?

    @Query("SELECT * FROM tokens ORDER BY addedAt DESC")
    fun getAllTokens(): Flow<List<TokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: TokenEntity)

    @Query("UPDATE tokens SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE tokens SET isActive = 1 WHERE token = :token")
    suspend fun activateToken(token: String)

    @Transaction
    suspend fun setActiveToken(tokenEntity: TokenEntity) {
        deactivateAll()
        insertToken(tokenEntity.copy(isActive = true))
    }

    @Query("DELETE FROM tokens WHERE token = :token")
    suspend fun deleteToken(token: String)

    @Query("DELETE FROM tokens")
    suspend fun clearAll()
}

@Entity(tableName = "cached_projects")
data class CachedProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val framework: String?,
    val updatedAt: Long,
    val productionUrl: String?,
    val productionState: String?,
    val repoName: String?,
    val teamId: String?
)

@Dao
interface CachedProjectDao {
    @Query("SELECT * FROM cached_projects WHERE (:teamId IS NULL AND teamId IS NULL) OR teamId = :teamId ORDER BY updatedAt DESC")
    fun getProjects(teamId: String?): Flow<List<CachedProjectEntity>>

    @Query("SELECT * FROM cached_projects WHERE (:teamId IS NULL AND teamId IS NULL) OR teamId = :teamId ORDER BY updatedAt DESC")
    suspend fun getProjectsSync(teamId: String?): List<CachedProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<CachedProjectEntity>)

    @Query("DELETE FROM cached_projects WHERE (:teamId IS NULL AND teamId IS NULL) OR teamId = :teamId")
    suspend fun clearProjects(teamId: String?)
}

@Database(
    entities = [TokenEntity::class, CachedProjectEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
    abstract fun cachedProjectDao(): CachedProjectDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vercel_dashboard_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
