package com.example.test

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    private var callReceiver: CallReceiver? = null
    private var webView: WebView? = null
    lateinit var biometricHelper: BiometricHelper
    private var TAG: String = "asdf"
    private lateinit var sharedPreferences: SharedPreferences

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d(TAG, "🎤 RECORD_AUDIO 권한 허용됨!")
            } else {
                Log.e(TAG, "❌ RECORD_AUDIO 권한 거부됨!")
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences 초기화 (토큰 저장용)
        sharedPreferences = getSharedPreferences("TokenStorage", Context.MODE_PRIVATE)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            addJavascriptInterface(BiometricWebInterface(this@MainActivity), "AndroidBiometric") // 이 이름으로 웹뷰에서 생체 인증을 요청한다.
            addJavascriptInterface(TokenWebInterface(), "AndroidBridge") // 이 이름으로 웹뷰에서 토큰을 저장/요청한다.
            loadUrl("file:///android_asset/biometric.html") // 여기에 배포 url 홈화면 작성
        }
        setContentView(webView)

        biometricHelper = BiometricHelper(this, webView)

        Log.d("MainActivity", "onCreate 호출됨!")

        requestStoragePermission()

        requestPermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.RECORD_AUDIO
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

    private val requestManageStoragePermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d("Permission", "✅ MANAGE_EXTERNAL_STORAGE 권한 허용됨!")
                } else {
                    Log.e("Permission", "❌ MANAGE_EXTERNAL_STORAGE 권한 거부됨!")
                }
            }
        }

    fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                requestManageStoragePermission.launch(intent)
            }
        } else {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun isCallRecording(): Boolean {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        return audioManager.mode == AudioManager.MODE_IN_CALL && !audioManager.isMicrophoneMute
    }

    override fun onDestroy() {
        super.onDestroy()
        callReceiver?.let {
            unregisterReceiver(it)
            Log.d("MainActivity", "CallReceiver 해제됨!")
        }
    }

    // Android → WebView 브릿지 추가
    inner class TokenWebInterface {
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
}
