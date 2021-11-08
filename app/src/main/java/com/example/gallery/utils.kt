package com.example.gallery

fun <Key, Value> MutableMap<Key, MutableList<Value>>.addToList(key: Key, vararg value: Value){

    val list: MutableList<Value> = getOrPut(key, { mutableListOf() } )
    list.addAll(value)
}