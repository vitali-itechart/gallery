package com.example.gallery.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.Constants.IMG_PATH_KEY
import com.example.gallery.Constants.RV_SPAN_COUNT
import com.example.gallery.Constants.defaultFolderName
import com.example.gallery.R
import com.example.gallery.data.GalleryRepository
import com.example.gallery.data.entity.Folder
import com.example.gallery.data.entity.Image
import com.example.gallery.presenter.GalleryPresenter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class MainFragment : Fragment(), GalleryContract.MainView {

    private var rv: RecyclerView? = null
    private var presenter: GalleryPresenter? = null
    private var loadingProgressBar: ProgressBar? = null
    private var loadingText: TextView? = null
    private var foldersChipGroup: ChipGroup? = null
    private var pbContainer: LinearLayout? = null

    companion object {
        val FRAGMENT_NAME: String = MainFragment::class.java.name
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingProgressBar = view.findViewById(R.id.loading)
        loadingText = view.findViewById(R.id.loading_text)
        foldersChipGroup = view.findViewById(R.id.folders)
        rv = view.findViewById(R.id.previews)

        presenter = GalleryPresenter(GalleryRepository(requireContext()))
        presenter?.attachView(this)
        presenter?.loadContent()
    }

    override fun beginWaiting() {
        loadingProgressBar?.visibility = View.VISIBLE
        loadingText?.visibility = View.VISIBLE
        pbContainer?.visibility = View.VISIBLE
    }

    override fun stopWaiting() {
        loadingProgressBar?.visibility = View.GONE
        loadingText?.visibility = View.GONE
        pbContainer?.visibility = View.GONE
    }

    override fun showFullImage(image: Image) {
        showImageFullscreen(image.path)
    }

    override fun showFolders(foldersList: List<Folder>) {

        val selectedFolderName = foldersList[0].name
        var defaultFolderPresents = false
        //sets the first folder as selected, otherwise selects the camera photos folder
        foldersList.find { it.name == defaultFolderName }?.let {
            defaultFolderPresents = true
            selectedFolderName == getString(R.string.camera)
        }

        foldersList.forEach { folder ->
            val chip = Chip(requireContext())
            var isDefault = folder.name == defaultFolderName
            chip.isCheckable = true
            chip.isChecked = isDefault
            chip.text = if (isDefault) selectedFolderName else folder.name
            chip.setOnClickListener {
                chip.isChecked = true
                presenter?.loadPreviewsByFolderName(folder.name)
            }
            foldersChipGroup?.addView(chip)
            presenter?.loadPreviewsByFolderName(folder.name)
            if (!defaultFolderPresents) {
                chip.isChecked = true
            }
        }
    }

    override fun showPreviews(imagesList: List<Image>) {
        rv?.let {
            it.layoutManager = GridLayoutManager(context, RV_SPAN_COUNT)
            it.adapter =
                PreviewsRecyclerViewAdapter(imagesList, onClick = {
                    showFullImage(it)
                })
        }
    }

    private fun showImageFullscreen(imagePath: String) {

        val manager = requireActivity().supportFragmentManager
        val fragmentTransaction = manager
            .beginTransaction()
            .replace(
                R.id.container,
                getFullscreenFragment(manager).apply { arguments = bundleOf(IMG_PATH_KEY to imagePath) },
                FullscreenFragment.FRAGMENT_NAME
            )
        fragmentTransaction.addToBackStack(FullscreenFragment.FRAGMENT_NAME)
        fragmentTransaction.commit()
    }

    private fun getFullscreenFragment(supportFragmentManager: FragmentManager): FullscreenFragment {
        var fragment = supportFragmentManager.findFragmentByTag(FullscreenFragment.FRAGMENT_NAME)
        if (fragment == null) {
            fragment = FullscreenFragment()
        }
        return fragment as FullscreenFragment
    }

    override fun showDeletionDialog() {
        TODO("show deletion dialog")
    }

    override fun onFailure(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.detachView()
    }
}