package com.example.gallery.ui

import android.database.ContentObserver
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gallery.Constants.IMG_KEY
import com.example.gallery.Constants.RV_SPAN_COUNT
import com.example.gallery.Constants.defaultFolderName
import com.example.gallery.R
import com.example.gallery.data.entity.Folder
import com.example.gallery.data.entity.Image
import com.example.gallery.presenter.GalleryPresenter
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment(), GalleryContract.MainView {

    private var rv: RecyclerView? = null
    @Inject lateinit var presenter: GalleryPresenter
    private var loadingProgressBar: ProgressBar? = null
    private var loadingText: TextView? = null
    private var foldersChipGroup: ChipGroup? = null
    private var pbContainer: LinearLayout? = null
    private var emptyStateTv: TextView? = null
    private lateinit var contentObserver: ContentObserver

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
        emptyStateTv = view.findViewById(R.id.empty_text)

        initContentObserver()
        presenter.attachView(this)
        presenter.loadContent()
        println(presenter)
    }

    private fun initContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                presenter.loadContent()
            }
        }
        activity?.contentResolver?.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
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
        showImageFullscreen(image)
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun showFolders(foldersList: List<Folder>) {

        if (foldersList.isEmpty()) {
            emptyStateTv?.visibility = View.VISIBLE
            return
        } else {
            emptyStateTv?.visibility = View.GONE
        }
        val selectedFolderName = foldersList[0].name
        var defaultFolderPresents = false
        //sets the first folder as selected, otherwise selects the camera photos folder
        foldersList.find { it.name == defaultFolderName }?.let {
            defaultFolderPresents = true
            selectedFolderName == getString(R.string.camera)
        }

        foldersList.forEach { folder ->
            val chip = Chip(requireContext())
            val isDefault = folder.name == defaultFolderName
            chip.isCheckable = true
            chip.isChecked = isDefault
            chip.text = if (isDefault) selectedFolderName else folder.name
            chip.setOnClickListener {
                chip.isChecked = true
                presenter.loadImagesByFolderName(folder.name)
            }
            foldersChipGroup?.addView(chip)
            presenter.loadImagesByFolderName(folder.name)
            if (!defaultFolderPresents) {
                chip.isChecked = true
            }
        }
    }

    override fun showPreviews(imagesList: List<Image>) {
        rv?.let {
            it.layoutManager = GridLayoutManager(context, RV_SPAN_COUNT)
            it.adapter =
                PreviewsRecyclerViewAdapter(imagesList, onClick = {image ->
                    showFullImage(image)
                })
        }
    }

    private fun showImageFullscreen(image: Image) {

        val manager = requireActivity().supportFragmentManager
        val fragmentTransaction = manager
            .beginTransaction()
            .replace(
                R.id.container,
                getFullscreenFragment(manager).apply { arguments = bundleOf(IMG_KEY to image) },
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

    override fun onFailure(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
        activity?.contentResolver?.unregisterContentObserver(contentObserver)
    }
}