package com.example.gallery.data

import android.content.ContentUris
import android.content.Context
import com.example.gallery.data.entity.Folder

import java.io.File

import com.example.gallery.data.entity.Image
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider.getUriForFile
import com.example.gallery.addToList
import com.example.gallery.sdk29AndUp


class GalleryRepository(val context: Context) {

    fun getContent(callback: (Throwable?, List<Folder>) -> Unit) {
        callback(null, getAllFoldersWithImages())
    }

    fun getImage(callback: (Throwable?, Image) -> Unit) {
        // TODO: 10/25/21 return the fullscreen image
    }

    private fun getAllFoldersWithImages(): List<Folder> {

        val collection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
        )

        val imagesByFolderNames = mutableMapOf<String, MutableList<Image>>()

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

            val isEmpty = !(cursor.moveToFirst()) || cursor.count == 0

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
                    val bucketName = cursor.getString(bucketDisplayName)
                    val image = Image(id, displayName, width, height, contentUri)
                    imagesByFolderNames.addToList(bucketName, image)
                } while (cursor.moveToNext())
            }
            imagesByFolderNames.flatMap { listOf(Folder(it.key, it.value)) }
        } ?: listOf())
    }
}