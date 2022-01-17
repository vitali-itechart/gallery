package com.example.gallery

import android.net.Uri
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Content
import com.example.gallery.data.entity.Image
import com.example.gallery.tea.TeaEffectHandler
import com.example.gallery.tea.TeaFeatureHolder
import dagger.hilt.android.lifecycle.HiltViewModel
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

    sealed class State {
        object Loading : State()
        data class Loaded(val content: Content) : State()
        val loaded get() = this as Loaded
    }

    sealed class Msg {
        class LoadContent(val content: Content) : Msg()
        class OnContentLoaded(val content: Content) : Msg()
        class OnFolderClicked(val selectedFolderIndex: Int) : Msg()
    }

    sealed class Effect {
        object LoadContent : Effect()
    }

    override fun reduce(msg: Msg, state: State): ReducerResult = when (msg) {
        is Msg.LoadContent -> State.Loaded(msg.content) to setOf(Effect.LoadContent)
        is Msg.OnFolderClicked -> state.loaded.copy(
            content = state.loaded.content.copy(
            selectedFolderIndex = msg.selectedFolderIndex)) to emptySet()
        is Msg.OnContentLoaded -> State.Loaded(msg.content) to emptySet()
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
        }.let(consumer)
    }

    private fun loadContent() = repository.getContentBlocking().let {
        MainViewModel.Msg.OnContentLoaded(it)
    }
}