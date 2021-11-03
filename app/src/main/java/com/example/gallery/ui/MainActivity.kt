package com.example.gallery.ui

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.example.gallery.R

import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager

import android.os.Build
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity(), GalleryContract {

    private var permissionsCallback: ((Throwable?, Boolean) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
    }

    private fun setup() {
        setupPermissions { error, granted ->
            if (granted) {
                showMainFragment()
            } else {
                processPermissionDecline { result ->
                    when (result) {
                        PermissionDialogResult.YES -> { setup() }
                        PermissionDialogResult.CLOSE -> { finishAffinity() }
                    }
                }
            }
        }
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

    private fun setupPermissions(callback: (Throwable?, Boolean) -> Unit) {
        permissionsCallback = callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {
                println("Permission is granted")
                permissionsCallback?.invoke(null, true)
            } else {
                println("Permission is revoked")
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            println("No need to request the permissions")
            permissionsCallback?.invoke(null, true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val grantedResultCode = 0 //not Activity.RESULT_OK for some reason
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
            .setPositiveButton(positiveButtonText) {_,_-> callback(PermissionDialogResult.YES); dialog?.dismiss()}
            .setNegativeButton(negativeButtonText) {_,_-> callback(PermissionDialogResult.CLOSE); dialog?.dismiss()}
            .setCancelable(false)
            .show()
    }

    private enum class PermissionDialogResult {YES, CLOSE}
}