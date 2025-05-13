package com.bookmarkmanager.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.repository.BookmarkRepository
import com.bookmarkmanager.data.repository.CategoryRepository
import com.bookmarkmanager.data.repository.SubcategoryRepository
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookmarkRepository: BookmarkRepository,
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // Data classes for export/import that don't have Room-specific annotations
    data class ExportCategory(
        val id: Long,
        val name: String
    )
    
    data class ExportSubcategory(
        val id: Long,
        val name: String,
        val categoryId: Long
    )
    
    data class ExportBookmark(
        val id: Long,
        val name: String,
        val url: String?,
        val description: String?,
        val categoryId: Long,
        val subcategoryId: Long,
        val type: String,
        val createdAt: Long,
        val updatedAt: Long
    )
    
    data class ExportData(
        val categories: List<ExportCategory>,
        val subcategories: List<ExportSubcategory>,
        val bookmarks: List<ExportBookmark>
    )
    
    // Export to JSON
    suspend fun exportToJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val exportData = createExportData()
            
            val jsonAdapter = moshi.adapter(ExportData::class.java)
            val json = jsonAdapter.toJson(exportData)
            
            val file = saveToFile(json, "bookmarks.json")
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Export to CSV
    suspend fun exportToCsv(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val exportData = createExportData()
            
            // Create a directory to store CSV files
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "bookmark_export")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            // Export categories
            val categoriesFile = File(directory, "categories.csv")
            csvWriter().open(categoriesFile) {
                writeRow("id", "name")
                exportData.categories.forEach { category ->
                    writeRow(category.id, category.name)
                }
            }
            
            // Export subcategories
            val subcategoriesFile = File(directory, "subcategories.csv")
            csvWriter().open(subcategoriesFile) {
                writeRow("id", "name", "categoryId")
                exportData.subcategories.forEach { subcategory ->
                    writeRow(subcategory.id, subcategory.name, subcategory.categoryId)
                }
            }
            
            // Export bookmarks
            val bookmarksFile = File(directory, "bookmarks.csv")
            csvWriter().open(bookmarksFile) {
                writeRow("id", "name", "url", "description", "categoryId", "subcategoryId", "type", "createdAt", "updatedAt")
                exportData.bookmarks.forEach { bookmark ->
                    writeRow(
                        bookmark.id,
                        bookmark.name,
                        bookmark.url ?: "",
                        bookmark.description ?: "",
                        bookmark.categoryId,
                        bookmark.subcategoryId,
                        bookmark.type,
                        bookmark.createdAt,
                        bookmark.updatedAt
                    )
                }
            }
            
            // Create a ZIP file (simplified, just returning the directory path)
            Result.success(directory.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Import from JSON
    suspend fun importFromJson(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: throw IOException("Could not read file")
            
            val jsonAdapter = moshi.adapter(ExportData::class.java)
            val exportData = jsonAdapter.fromJson(json) ?: throw IOException("Invalid JSON format")
            
            importData(exportData)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Import from CSV
    suspend fun importFromCsv(uri: Uri): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IOException("Could not read file")
            
            // Assuming a single CSV file with all data (simplification)
            val lines = inputStream.bufferedReader().readLines()
            
            // Simple parsing logic (this would need to be enhanced for a real app)
            val categories = mutableListOf<ExportCategory>()
            val subcategories = mutableListOf<ExportSubcategory>()
            val bookmarks = mutableListOf<ExportBookmark>()
            
            // Parse header and rows (simplified)
            val header = lines[0].split(",")
            
            if (header.contains("categoryId") && header.contains("subcategoryId")) {
                // This is the bookmarks file
                for (i in 1 until lines.size) {
                    val values = csvReader().readAll(lines[i])[0]
                    if (values.size >= 9) {
                        bookmarks.add(
                            ExportBookmark(
                                id = values[0].toLong(),
                                name = values[1],
                                url = if (values[2].isBlank()) null else values[2],
                                description = if (values[3].isBlank()) null else values[3],
                                categoryId = values[4].toLong(),
                                subcategoryId = values[5].toLong(),
                                type = values[6],
                                createdAt = values[7].toLong(),
                                updatedAt = values[8].toLong()
                            )
                        )
                    }
                }
            }
            
            // Create export data object
            val exportData = ExportData(categories, subcategories, bookmarks)
            
            // Import data
            importData(exportData)
            
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods
    private suspend fun createExportData(): ExportData {
        val categories = categoryRepository.getAllCategoriesOnce().map { category ->
            ExportCategory(
                id = category.id,
                name = category.name
            )
        }
        
        val subcategories = subcategoryRepository.getAllSubcategoriesOnce().map { subcategory ->
            ExportSubcategory(
                id = subcategory.id,
                name = subcategory.name,
                categoryId = subcategory.categoryId
            )
        }
        
        val bookmarks = bookmarkRepository.getAllBookmarksOnce().map { bookmark ->
            ExportBookmark(
                id = bookmark.id,
                name = bookmark.name,
                url = bookmark.url,
                description = bookmark.description,
                categoryId = bookmark.categoryId,
                subcategoryId = bookmark.subcategoryId,
                type = bookmark.type.name,
                createdAt = bookmark.createdAt,
                updatedAt = bookmark.updatedAt
            )
        }
        
        return ExportData(categories, subcategories, bookmarks)
    }
    
    private suspend fun importData(exportData: ExportData) {
        // Clear existing data
        bookmarkRepository.deleteAllBookmarks()
        subcategoryRepository.deleteAllSubcategories()
        categoryRepository.deleteAllCategories()
        
        // Import categories
        val categoryIdMap = mutableMapOf<Long, Long>()
        exportData.categories.forEach { exportCategory ->
            val newId = categoryRepository.insertCategory(
                Category(
                    name = exportCategory.name
                )
            )
            categoryIdMap[exportCategory.id] = newId
        }
        
        // Import subcategories
        val subcategoryIdMap = mutableMapOf<Long, Long>()
        exportData.subcategories.forEach { exportSubcategory ->
            val categoryId = categoryIdMap[exportSubcategory.categoryId] ?: return@forEach
            
            val newId = subcategoryRepository.insertSubcategory(
                Subcategory(
                    name = exportSubcategory.name,
                    categoryId = categoryId
                )
            )
            subcategoryIdMap[exportSubcategory.id] = newId
        }
        
        // Import bookmarks
        exportData.bookmarks.forEach { exportBookmark ->
            val categoryId = categoryIdMap[exportBookmark.categoryId] ?: return@forEach
            val subcategoryId = subcategoryIdMap[exportBookmark.subcategoryId] ?: return@forEach
            
            bookmarkRepository.insertBookmark(
                Bookmark(
                    name = exportBookmark.name,
                    url = exportBookmark.url,
                    description = exportBookmark.description,
                    categoryId = categoryId,
                    subcategoryId = subcategoryId,
                    type = BookmarkType.valueOf(exportBookmark.type),
                    createdAt = exportBookmark.createdAt,
                    updatedAt = exportBookmark.updatedAt
                )
            )
        }
    }
    
    private fun saveToFile(content: String, fileName: String): File {
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray())
        }
        return file
    }
}
