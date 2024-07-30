package com.mrrit.brusher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebStorage
import android.webkit.WebStorage.Origin
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.io.File


class SettingsFrag(mainActivity: MainActivity) : Fragment() {

    lateinit var storageTv : TextView
    lateinit var cacheTv : TextView
    lateinit var cookieTv : TextView
    var localStorageSpaceUsed : Long = -1
    var cacheSpaceUsed : Long = -1
    var cookiesSpaceUsed = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v =  inflater.inflate(R.layout.fragment_settings, container, false)

        storageTv = v.findViewById(R.id.frag_settings_storage_tv)
        cacheTv = v.findViewById(R.id.frag_settings_cache_tv)
        cookieTv = v.findViewById(R.id.frag_settings_cookie_tv)

        val webStrg = WebStorage.getInstance()
        webStrg.getOrigins { origins->
            localStorageSpaceUsed = 0
            for( origin in origins.values){
                localStorageSpaceUsed += (origin as Origin).usage
            }
            storageTv.text = "Local Storage Used : $localStorageSpaceUsed"

        }

        val cacheDir: File = File(requireActivity().cacheDir, "WebView")
        cacheSpaceUsed = getDirSize(cacheDir)

        storageTv.text = "Local Storage Used : $localStorageSpaceUsed"
        cacheTv.text = "Cache Storage Used : $cacheSpaceUsed"


        return v
    }

    private fun getDirSize(dir: File): Long {
        var size: Long = 0

        dir.listFiles()?.let {
            for (file in it) {
                if (file.isFile) {
                    size += file.length()
                } else if (file.isDirectory) {
                    size += getDirSize(file)
                }
            }
        }

        return size
    }



}