package com.example.gallery.ui

import com.example.gallery.data.entity.Folder
import com.example.gallery.data.entity.Image

interface GalleryContract {

    interface BaseView

    interface MainView : BaseView {
        fun beginWaiting()
        fun stopWaiting()
        fun showFullImage(image: Image)
        fun showFolders(foldersList: List<Folder>)
        fun showPreviews(imagesList: List<Image>)
        fun onFailure(message: String)
    }

    interface FullscreenView : BaseView

    interface Presenter {
        fun loadContent()
        fun loadImagesByFolderName(folderName: String)
        fun attachView(mainView: BaseView)
        fun detachView()
    }
}