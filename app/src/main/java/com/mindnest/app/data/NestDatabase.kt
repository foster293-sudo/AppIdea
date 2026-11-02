package com.mindnest.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Folder::class, Idea::class], version = 1, exportSchema = false)
abstract class NestDatabase : RoomDatabase() {
    abstract fun dao(): NestDao

    companion object {
        @Volatile
        private var instance: NestDatabase? = null

        fun get(context: Context): NestDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    NestDatabase::class.java,
                    "mindnest.db",
                )
                    .addCallback(SeedCallback)
                    .build()
                    .also { instance = it }
            }
    }

    /** A few starter folders so the app never opens to a blank, intimidating screen. */
    private object SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            val seeds = listOf(
                Triple("Projects", "🚀", 0),
                Triple("To learn", "📚", 2),
                Triple("Someday", "🌙", 6),
            )
            seeds.forEach { (name, emoji, color) ->
                db.execSQL(
                    "INSERT INTO folders (parentId, name, emoji, colorIndex, createdAt) VALUES (NULL, ?, ?, ?, ?)",
                    arrayOf<Any>(name, emoji, color, now),
                )
            }
            db.execSQL(
                "INSERT INTO ideas (folderId, text, pinned, createdAt, updatedAt) VALUES (NULL, ?, 0, ?, ?)",
                arrayOf<Any>(
                    "Welcome to Mindnest 👋\n\nTap Capture whenever a thought pops up. It lands here in your Inbox — sort it into a folder later, when you have the headspace.",
                    now,
                    now,
                ),
            )
        }
    }
}
