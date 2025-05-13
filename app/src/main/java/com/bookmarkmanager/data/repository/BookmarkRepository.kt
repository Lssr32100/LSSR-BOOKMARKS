package com.bookmarkmanager.data.repository

import com.bookmarkmanager.data.local.dao.BookmarkDao
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    suspend fun insertBookmark(bookmark: Bookmark): Long {
        return bookmarkDao.insertBookmark(bookmark)
    }
    
    suspend fun updateBookmark(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }
    
    suspend fun deleteBookmark(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark)
    }
    
    suspend fun getBookmarkById(id: Long): Bookmark? {
        return bookmarkDao.getBookmarkById(id)
    }
    
    fun getAllBookmarksOrderByNameAsc(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarksOrderByNameAsc()
    }
    
    fun getAllBookmarksOrderByNameDesc(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarksOrderByNameDesc()
    }
    
    fun getAllBookmarksOrderByCategory(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarksOrderByCategory()
    }
    
    fun getAllBookmarksOrderBySubcategory(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarksOrderBySubcategory()
    }
    
    fun getAllBookmarksOrderByType(): Flow<List<Bookmark>> {
        return bookmarkDao.getAllBookmarksOrderByType()
    }
    
    fun getBookmarksByCategory(categoryId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByCategory(categoryId)
    }
    
    fun getBookmarksBySubcategory(subcategoryId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksBySubcategory(subcategoryId)
    }
    
    fun getBookmarksByType(type: BookmarkType): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByType(type)
    }
    
    fun searchBookmarks(query: String): Flow<List<Bookmark>> {
        return bookmarkDao.searchBookmarks(query)
    }
    
    suspend fun getAllBookmarksOnce(): List<Bookmark> {
        return bookmarkDao.getAllBookmarksOnce()
    }
    
    suspend fun deleteAllBookmarks() {
        bookmarkDao.deleteAllBookmarks()
    }
}
