package com.example.test.ui

import android.content.Context
import android.webkit.JavascriptInterface
import com.example.test.MainActivity
import com.example.test.biometric.BiometricCallback
import com.example.test.biometric.BiometricHelper

class BiometricWebInterface(
    private val context: Context
) : BiometricCallback {

    private val biometricHelper = BiometricHelper(context, this) // ✅ 콜백 전달

    @JavascriptInterface
    fun authenticate() {
        biometricHelper.authenticate()
    }

    override fun onBiometricSuccess() {
        // ✅ WebView에서 생체 인증 성공 메시지 표시
        (context as? MainActivity)?.runOnUiThread {
            (context.webView)?.evaluateJavascript("alert('생체 인증 성공!')", null)
        }
    }

    override fun onBiometricFailure(errorMessage: String) {
        // ✅ WebView에서 생체 인증 실패 메시지 표시
        (context as? MainActivity)?.runOnUiThread {
            (context.webView)?.evaluateJavascript("alert('생체 인증 실패: $errorMessage')", null)
        }
    }
}