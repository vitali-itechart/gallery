package com.example.gallery.data.entity

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    var contentUri: Uri,
    var dateTaken: String?,
    var mimeType: String
) : Parcelable
