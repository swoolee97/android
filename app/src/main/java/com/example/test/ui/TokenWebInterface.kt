package com.example.test.ui

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.webkit.JavascriptInterface

class TokenWebInterface(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("TokenStorage", Context.MODE_PRIVATE)
    private val TAG = "TokenWebInterface"

    @JavascriptInterface
    fun getAccessToken(): String {
        val accessToken = sharedPreferences.getString("accessToken", "No AccessToken")
        val refreshToken = sharedPreferences.getString("refreshToken", "No RefreshToken")

        // JavaScript에서 JSON 형태로 사용할 수 있도록 반환
        return """{"accessToken": "$accessToken", "refreshToken": "$refreshToken"}"""
    }

    @JavascriptInterface
    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString("accessToken", accessToken)
            putString("refreshToken", refreshToken)
            apply()
        }
        Log.d(TAG, "✅ AccessToken 및 RefreshToken 저장됨!")
    }
}