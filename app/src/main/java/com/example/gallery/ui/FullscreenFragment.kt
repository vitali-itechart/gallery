package com.example.gallery.ui

import android.app.RecoverableSecurityException
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.gallery.Constants.IMG_KEY
import com.example.gallery.Constants.URI_KEY
import com.example.gallery.R
import com.example.gallery.data.entity.Image
import com.example.gallery.databinding.FragmentFullscreenBinding
import com.example.gallery.presenter.GalleryPresenter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@AndroidEntryPoint
class FullscreenFragment : Fragment(), GalleryContract.FullscreenView {
    private val hideHandler = Handler()
    @Inject lateinit var presenter: GalleryPresenter
    private var isFromAnotherApp = false

    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var deleteButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null

    private var _binding: FragmentFullscreenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFullscreenBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uriString = arguments?.getString(URI_KEY)
        val uri = Uri.parse(uriString)
        val image = arguments?.get(IMG_KEY) as? Image
        isFromAnotherApp = uriString != null
//        presenter = GalleryPresenter(GalleryRepository(requireContext()))
        presenter?.attachView(this)

        visible = true

        deleteButton = binding.dummyButton
        fullscreenContent = binding.fullscreenContent
        fullscreenContentControls = binding.fullscreenContentControls
        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener { toggle() }

        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == AppCompatActivity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        image?.let {
                            deletePhotoFromExternalStorage(image.contentUri)
                        } ?: deletePhotoFromExternalStorage(uri)
                    }
                    parentFragmentManager.popBackStack()

                    Toast.makeText(
                        requireContext(),
                        "Photo deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Photo couldn't be deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        deleteButton?.setOnTouchListener(delayHideTouchListener)
        deleteButton?.setOnClickListener {
            requestDeletionConfirmation { result ->
                when (result) {
                    DeletionDialogResult.YES -> {
                        image?.let { deletePhotoFromExternalStorage(image.contentUri) } ?: deletePhotoFromExternalStorage(uri)
                    }
                    DeletionDialogResult.CANCEL -> {
                    }
                }
            }
        }

        if (uri != null) {
            (fullscreenContent as ImageView).setImageURI(uri)
        } else {
            image?.let { (fullscreenContent as ImageView).setImageURI(image.contentUri) }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteButton = null
        fullscreenContent = null
        fullscreenContentControls = null
        presenter?.detachView()
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        if (!isFromAnotherApp) {
            fullscreenContent?.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            visible = true

            // Schedule a runnable to display UI elements after a delay
            hideHandler.removeCallbacks(hidePart2Runnable)
            hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
            (activity as? AppCompatActivity)?.supportActionBar?.show()
        }
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {

        val FRAGMENT_NAME: String = FullscreenFragment::class.java.name

        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun requestDeletionConfirmation(callback: (DeletionDialogResult) -> Unit) {

        val titleMessage = getString(R.string.sure)
        val message = getString(R.string.del_warning)
        val positiveButtonText = getString(R.string.yes)
        val negativeButtonText = getString(R.string.cancel)

        var dialog: AlertDialog? = null

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(titleMessage)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> callback(DeletionDialogResult.YES); dialog?.dismiss() }
            .setNegativeButton(negativeButtonText) { _, _ -> callback(DeletionDialogResult.CANCEL); dialog?.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun deletePhotoFromExternalStorage(photoUri: Uri) {
        val contentResolver = activity?.contentResolver
        try {
            contentResolver?.delete(photoUri, null, null)
        } catch (e: SecurityException) {
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(
                        contentResolver ?: error("Content resolver must not be null"),
                        listOf(photoUri)
                    ).intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    recoverableSecurityException?.userAction?.actionIntent?.intentSender
                }
                else -> null
            }
            intentSender?.let { sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
            }
        }
    }

    private enum class DeletionDialogResult { YES, CANCEL }
}