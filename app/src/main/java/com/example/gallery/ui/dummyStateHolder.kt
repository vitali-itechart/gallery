package com.example.gallery.ui

import android.net.Uri
import com.example.gallery.MainViewModel
import com.example.gallery.data.entity.Content
import com.example.gallery.data.entity.Image

val dummyContent = Content()
val dummyState = MainViewModel.State.Loaded(
    dummyContent,
    image = Image(0, "Sample name", 100, 120, Uri.EMPTY, "22/7/1562", "image/jpeg")
)