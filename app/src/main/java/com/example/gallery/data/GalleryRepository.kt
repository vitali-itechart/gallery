package com.example.gallery.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.ContentObserver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import com.example.gallery.data.entity.Content
import com.example.gallery.data.entity.Folder
import com.example.gallery.data.entity.Image
import com.example.gallery.sdk29AndUp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import javax.inject.Inject


class GalleryRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val _contentFlow = MutableStateFlow(Content(getAllFoldersWithImages(), 0))
    val contentFlow: StateFlow<Content> = _contentFlow

    init {

        val contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                _contentFlow.update { it.copy(folders = getAllFoldersWithImages()) }
            }
        }
        context.contentResolver?.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    fun saveMediaToStorage(file: File) {
        val mimeType = file.toURI().toURL().openConnection().contentType
        if (mimeType.contains("image").not()) {
            throw IllegalArgumentException("Wrong image format: $mimeType")
        }
        val input = context.contentResolver.openInputStream(file.toUri())
        val bitmap = BitmapFactory.decodeStream(input)
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver?.also { resolver ->

                val contentValues = ContentValues().apply {

                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    fun getContentBlocking(): Content {
        return Content(getAllFoldersWithImages(), 0)
    }

    suspend fun getContent(): Content { //I'm aware suspension is redundant here but is here just for demo purposes
        return Content(getAllFoldersWithImages(), 0)
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
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MIME_TYPE,
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
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

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
                    val dateTaken = cursor.getString(dateTakenColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val image = Image(id, displayName, width, height, contentUri, dateTaken, mimeType)
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