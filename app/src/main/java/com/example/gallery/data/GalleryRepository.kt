package com.example.gallery.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.gallery.data.entity.Folder
import com.example.gallery.data.entity.Image
import com.example.gallery.sdk29AndUp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class GalleryRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getContent(): Flow<List<Folder>> {
        return flow {
            emit(getAllFoldersWithImages())
        }
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

        val bucketNamesWithImages = mutableListOf<Pair<String, Image>>()

        val cur = context.contentResolver.query(
            collection,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        )

        return cur?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val bucketDisplayName =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            val isEmpty = !(cursor.moveToFirst()) || cursor.count == 0

            if (isEmpty) println("Cursor is empty!")

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
                    bucketNamesWithImages.add(bucketName to image)
                } while (cursor.moveToNext())
            }
            bucketNamesWithImages
                .groupBy { it.first }
                .flatMap {
                    listOf(Folder(it.key, it.value.map { it.second }))
                }
        } ?: listOf()
    }
}