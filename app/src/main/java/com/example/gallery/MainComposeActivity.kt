package com.example.gallery


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.runtime.*

import com.example.gallery.data.entity.Content

import com.example.gallery.ui.UI

import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject

@ExperimentalFoundationApi
@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {

    @Inject
    lateinit var vm: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val state: State<Content> = vm.stateFlow.collectAsState(initial = Content())

            UI(
                state,
                onSelectedChanged = { newSelectedFolderIndex ->
                    vm.processEvent(
                        Event.SelectFolder(newSelectedFolderIndex)
                    )
                },
                onPermissionDenied = { finishAffinity() })
        }
    }
}
