package com.example.gallery.data.entity

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Image(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    var contentUri: Uri
) : Parcelable
