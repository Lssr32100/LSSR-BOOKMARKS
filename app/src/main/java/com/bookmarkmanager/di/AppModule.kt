package com.bookmarkmanager.di

import android.content.Context
import com.bookmarkmanager.data.local.BookmarkDatabase
import com.bookmarkmanager.data.local.dao.BookmarkDao
import com.bookmarkmanager.data.local.dao.CategoryDao
import com.bookmarkmanager.data.local.dao.SubcategoryDao
import com.bookmarkmanager.data.repository.BookmarkRepository
import com.bookmarkmanager.data.repository.CategoryRepository
import com.bookmarkmanager.data.repository.SubcategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
    
    @Provides
    @Singleton
    fun provideBookmarkDatabase(
        @ApplicationContext context: Context,
        scope: CoroutineScope
    ): BookmarkDatabase {
        return BookmarkDatabase.getDatabase(context, scope)
    }
    
    @Provides
    @Singleton
    fun provideCategoryDao(database: BookmarkDatabase): CategoryDao {
        return database.categoryDao()
    }
    
    @Provides
    @Singleton
    fun provideSubcategoryDao(database: BookmarkDatabase): SubcategoryDao {
        return database.subcategoryDao()
    }
    
    @Provides
    @Singleton
    fun provideBookmarkDao(database: BookmarkDatabase): BookmarkDao {
        return database.bookmarkDao()
    }
    
    @Provides
    @Singleton
    fun provideCategoryRepository(categoryDao: CategoryDao): CategoryRepository {
        return CategoryRepository(categoryDao)
    }
    
    @Provides
    @Singleton
    fun provideSubcategoryRepository(subcategoryDao: SubcategoryDao): SubcategoryRepository {
        return SubcategoryRepository(subcategoryDao)
    }
    
    @Provides
    @Singleton
    fun provideBookmarkRepository(bookmarkDao: BookmarkDao): BookmarkRepository {
        return BookmarkRepository(bookmarkDao)
    }
}