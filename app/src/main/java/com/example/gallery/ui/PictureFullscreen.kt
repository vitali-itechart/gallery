package com.example.gallery.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import coil.compose.rememberImagePainter

@Composable
fun PictureFullscreen(imageUri: String) {

    Image(
        rememberImagePainter(data = imageUri.toUri()),
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}