package com.example.gallery.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.example.gallery.R

import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager
import android.database.ContentObserver

import android.os.Build
import android.provider.MediaStore
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import com.example.gallery.Constants
import com.example.gallery.data.entity.Image
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), GalleryContract {

    private var permissionsCallback: ((Throwable?, Boolean) -> Unit)? = null
    private var isFromAnotherApp = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
    }

    private fun setup() {

        if (intent.data != null) {
            isFromAnotherApp = true
            showImageFullscreen(intent.dataString)
        } else {
            setupPermissions { error, granted ->
                if (granted) {
                    showMainFragment()
                } else {
                    processPermissionDecline { result ->
                        when (result) {
                            PermissionDialogResult.YES -> {
                                setup()
                            }
                            PermissionDialogResult.CLOSE -> {
                                finishAffinity()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showImageFullscreen(path: String?) {

        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.container,
                getFullscreenFragment(supportFragmentManager).apply { arguments = bundleOf(Constants.URI_KEY to path) },
                FullscreenFragment.FRAGMENT_NAME
            )
        fragmentTransaction.addToBackStack(FullscreenFragment.FRAGMENT_NAME)
        fragmentTransaction.commit()
    }

    private fun showMainFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                getMainFragment(supportFragmentManager),
                MainFragment.FRAGMENT_NAME
            )
        fragmentTransaction.addToBackStack(MainFragment.FRAGMENT_NAME)
        fragmentTransaction.commit()
    }

    private fun getMainFragment(supportFragmentManager: FragmentManager): MainFragment {
        var fragment = supportFragmentManager.findFragmentByTag(MainFragment.FRAGMENT_NAME)
        if (fragment == null) {
            fragment = MainFragment()
        }
        return fragment as MainFragment
    }

    private fun getFullscreenFragment(supportFragmentManager: FragmentManager): FullscreenFragment {
        var fragment = supportFragmentManager.findFragmentByTag(FullscreenFragment.FRAGMENT_NAME)
        if (fragment == null) {
            fragment = FullscreenFragment()
        }
        return fragment as FullscreenFragment
    }


    private fun setupPermissions(callback: (Throwable?, Boolean) -> Unit) {
        permissionsCallback = callback
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            println("Permission is granted")
            permissionsCallback?.invoke(null, true)
        } else {
            println("Permission is revoked")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val grantedResultCode = 0
        val isGranted = grantResults[0] == grantedResultCode
        permissionsCallback?.invoke(null, isGranted)
    }

    private fun processPermissionDecline(callback: (PermissionDialogResult) -> Unit) {

        val titleMessage = getString(R.string.perm_denied)
        val message = getString(R.string.permission_warning)
        val positiveButtonText = getString(R.string.yes)
        val negativeButtonText = getString(R.string.close)

        var dialog: AlertDialog? = null

        dialog = AlertDialog.Builder(this)
            .setTitle(titleMessage)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { _, _ -> callback(PermissionDialogResult.YES); dialog?.dismiss() }
            .setNegativeButton(negativeButtonText) { _, _ -> callback(PermissionDialogResult.CLOSE); dialog?.dismiss() }
            .setCancelable(false)
            .show()
    }

    private enum class PermissionDialogResult { YES, CLOSE }

    override fun onResume() {
        super.onResume()
        supportActionBar?.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isFromAnotherApp) finish()
    }
}