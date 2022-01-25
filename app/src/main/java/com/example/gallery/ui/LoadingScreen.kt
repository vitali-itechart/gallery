package com.example.gallery.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.gallery.R

@Composable
fun LoadingScreen() {
    Text(text = stringResource(R.string.loading), Modifier.padding(8.dp))
}