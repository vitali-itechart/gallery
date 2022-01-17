package com.example.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gallery.MainViewModel
import com.example.gallery.ui.ScreenIdentifiers.MAIN_SCREEN
import com.example.gallery.ui.ScreenIdentifiers.PERMISSIONS_SCREEN
import com.example.gallery.ui.ScreenIdentifiers.PICTURE_FULLSCREEN
import com.example.gallery.ui.theme.GalleryTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ExperimentalFoundationApi
@Composable
fun UI(state: MainViewModel.State, onSelectedChanged: (Int) -> Unit, onPermissionDenied: () -> Unit) {

    GalleryTheme {

        val navController = rememberNavController()

        var permission by remember { mutableStateOf(false) }

        NavHost(
            navController = navController,
            startDestination = if (permission) MAIN_SCREEN else PERMISSIONS_SCREEN,
        ) {
            composable(PERMISSIONS_SCREEN) {

                PermissionsScreen(permissionGranted = { granted ->
                    if (granted) {
                        permission = true
                        navController.navigate(MAIN_SCREEN)
                    } else {
                        onPermissionDenied()
                    }
                })
            }
            composable(MAIN_SCREEN) {
                when (state) {
                    is MainViewModel.State.Loaded -> {
                        MainScreen(state,
                            onSelectedChanged = onSelectedChanged,
                            onImageSelected = {
                                val encodedUrl = URLEncoder.encode(
                                    it.contentUri.toString(),
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate("$PICTURE_FULLSCREEN/$encodedUrl")
                            })

                    }
                }
            }
            composable("$PICTURE_FULLSCREEN/{imageUri}") { backStackEntry ->
                backStackEntry.arguments?.getString("imageUri")?.let { PictureFullscreen(it) }
            }
        }
    }
}