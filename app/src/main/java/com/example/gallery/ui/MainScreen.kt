package com.example.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gallery.MainViewModel
import com.example.gallery.data.entity.Image

@ExperimentalFoundationApi
@Composable
fun MainScreen(
    state: MainViewModel.State.Loaded,
    onSelectedChanged: (Int) -> Unit,
    onImageSelected: (Image) -> Unit
) {

    Surface(color = MaterialTheme.colors.background) {

        with(state) {

            val folders = content.folders

            if (folders.isEmpty()) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "No folders with images found", modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                    )
                }
            } else {
                Column {
                    Spacer(modifier = Modifier.padding(18.dp))
                    ContentContainer(
                        state = state.content,
                        selectedFolder = folders[content.selectedFolderIndex],
                        onSelectedChanged = onSelectedChanged,
                        onImageSelected = onImageSelected
                    )
                }
            }
        }
    }
}