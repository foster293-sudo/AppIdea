package com.mindnest.app.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A folder can live at the top level (parentId == null) or inside another folder,
 * which gives an unlimited-depth hierarchy.
 */
@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("parentId")],
)
data class Folder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentId: Long? = null,
    val name: String,
    val emoji: String = "📁",
    val colorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

/** An idea with folderId == null sits in the Inbox, waiting to be sorted. */
@Entity(
    tableName = "ideas",
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("folderId")],
)
data class Idea(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long? = null,
    val text: String,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

data class FolderWithCounts(
    @Embedded val folder: Folder,
    val ideaCount: Int,
    val folderCount: Int,
)
