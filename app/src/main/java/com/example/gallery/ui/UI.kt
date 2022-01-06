package com.example.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.gallery.data.entity.Content
import com.example.gallery.ui.theme.GalleryTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ExperimentalFoundationApi
@Composable
fun UI(state: State<Content>, onSelectedChanged: (Int) -> Unit, onPermissionDenied: () -> Unit) {

    GalleryTheme {

        val navController = rememberNavController()

        var permission by remember { mutableStateOf(false) }

        NavHost(
            navController = navController,
            startDestination = if (permission) "MainScreen" else "PermissionsScreen",
        ) {
            composable("PermissionsScreen") {

                PermissionsScreen(permissionGranted = { granted ->
                    if (granted) {
                        println("permission granted!!!")
                        permission = true
                        navController.navigate("MainScreen")
                    } else {
                        onPermissionDenied()
                    }
                })
            }
            composable("MainScreen") {
                MainScreen(state,
                    onSelectedChanged = onSelectedChanged,
                    onImageSelected = {
                        val encodedUrl = URLEncoder.encode(
                            it.contentUri.toString(),
                            StandardCharsets.UTF_8.toString()
                        )
                        navController.navigate("PictureFullscreen/$encodedUrl")
                    })
            }
            composable("PictureFullscreen/{imageUri}") { backStackEntry ->
                backStackEntry.arguments?.getString("imageUri")?.let { PictureFullscreen(it) }
            }
        }
    }
}