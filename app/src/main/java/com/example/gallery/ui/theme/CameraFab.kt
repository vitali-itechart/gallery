package com.example.gallery.ui.theme

import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.runtime.Composable

@Composable
fun CameraFab(onCameraFabClick: ()->Unit = {}) {
    ExtendedFloatingActionButton(
        onClick = onCameraFabClick,
        icon = {
            Icon(
                Icons.Filled.Camera,
                contentDescription = "OpenCamera",
            )
        },
        text = { Text("Take photo") },
    )
}