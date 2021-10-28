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
        fun showDeletionDialog()
        fun onFailure(message: String)
    }

    interface FullscreenView : BaseView {
        fun onImageDeleted()
    }

    interface Presenter {
        fun loadContent()
        fun loadImagesByFolderName(folderName: String)
        fun getImage()
        fun deleteImageByPath(path: String)
        fun attachView(mainView: BaseView)
        fun detachView()
    }
}