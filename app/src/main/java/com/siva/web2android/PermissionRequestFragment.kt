package com.siva.web2android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.PermissionRequest
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class PermissionRequestFragment: Fragment() {

    companion object {
        val NAME: String = PermissionRequestFragment::class.java.simpleName
    }

    private var permissions: Array<String>? = null

    private val REQ_PERMISSIONS = 200000

    private var request: PermissionRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        permissions?.let {
            requestPermissions(it, REQ_PERMISSIONS)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode != REQ_PERMISSIONS) return

        parentFragmentManager?.beginTransaction()?.remove(this)?.commit()

        val grants = mutableListOf<String>()
        permissions.zip(grantResults.toTypedArray()) { permission, result ->
            if (Manifest.permission.RECORD_AUDIO.equals(permission) &&
                    result == PackageManager.PERMISSION_GRANTED)
                        grants.add(PermissionRequest.RESOURCE_AUDIO_CAPTURE)

            if (Manifest.permission.CAMERA.equals(permission) &&
                    result == PackageManager.PERMISSION_GRANTED)
                        grants.add(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
        }.toTypedArray()

        request?.grant(grants.toTypedArray())
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun requestPermission(request: PermissionRequest?, fm: FragmentManager?) {
        val permissions = request?.resources?.map { item ->
            when(item) {
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> Manifest.permission.RECORD_AUDIO
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> Manifest.permission.CAMERA
                else -> null
            }
        }?.filterNotNull()?.toTypedArray()

        if (permissions == null || permissions.isEmpty()) return

        this.request = request

        this.permissions = permissions
        fm?.beginTransaction()?.add(this, NAME)?.commit()

    }

}