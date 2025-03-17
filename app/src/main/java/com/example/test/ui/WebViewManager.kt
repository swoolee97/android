package com.example.test.ui

import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class WebViewManager(private val context: Context) {
    fun setupWebView(): WebView {
        return WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(BiometricWebInterface(context), "AndroidBiometric") // 브릿지 등록
            addJavascriptInterface(TokenWebInterface(context), "AndroidBridge")
            loadUrl("file:///android_asset/biometric.html") // 여기에 웹페이지 홈 등록
        }
    }
}
