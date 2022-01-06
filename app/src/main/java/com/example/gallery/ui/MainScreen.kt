package com.example.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gallery.data.entity.Content
import com.example.gallery.data.entity.Image

@ExperimentalFoundationApi
@Composable
fun MainScreen(
    content: State<Content>,
    onSelectedChanged: (Int) -> Unit,
    onImageSelected: (Image) -> Unit
) {

    Surface(color = MaterialTheme.colors.background) {

        with(content.value) {

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
                    TopAppBar(title = { Text(text = "Gallery") })
                    Spacer(modifier = Modifier.padding(18.dp))
                    ContentContainer(
                        state = content,
                        selectedFolder = folders[selectedFolderIndex],
                        onSelectedChanged = onSelectedChanged,
                        onImageSelected = onImageSelected
                    )
                }
            }
        }
    }
}