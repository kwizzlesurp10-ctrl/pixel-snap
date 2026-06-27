package com.pixelsnap.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Snap::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SnapDatabase : RoomDatabase() {
    abstract fun snapDao(): SnapDao

    companion object {
        @Volatile
        private var INSTANCE: SnapDatabase? = null

        fun getDatabase(context: Context): SnapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SnapDatabase::class.java,
                    "pixelsnap_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
