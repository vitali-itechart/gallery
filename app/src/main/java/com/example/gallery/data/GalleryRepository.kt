package com.example.gallery.data

import android.content.Context
import com.example.gallery.data.entity.Folder

import java.io.File

import android.os.Environment
import android.graphics.Bitmap
import com.example.gallery.data.entity.Image

class GalleryRepository(private val context: Context) {

    fun getContent(callback: (Throwable?, List<Folder>) -> Unit) {
        callback(null, getFoldersWithImages())
    }

    fun getImage(callback: (Throwable?, Image) -> Unit) {
        // TODO: 10/25/21 return the fullscreen image
    }

    private fun getFoldersWithImages(): MutableList<Folder> {

        val resultFolders = mutableListOf<Folder>()

        val basePath = Environment.getExternalStorageDirectory()
        val mainFolder = File(basePath.absolutePath)

        val files = mainFolder.walkTopDown()
        val foldersWithImages = files.filter { it.containsImages() }

        foldersWithImages.forEach {folder ->
            val images = mutableListOf<Image>()
            folder.listFiles { file -> isImageFile(file.absolutePath) }?.forEach {imageFile ->
                val previewBitmap: Bitmap = BitmapHelper.decodeBitmapFromFile(
                    imageFile.absolutePath,
                    50,
                    50
                )
                images.add(Image(imageFile.absolutePath, previewBitmap))
            }
            resultFolders.add(Folder(folder.name, images))
        }
        return resultFolders
    }

    private fun isImageFile(filePath: String): Boolean {
        val imageFileExtensions = listOf(".jpg", ".png", ".jpeg")
        return imageFileExtensions.any { filePath.endsWith(it) }
    }

    private fun File.containsImages(): Boolean {
        if (!isDirectory) return false
        return listFiles()?.any { file -> isImageFile(file.absolutePath) } ?: false
    }

    fun deleteImageByPath(path: String): Boolean {
        val file = File(path)
        println("File exists: ${file.exists()}")
        val deleted = file.delete()
        println("File deleted: $deleted")
        return deleted
    }
}