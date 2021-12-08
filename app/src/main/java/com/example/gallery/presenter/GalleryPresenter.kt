package com.example.gallery.presenter

import com.example.gallery.Constants.UNKNOWN_ERROR
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Folder
import com.example.gallery.ui.GalleryContract
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class GalleryPresenter @Inject constructor(private val repo: GalleryRepository) :
    GalleryContract.Presenter {

    private var mainView: GalleryContract.MainView? = null
    private var fullscreenView: GalleryContract.FullscreenView? = null
    private var foldersCache: List<Folder>? = null
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    override fun loadContent() {

        scope.launch {

            repo.getContent()
                .catch { mainView?.onFailure(it.message ?: UNKNOWN_ERROR) }
                .collect { foldersList ->
                    foldersCache = foldersList
                    mainView?.stopWaiting()
                    mainView?.showFolders(foldersList)
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
            scope.launch {

                repo.getContent()
                    .catch { mainView?.onFailure(it.message ?: UNKNOWN_ERROR) }
                    .collect { foldersList ->
                        foldersCache = foldersList
                        mainView?.stopWaiting()
                        mainView?.showFolders(foldersList)
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