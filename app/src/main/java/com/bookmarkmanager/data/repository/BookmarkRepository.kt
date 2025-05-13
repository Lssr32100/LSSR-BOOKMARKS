package com.bookmarkmanager.data.repository

import com.bookmarkmanager.data.local.dao.BookmarkDao
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(private val bookmarkDao: BookmarkDao) {
    
    val allBookmarks: Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
    
    fun getBookmarksByCategory(categoryId: Int): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByCategory(categoryId)
    }
    
    fun getBookmarksBySubcategory(subcategoryId: Int): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksBySubcategory(subcategoryId)
    }
    
    fun getBookmarksByType(type: BookmarkType): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByType(type)
    }
    
    fun searchBookmarks(query: String): Flow<List<Bookmark>> {
        return bookmarkDao.searchBookmarks(query)
    }
    
    fun searchBookmarksWithCategoryAndSubcategory(query: String): Flow<List<Bookmark>> {
        return bookmarkDao.searchBookmarksWithCategoryAndSubcategory(query)
    }
    
    suspend fun insert(bookmark: Bookmark): Long {
        return bookmarkDao.insertBookmark(bookmark)
    }
    
    suspend fun update(bookmark: Bookmark) {
        bookmarkDao.updateBookmark(bookmark)
    }
    
    suspend fun delete(bookmark: Bookmark) {
        bookmarkDao.deleteBookmark(bookmark)
    }
    
    suspend fun deleteById(bookmarkId: Int) {
        bookmarkDao.deleteBookmarkById(bookmarkId)
    }
    
    suspend fun getBookmarkById(bookmarkId: Int): Bookmark? {
        return bookmarkDao.getBookmarkById(bookmarkId)
    }
}