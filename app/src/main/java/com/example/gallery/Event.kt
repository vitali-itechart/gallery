package com.example.gallery


sealed class Event {

    class SelectFolder(val index: Int) : Event()
}
