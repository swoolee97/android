package com.example.test.biometric

interface BiometricCallback {
    fun onBiometricSuccess()
    fun onBiometricFailure(errorMessage: String)
}
