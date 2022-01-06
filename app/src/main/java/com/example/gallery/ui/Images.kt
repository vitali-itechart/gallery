package com.example.gallery.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.gallery.data.entity.Image

@ExperimentalFoundationApi
@Composable
fun Images(list: List<Image> = emptyList(), onImageSelected: (Image) -> Unit = {}) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(128.dp),

        contentPadding = PaddingValues(
            start = 12.dp,
            top = 16.dp,
            end = 12.dp,
            bottom = 16.dp
        ),
        content = {
            items(list.size) { index ->
                Card(
                    backgroundColor = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    elevation = 8.dp,
                ) {
                    val image = list[index]
                    Image(
                        rememberImagePainter(data = image.contentUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(128.dp)
                            .selectable(selected = false, enabled = true, onClick = {
                                onImageSelected(image)
                            })
                    )
                }
            }
        }
    )
}