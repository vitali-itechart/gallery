package com.example.gallery.data

import android.content.ContentUris
import android.content.Context
import com.example.gallery.data.entity.Folder

import java.io.File

import android.os.Environment
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.gallery.data.entity.Image
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider.getUriForFile
import com.example.gallery.sdk29AndUp


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

        foldersWithImages.forEach { folder ->
            val images = mutableListOf<Image>()
            folder.listFiles { file -> isImageFile(file.absolutePath) }?.forEach { imageFile ->
                val photos = loadPhotosFromExternalStorage(folder.name)
                images.addAll(photos)
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

    private fun loadPhotosFromExternalStorage(folderName: String): List<Image> {
        val collection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.DATA,
        )

        val photos = mutableListOf<Image>()

        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"

        val selectionArgs = arrayOf("DCIM/")

        return context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                println("Data: ${cursor.getString(data)}") // TODO: 10/28/21 stopped here: retrieve folder from data
                photos.add((Image(id, displayName, width, height, contentUri)))
            }
            photos.toList()
        } ?: listOf()
    }


    fun deleteImageByPath(path: String): Boolean {
        val file = File(path)
        println("File exists: ${file.exists()}")
        val contentUri: Uri = getUriForFile(context, "com.example.gallery", file)
        println("Content URI: $contentUri")
        val deleted = file.delete()
        println("File deleted: $deleted")
        return deleted
    }


}