package com.bookmarkmanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bookmarkmanager.data.local.dao.BookmarkDao
import com.bookmarkmanager.data.local.dao.CategoryDao
import com.bookmarkmanager.data.local.dao.SubcategoryDao
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Category::class, Subcategory::class, Bookmark::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BookmarkDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun subcategoryDao(): SubcategoryDao
    abstract fun bookmarkDao(): BookmarkDao
    
    companion object {
        @Volatile
        private var INSTANCE: BookmarkDatabase? = null
        
        fun getDatabase(context: Context, scope: CoroutineScope): BookmarkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BookmarkDatabase::class.java,
                    "bookmark_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(BookmarkDatabaseCallback(scope))
                .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        private class BookmarkDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(
                            database.categoryDao(),
                            database.subcategoryDao(),
                            database.bookmarkDao()
                        )
                    }
                }
            }
        }
        
        // Populate the database with initial data
        private suspend fun populateDatabase(
            categoryDao: CategoryDao,
            subcategoryDao: SubcategoryDao,
            bookmarkDao: BookmarkDao
        ) {
            // Create default categories
            val websitesId = categoryDao.insertCategory(Category(name = "Websites"))
            val appsId = categoryDao.insertCategory(Category(name = "Apps"))
            val toolsId = categoryDao.insertCategory(Category(name = "Tools"))
            
            // Create default subcategories
            val socialMediaId = subcategoryDao.insertSubcategory(Subcategory(name = "Social Media", category_id = websitesId.toInt()))
            val newsId = subcategoryDao.insertSubcategory(Subcategory(name = "News", category_id = websitesId.toInt()))
            
            val productivityId = subcategoryDao.insertSubcategory(Subcategory(name = "Productivity", category_id = appsId.toInt()))
            val entertainmentId = subcategoryDao.insertSubcategory(Subcategory(name = "Entertainment", category_id = appsId.toInt()))
            
            val devToolsId = subcategoryDao.insertSubcategory(Subcategory(name = "Development", category_id = toolsId.toInt()))
            val designToolsId = subcategoryDao.insertSubcategory(Subcategory(name = "Design", category_id = toolsId.toInt()))
            
            // Create default bookmarks
            bookmarkDao.insertBookmark(
                Bookmark(
                    name = "Twitter",
                    url = "https://twitter.com",
                    description = "Social media platform",
                    category_id = websitesId.toInt(),
                    subcategory_id = socialMediaId.toInt(),
                    type = BookmarkType.FREE
                )
            )
            
            bookmarkDao.insertBookmark(
                Bookmark(
                    name = "CNN",
                    url = "https://cnn.com",
                    description = "News website",
                    category_id = websitesId.toInt(),
                    subcategory_id = newsId.toInt(),
                    type = BookmarkType.FREE
                )
            )
            
            bookmarkDao.insertBookmark(
                Bookmark(
                    name = "Microsoft Office",
                    url = "https://office.com",
                    description = "Office suite",
                    category_id = appsId.toInt(),
                    subcategory_id = productivityId.toInt(),
                    type = BookmarkType.PAID
                )
            )
            
            bookmarkDao.insertBookmark(
                Bookmark(
                    name = "Spotify",
                    url = "https://spotify.com",
                    description = "Music streaming",
                    category_id = appsId.toInt(),
                    subcategory_id = entertainmentId.toInt(),
                    type = BookmarkType.FREEMIUM
                )
            )
            
            bookmarkDao.insertBookmark(
                Bookmark(
                    name = "Visual Studio Code",
                    url = "https://code.visualstudio.com",
                    description = "Code editor",
                    category_id = toolsId.toInt(),
                    subcategory_id = devToolsId.toInt(),
                    type = BookmarkType.FREE
                )
            )
            
            bookmarkDao.insertBookmark(
                Bookmark(
                    name = "Adobe Photoshop",
                    url = "https://adobe.com/photoshop",
                    description = "Image editing software",
                    category_id = toolsId.toInt(),
                    subcategory_id = designToolsId.toInt(),
                    type = BookmarkType.PAID
                )
            )
        }
    }
}