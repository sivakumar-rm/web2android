package com.siva.web2android

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.GeolocationPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class GeoLocationFragment: Fragment() {

    companion object {
        val NAME: String = GeoLocationFragment::class.java.simpleName
    }

    private val REQ_LOCATION_PERMISSION = 100001

    private var origin: String? = null

    private var callback: GeolocationPermissions.Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), REQ_LOCATION_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQ_LOCATION_PERMISSION)
            onLocationPermission(permissions, grantResults)
    }

    fun requestPermission(origin: String?, ctx: Context?,
                          callback: GeolocationPermissions.Callback?, fm: FragmentManager?) {
        if (!Config.locationAccessEnabled) {
            callback?.invoke(origin, false, false)
            return
        }

        if (ctx == null) {
            callback?.invoke(origin, false, false)
            return
        }

        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            callback?.invoke(origin, true, false)
            return
        }

        this.origin = origin
        this.callback = callback

        fm?.beginTransaction()?.add(this, NAME)?.commit()
    }

    private fun onLocationPermission(permissions: Array<out String>, grantResults: IntArray) {
        parentFragmentManager?.beginTransaction()?.remove(this)?.commit()

        val granted: Boolean = grantResults.zip(permissions).map {
            if (android.Manifest.permission.ACCESS_COARSE_LOCATION.equals(it.second) &&
                it.first == PackageManager.PERMISSION_GRANTED) true
            else if (android.Manifest.permission.ACCESS_FINE_LOCATION.equals(it.second) &&
                it.first == PackageManager.PERMISSION_GRANTED) true
            else null

        }.reduce { acc, value -> ((acc ?: false) || (value ?: true)) } ?: false

        callback?.invoke(origin, granted, false)
    }

}