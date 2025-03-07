package com.example.test

import android.webkit.JavascriptInterface
import android.util.Log

class BiometricWebInterface(private val mainActivity: MainActivity) {
    @JavascriptInterface
    fun authenticate() {
        Log.d("BiometricWebInterface", "authenticate() 호출됨")
        mainActivity.runOnUiThread {
            mainActivity.biometricHelper.authenticate()
        }
    }
}
