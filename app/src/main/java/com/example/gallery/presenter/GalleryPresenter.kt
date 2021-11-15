package com.example.gallery.presenter

import com.example.gallery.Constants.UNKNOWN_ERROR
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Folder
import com.example.gallery.ui.GalleryContract
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryPresenter @Inject constructor (private val repo: GalleryRepository) : GalleryContract.Presenter {

    private var mainView: GalleryContract.MainView? = null
    private var fullscreenView: GalleryContract.FullscreenView? = null
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
                if (error != null) {
                    foldersCache = foldersList
                    loadPreviewsFromCache()
                } else {
                    mainView?.onFailure(error?.message ?: UNKNOWN_ERROR)
                }
            }
        } else {
            loadPreviewsFromCache()
        }
    }

    override fun attachView(mainView: GalleryContract.BaseView) {
        when (mainView) {
            is GalleryContract.MainView -> this.mainView = mainView
            is GalleryContract.FullscreenView -> this.fullscreenView = mainView
        }
    }

    override fun detachView() {
        mainView = null
    }
}