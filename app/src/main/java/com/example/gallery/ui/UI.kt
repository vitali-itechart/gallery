package com.example.gallery.ui

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.gallery.MainViewModel
import com.example.gallery.data.entity.Image
import com.example.gallery.ui.ScreenIdentifiers.CAMERA_SCREEN
import com.example.gallery.ui.ScreenIdentifiers.MAIN_SCREEN
import com.example.gallery.ui.ScreenIdentifiers.METADATA_SCREEN
import com.example.gallery.ui.ScreenIdentifiers.PERMISSIONS_SCREEN
import com.example.gallery.ui.ScreenIdentifiers.PICTURE_FULLSCREEN
import com.example.gallery.ui.camera.CameraCapture
import com.example.gallery.ui.theme.CameraFab
import com.example.gallery.ui.theme.GalleryTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File
import java.util.*

@ExperimentalCoroutinesApi
@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@Composable
fun UI(
    state: MainViewModel.State,
    onSelectedChanged: (Int) -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    onImageSelected: (Image) -> Unit = {},
    onMetadataClick: (Image) -> Unit = {},
    onPictureTaken: (File) -> Unit = {},
) {

    GalleryTheme {

        val navController = rememberNavController()

        var permission by remember { mutableStateOf(false) }
        var fabVisible by remember { mutableStateOf(true) }

        Scaffold(topBar = {
            val destination = navController.currentBackStackEntryAsState()
            TopAppBar(
                title = { Text(text = destination.value?.destination?.route ?: MAIN_SCREEN) },
                navigationIcon = if (destination.value?.destination?.route ?: MAIN_SCREEN != MAIN_SCREEN) {
                    {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                } else {
                    null
                }
            )
        }, floatingActionButton = {

            val destination = navController.currentBackStackEntryAsState()
            fabVisible = when (destination.value?.destination?.route) {
                PICTURE_FULLSCREEN -> false
                CAMERA_SCREEN -> false
                METADATA_SCREEN -> false
                else -> true
            }

            if (fabVisible) {
                CameraFab(onCameraFabClick = {
                    navController.navigate(CAMERA_SCREEN)
                })
            }
        }) {

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
                                    onImageSelected(it)
                                    navController.navigate(PICTURE_FULLSCREEN)
                                })
                        }
                        MainViewModel.State.Loading -> {
                            LoadingScreen()
                        }
                    }
                }
                composable(PICTURE_FULLSCREEN) {
                    PictureFullscreen(state) {
                        onMetadataClick(it)
                        navController.navigate(METADATA_SCREEN)
                    }
                }
                composable(METADATA_SCREEN) {
                    MetadataScreen(state)
                }
                composable(CAMERA_SCREEN) {
                    fabVisible = false
                    CameraCapture(onImageFile = onPictureTaken)
                }
            }
        }
    }
}