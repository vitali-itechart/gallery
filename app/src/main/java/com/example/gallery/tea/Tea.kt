package com.example.gallery.tea

interface Feature<Msg, State, Effect> {
    val state: State

    fun accept(msg: Msg)
    fun reduce(msg: Msg, state: State): Pair<State, Set<Effect>>
}

interface TeaEffectHandler<out Effect, Msg> {
    suspend fun execute(eff: @UnsafeVariance Effect, consumer: (Msg) -> Unit)
}
