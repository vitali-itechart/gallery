package com.example.gallery.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gallery.MainViewModel

@Composable
fun MetadataScreen(state: MainViewModel.State = dummyState) {

    with(state as? MainViewModel.State.Loaded ?: dummyState) {

        Surface(Modifier.padding(24.dp)) {
            Column {
                Text(text = "Name: ${image?.name ?: "Unknown name"}")
                Text(text = "Mime type: ${image?.mimeType ?: "Unknown type"}")
                Text(text = "Date taken: ${image?.dateTaken ?: "Unknown date"}")
                Text(text = "Height: ${image?.height.toString()}")
                Text(text = "Width: ${image?.width.toString()}")
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MetadataScreen()
}