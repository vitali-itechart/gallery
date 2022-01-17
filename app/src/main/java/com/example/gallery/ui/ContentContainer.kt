package com.example.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gallery.data.entity.Content
import com.example.gallery.data.entity.Folder
import com.example.gallery.data.entity.Image

@ExperimentalFoundationApi
@Composable
fun ContentContainer(
    state: Content,
    selectedFolder: Folder? = null,
    onSelectedChanged: (Int) -> Unit = {},
    onImageSelected: (Image) -> Unit = {},
) {
    with(state) {
        Column(modifier = Modifier.padding(8.dp)) {
            LazyRow {
                items(folders.size) { index ->
                    Chip(
                        index = index,
                        name = folders[index].name,
                        isSelected = index == selectedFolderIndex,
                        onSelectionChanged = {
                            onSelectedChanged(it)
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.padding(18.dp))
            Images(
                selectedFolder?.imagesList ?: folders[selectedFolderIndex].imagesList,
                onImageSelected = onImageSelected
            )
        }
    }
}