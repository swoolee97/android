package com.example.test

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricHelper(private val context: Context, private val webView: WebView?) {
    private val executor: Executor = ContextCompat.getMainExecutor(context)
    private val biometricPrompt: BiometricPrompt
    private val promptInfo: BiometricPrompt.PromptInfo

    init {
        biometricPrompt = BiometricPrompt(context as FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    webView?.evaluateJavascript("alert('생체 인증 성공!')", null)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    webView?.evaluateJavascript("alert('생체 인증 실패: $errString')", null)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("생체 인증 필요")
            .setSubtitle("앱 액세스를 위해 인증하세요")
            .setNegativeButtonText("취소")
            .build()
    }

    fun authenticate() {
        if (isBiometricAvailable()) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            Log.e("BiometricHelper", "생체 인증이 지원되지 않음")
            webView?.evaluateJavascript("alert('생체 인증이 지원되지 않는 기기입니다.')", null)
        }
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        Log.d("BiometricHelper", "canAuthenticate() 반환값: $canAuthenticate")

        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }
}
