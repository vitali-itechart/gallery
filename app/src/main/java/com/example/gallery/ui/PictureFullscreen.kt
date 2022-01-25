package com.example.gallery.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.gallery.MainViewModel
import com.example.gallery.R
import com.example.gallery.data.entity.Image

@Composable
fun PictureFullscreen(state: MainViewModel.State, onShowMetadataClick: (Image) -> Unit) {

    if (state is MainViewModel.State.Loaded) {

        Image(
            rememberImagePainter(data = state.image?.contentUri),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
            onClick = { state.image?.let { onShowMetadataClick(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(64.dp)
        ) {
            Text(text = stringResource(R.string.show_metadata))
        }
    }

}