package com.example.gallery

/**
 * Adds a value to a list that is associated with the key if the list exists and creates it first if it doesn't*/
fun <Key, Value> MutableMap<Key, MutableList<Value>>.addToList(key: Key, vararg value: Value){

    val list: MutableList<Value> = getOrPut(key, { mutableListOf() } )
    list.addAll(value)
}