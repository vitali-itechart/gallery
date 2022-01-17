package com.example.gallery.tea

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

abstract class TeaFeatureHolder<Msg : Any, State : Any, Effect : Any>(
    initState: State,
    private val initEffects: Set<Effect>,
    vararg handlers: Pair<KClass<out Effect>, TeaEffectHandler<Effect, Msg>>
) : ViewModel(), Feature<Msg, State, Effect> {

    private var currentStateCompose by mutableStateOf(initState)
    private val effectHandlers = handlers.toList()

    constructor(
        initState: State,
        vararg handlers: Pair<KClass<out Effect>, TeaEffectHandler<Effect, Msg>>
    ) : this(initState, initEffects = emptySet(), *handlers)

    init {
        viewModelScope.launch {
            executeEffects(initEffects)
        }
    }

    override val state: State get() = currentStateCompose

    override fun accept(msg: Msg) {
        val (newState, effects) = reduce(msg, currentStateCompose)
        currentStateCompose = newState

        executeEffects(effects)
    }

    private fun executeEffects(effects: Set<Effect>) {
        effects.forEach { eff ->
            viewModelScope.launch {
                effectHandlers.forEach { (effectClass, handler) ->
                    if (effectClass.isInstance(eff)) {
                        handler.execute(eff, ::accept)
                    }
                }
            }
        }
    }
}