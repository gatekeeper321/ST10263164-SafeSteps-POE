package com.fake.safesteps.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CachedAlert::class, CachedContact::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AlertDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AlertDatabase? = null

        fun getDatabase(context: Context): AlertDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlertDatabase::class.java,
                    "safesteps_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}