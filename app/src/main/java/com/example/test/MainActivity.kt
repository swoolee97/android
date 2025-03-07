package com.example.test

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    private var callReceiver: CallReceiver? = null
    private var webView: WebView? = null
    lateinit var biometricHelper: BiometricHelper

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(BiometricWebInterface(this@MainActivity), "AndroidBiometric")
            loadUrl("file:///android_asset/biometric.html")
        }
        setContentView(webView)

        biometricHelper = BiometricHelper(this, webView)

        Log.d("MainActivity", "onCreate 호출됨!")

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            )
        )
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
            val callLogGranted = permissions[Manifest.permission.READ_CALL_LOG] ?: false

            if (phoneStateGranted && callLogGranted) {
                Log.d("MainActivity", "권한이 허용됨!")
                registerCallReceiver()
            } else {
                Log.d("MainActivity", "권한이 거부됨!")
            }
        }

    private fun registerCallReceiver() {
        callReceiver = CallReceiver()
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(callReceiver, filter)
        Log.d("MainActivity", "CallReceiver 등록됨!")
    }

    override fun onDestroy() {
        super.onDestroy()
        callReceiver?.let {
            unregisterReceiver(it)
            Log.d("MainActivity", "CallReceiver 해제됨!")
        }
    }
}
