package com.mindnest.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NestDao {

    // ---- Folders ----

    @Query(
        """
        SELECT f.*,
            (SELECT COUNT(*) FROM ideas i WHERE i.folderId = f.id) AS ideaCount,
            (SELECT COUNT(*) FROM folders c WHERE c.parentId = f.id) AS folderCount
        FROM folders f
        WHERE f.parentId IS :parentId
        ORDER BY f.name COLLATE NOCASE
        """
    )
    fun childFolders(parentId: Long?): Flow<List<FolderWithCounts>>

    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE")
    fun allFolders(): Flow<List<Folder>>

    @Insert
    suspend fun insertFolder(folder: Folder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<Folder>)

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("UPDATE folders SET parentId = :parentId WHERE id = :id")
    suspend fun moveFolder(id: Long, parentId: Long?)

    // ---- Ideas ----

    @Query("SELECT * FROM ideas WHERE folderId IS :folderId ORDER BY pinned DESC, updatedAt DESC")
    fun ideasIn(folderId: Long?): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE folderId IN (:folderIds)")
    suspend fun ideasInFolders(folderIds: List<Long>): List<Idea>

    @Query("SELECT COUNT(*) FROM ideas WHERE folderId IS NULL")
    fun inboxCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ideas")
    fun totalIdeas(): Flow<Int>

    @Query("SELECT * FROM ideas WHERE text LIKE '%' || :query || '%' ORDER BY updatedAt DESC LIMIT 200")
    fun searchIdeas(query: String): Flow<List<Idea>>

    @Query("SELECT * FROM ideas WHERE id != :excludeId ORDER BY RANDOM() LIMIT 1")
    suspend fun randomIdea(excludeId: Long): Idea?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIdea(idea: Idea): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdeas(ideas: List<Idea>)

    @Delete
    suspend fun deleteIdea(idea: Idea)

    @Query("UPDATE ideas SET folderId = :folderId, updatedAt = :now WHERE id = :id")
    suspend fun moveIdea(id: Long, folderId: Long?, now: Long)

    @Query("UPDATE ideas SET pinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)
}
