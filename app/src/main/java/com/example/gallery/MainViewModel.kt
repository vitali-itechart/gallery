package com.example.gallery

import androidx.lifecycle.ViewModel
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Content
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainViewModel @Inject constructor(private val repo: GalleryRepository) : ViewModel() {

    private val _stateFlow: MutableStateFlow<Content> = repo.getContent()
    val stateFlow = _stateFlow.asStateFlow()

    fun processEvent(event: Event) {
        when(event) {
            is Event.SelectFolder -> {
                _stateFlow.update {
                    it.copy(selectedFolderIndex = event.index)
                }
            }
        }
    }
}