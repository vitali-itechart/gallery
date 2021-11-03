package com.example.gallery.data

import android.content.ContentUris
import android.content.Context
import com.example.gallery.data.entity.Folder

import java.io.File

import com.example.gallery.data.entity.Image
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider.getUriForFile
import com.example.gallery.sdk29AndUp
import java.util.ArrayList


class GalleryRepository(private val context: Context) {

    fun getContent(callback: (Throwable?, List<Folder>) -> Unit) {
        callback(null, getFoldersWithImages())
    }

    fun getImage(callback: (Throwable?, Image) -> Unit) {
        // TODO: 10/25/21 return the fullscreen image
    }

    private fun getAllFoldersWithImages(): MutableList<Folder> {

        val collection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
        )

        val photos = mutableListOf<Image>()
        val folders = mutableListOf<Folder>()
        val imagesByUris = mutableMapOf<String, MutableList<Image>>()

        return (context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketDisplayName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            val uris = mutableSetOf<String>()

            val isEmpty = !(cursor.moveToFirst()) || cursor.count == 0

            if (isEmpty) println("Cursor is empty when loading images")

            if (!isEmpty) {
                do {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add((Image(id, displayName, width, height, contentUri)))
                    uris.add(cursor.getString(bucketDisplayName))
                    imagesByUris.putIfAbsent()
                } while (cursor.moveToNext())
            }
            uris.forEach { folders.add(Folder()) }
        } ?: listOf()).toMutableList()
    }

    private fun getFoldersWithImages(): MutableList<Folder> {

        val resultFolders = mutableListOf<Folder>()

        val foldersWithImagesNames = getImageFolderNames()

        foldersWithImagesNames.forEach { folderName ->
            val images = mutableListOf<Image>()
            val photos = loadPhotosFromExternalStorage(folderName)
            images.addAll(photos)
            resultFolders.add(Folder(folderName, images))
        }
        return resultFolders
    }

    private fun getImageFolderNames(): List<String> {

        val collection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
        )

        return context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->

            val uris = mutableSetOf<String>()

            val bucket = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val bucketName = cursor.getString(bucket)

                uris.add(bucketName)
            }

            uris.toList()
        } ?: emptyList()
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
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
        )

        val photos = mutableListOf<Image>()

        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = '$folderName'"

        return context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

            val isEmpty = !(cursor.moveToFirst()) || cursor.count == 0

            if (isEmpty) println("Cursor is empty when loading images")

            if (!isEmpty) {
                do {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add((Image(id, displayName, width, height, contentUri)))
                } while (cursor.moveToNext())
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