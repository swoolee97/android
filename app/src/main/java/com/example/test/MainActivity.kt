package com.example.test

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import com.example.test.biometric.BiometricCallback
import com.example.test.biometric.BiometricHelper
import com.example.test.call.CallReceiver
import com.example.test.permissions.PermissionManager
import com.example.test.ui.WebViewManager

class MainActivity : FragmentActivity() {
    private var callReceiver: CallReceiver? = null
    var webView: WebView? = null
    lateinit var biometricHelper: BiometricHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var permissionManager: PermissionManager  // ✅ PermissionManager 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("TokenStorage", Context.MODE_PRIVATE)

        biometricHelper = BiometricHelper(this, object : BiometricCallback {
            override fun onBiometricSuccess() {
                runOnUiThread {
                    webView?.evaluateJavascript("alert('생체 인증 성공!')", null)
                }
            }

            override fun onBiometricFailure(errorMessage: String) {
                runOnUiThread {
                    webView?.evaluateJavascript("alert('생체 인증 실패: $errorMessage')", null)
                }
            }
        })

        val webViewManager = WebViewManager(this)
        webView = webViewManager.setupWebView()
        setContentView(webView)
        Log.d("MainActivity", "onCreate 호출됨!")

        // ✅ PermissionManager 초기화
        permissionManager = PermissionManager(this)

        // ✅ 필수 권한 요청
        permissionManager.requestPermissions(
            onGranted = {
                Log.d("MainActivity", "✅ 모든 권한 허용됨!")
                registerCallReceiver()
            },
            onDenied = {
                Log.e("MainActivity", "❌ 필수 권한이 거부되었습니다.")
            }
        )

        // ✅ 저장소 권한 요청
        permissionManager.requestStoragePermission()
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
