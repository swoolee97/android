package com.example.test.biometric

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricHelper(private val context: Context, private val callback: BiometricCallback) {
    private val executor: Executor = ContextCompat.getMainExecutor(context)
    private val biometricPrompt: BiometricPrompt
    private val promptInfo: BiometricPrompt.PromptInfo

    init {
        biometricPrompt = BiometricPrompt(context as FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.onBiometricSuccess() // ✅ 성공 시 콜백 실행
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.onBiometricFailure(errString.toString()) // ✅ 실패 시 콜백 실행
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
            callback.onBiometricFailure("생체 인증이 지원되지 않는 기기입니다.") // ✅ 생체 인증 불가 시 콜백 실행
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
