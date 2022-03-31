package com.siva.web2android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FileChooserFragment: Fragment() {

    companion object {
        val NAME: String = FileChooserFragment::class.java.simpleName
    }

    private val CHOOSE_FILE = 10001

    private var fileChooserParams: WebChromeClient.FileChooserParams? = null

    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_FILE && resultCode == Activity.RESULT_OK)
            onFileChosen(data)
        else if (requestCode === CHOOSE_FILE && resultCode == Activity.RESULT_CANCELED) {
            filePathCallback?.onReceiveValue(emptyArray())
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            fileChooserParams?.acceptTypes?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.putExtra(Intent.EXTRA_MIME_TYPES, it)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                this.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            this.addCategory(Intent.CATEGORY_OPENABLE)
        }
        val title = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            fileChooserParams?.title
        else
            null
        val chooser = Intent.createChooser(intent, title)
        startActivityForResult(chooser, CHOOSE_FILE)
    }

    fun choose(fileChooserParams: WebChromeClient.FileChooserParams?,
               callback: ValueCallback<Array<Uri>>?, fm: FragmentManager) {
        filePathCallback = callback
        this.fileChooserParams = fileChooserParams
        fm.beginTransaction().add(this, NAME).commit()
    }

    private fun onFileChosen(intent: Intent?) {
        val clipData = intent?.clipData
        if (clipData != null && clipData.itemCount > 0) {
            val uris = Array<Uri>(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
            filePathCallback?.onReceiveValue(uris)
        } else {
            val uri = intent?.data
            if (uri != null)
                filePathCallback?.onReceiveValue(arrayOf(uri))
        }
        dismiss()
    }

    private fun dismiss() {
        parentFragmentManager?.beginTransaction()?.remove(this)?.commit()
    }

}