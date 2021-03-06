/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.gallery.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.gallery.ui.ScreenIdentifiers.MAIN_SCREEN

/**
 * Screen metadata for Rally.
 */
enum class GalleryScreen(
    val icon: ImageVector,
) {
    Content(
        icon = Icons.Filled.PieChart,
    ),
    ImageFullscreen(
        icon = Icons.Filled.AttachMoney,
    ),
    Metadata(
        icon = Icons.Filled.MoneyOff,
    );

    companion object {
        fun fromRoute(route: String?): GalleryScreen =
            when (route?.substringBefore("/")) {
                ImageFullscreen.name -> ImageFullscreen
                Metadata.name -> Metadata
                MAIN_SCREEN -> Content
                null -> Content
                else -> throw IllegalArgumentException("Route $route is not recognized.")
            }
    }
}
