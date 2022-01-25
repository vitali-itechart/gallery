package com.example.gallery


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.runtime.*

import com.example.gallery.ui.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi

import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@ExperimentalPermissionsApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            UI(
                vm.state,
                onSelectedChanged = { newSelectedFolderIndex ->
                    vm.accept(
                        MainViewModel.Msg.OnFolderClicked(newSelectedFolderIndex)
                    )
                },
                onPermissionDenied = { finishAffinity() },
                onImageSelected = { vm.accept(MainViewModel.Msg.OnImageSelected(it)) },
                onMetadataClick = { vm.accept(MainViewModel.Msg.OnImageSelected(it)) },
                onPictureTaken = { vm.accept(MainViewModel.Msg.SaveImageFile(it)) },
            )
        }
    }
}
