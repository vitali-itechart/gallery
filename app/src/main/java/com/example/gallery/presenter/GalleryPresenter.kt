package com.example.gallery.presenter

import com.example.gallery.Constants.UNKNOWN_ERROR
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Folder
import com.example.gallery.ui.GalleryContract

class GalleryPresenter(private val repo: GalleryRepository) : GalleryContract.Presenter {

    var mainView: GalleryContract.MainView? = null
    var fullscreenView: GalleryContract.FullscreenView? = null
    private var foldersCache: List<Folder>? = null

    override fun loadContent() {

        repo.getContent { error, foldersList ->
            if (error == null) {
                foldersCache = foldersList
                mainView?.stopWaiting()
                mainView?.showFolders(foldersList)
            } else {
                mainView?.onFailure(error.message ?: UNKNOWN_ERROR)
            }
        }
    }

    override fun loadImagesByFolderName(folderName: String) {

        fun loadPreviewsFromCache() {
            val folder = foldersCache?.find { it.name == folderName }
            folder?.let {
                mainView?.showPreviews(it.imagesList)
            }
        }

        if (foldersCache == null) {
            repo.getContent { error, foldersList ->
                foldersCache = foldersList
                loadPreviewsFromCache()
            }
        } else {
            loadPreviewsFromCache()
        }
    }

    override fun getImage() {

        repo.getImage { error, image ->
            if (error == null) {
                mainView?.stopWaiting()
                mainView?.showFullImage(image)
            } else {
                mainView?.onFailure(error.message ?: UNKNOWN_ERROR)
            }
        }
    }

    override fun deleteImageByPath(path: String) {
        repo.deleteImageByPath(path)
    }

    override fun attachView(view: GalleryContract.BaseView) {
        when (view) {
            is GalleryContract.MainView -> this.mainView = view
            is GalleryContract.FullscreenView -> this.fullscreenView = view
        }
    }

    override fun detachView() {
        mainView = null
    }
}