package com.example.gallery.data.entity

data class Content(val folders: List<Folder> = emptyList(), val selectedFolderIndex: Int = 0)