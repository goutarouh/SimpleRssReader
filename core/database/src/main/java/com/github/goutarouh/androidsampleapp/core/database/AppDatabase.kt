package com.github.goutarouh.androidsampleapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.goutarouh.androidsampleapp.core.database.dao.RssDao
import com.github.goutarouh.androidsampleapp.core.database.model.rss.RssEntity
import com.github.goutarouh.androidsampleapp.core.database.model.rss.RssItemEntity

@Database(
    entities = [
        RssEntity::class,
        RssItemEntity::class
    ],
    version = 1,
    exportSchema = false // TODO trueにする
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun rssDao(): RssDao
}