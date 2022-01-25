package com.example.gallery

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Content
import com.example.gallery.data.entity.Image
import com.example.gallery.tea.TeaEffectHandler
import com.example.gallery.tea.TeaFeatureHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

private typealias ReducerResult = Pair<MainViewModel.State, Set<MainViewModel.Effect>>

@HiltViewModel
class MainViewModel @Inject constructor(
    repo: GalleryRepository
) : TeaFeatureHolder<MainViewModel.Msg, MainViewModel.State, MainViewModel.Effect>(
    initState = State.Loading,
    initEffects = setOf(Effect.LoadContent),
    Effect::class to MainEffectHandler(repo)
) {

    init {
        viewModelScope.launch {
            repo.contentFlow.collect {
                accept(Msg.OnContentLoaded(it))
            }
        }
    }

    sealed class State {
        object Loading : State()
        data class Loaded(val content: Content, val image: Image? = null) : State()
        val loaded get() = this as Loaded
    }

    sealed class Msg {
        class LoadContent(val content: Content) : Msg()
        class SaveImageFile(val imageFile: File) : Msg()
        class OnContentLoaded(val content: Content) : Msg()
        class OnFolderClicked(val selectedFolderIndex: Int) : Msg()
        class OnImageSelected(val image: Image) : Msg()
        object OnImageSaved : Msg()
    }

    sealed class Effect {
        object LoadContent : Effect()
        data class SaveCapturedImage(val imageFile: File) : Effect()
    }

    override fun reduce(msg: Msg, state: State): ReducerResult = when (msg) {
        is Msg.LoadContent -> State.Loaded(msg.content) to setOf(Effect.LoadContent)
        is Msg.OnFolderClicked -> state.loaded.copy(
            content = state.loaded.content.copy(
            selectedFolderIndex = msg.selectedFolderIndex)) to emptySet()
        is Msg.OnContentLoaded -> State.Loaded(msg.content) to emptySet()
        is Msg.OnImageSelected -> state.loaded.copy(image = msg.image) to emptySet()
        is Msg.SaveImageFile -> state to setOf(Effect.SaveCapturedImage(msg.imageFile))
        Msg.OnImageSaved -> state to emptySet()
    }
}

private class MainEffectHandler(
    private val repository: GalleryRepository
) : TeaEffectHandler<MainViewModel.Effect, MainViewModel.Msg> {

    override suspend fun execute(
        eff: MainViewModel.Effect,
        consumer: (MainViewModel.Msg) -> Unit
    ) {
        when (eff) {
            MainViewModel.Effect.LoadContent -> loadContent()
            is MainViewModel.Effect.SaveCapturedImage -> saveCapturedImage(eff.imageFile)
        }.let(consumer)
    }

    private fun saveCapturedImage(file: File) = repository.saveMediaToStorage(file).let {
        MainViewModel.Msg.OnImageSaved
    }

    private suspend fun loadContent() = repository.getContent().let {
        MainViewModel.Msg.OnContentLoaded(it)
    }
}