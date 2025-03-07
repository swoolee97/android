package com.example.test

import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.test.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    private var callReceiver: CallReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestTheme {
                Greeting("Android!!!!!!")
            }
        }

        Log.d("MainActivity", "onCreate 호출됨!")

        // 📌 `READ_PHONE_STATE` & `READ_CALL_LOG` 권한 체크 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
        } else {
            registerCallReceiver()
        }
    }

    private fun requestPermissions() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
            val callLogGranted = permissions[Manifest.permission.READ_CALL_LOG] ?: false

            if (phoneStateGranted && callLogGranted) {
                Log.d("MainActivity", "권한이 허용됨!")
                registerCallReceiver()
            } else {
                Log.d("MainActivity", "권한이 거부됨!")
            }
        }

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG
            )
        )
    }

    private fun registerCallReceiver() {
        callReceiver = CallReceiver()
        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        registerReceiver(callReceiver, filter)
        Log.d("MainActivity", "CallReceiver 등록됨!")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (callReceiver != null) {
            unregisterReceiver(callReceiver)
            Log.d("MainActivity", "CallReceiver 해제됨!")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestTheme {
        Greeting("Android")
    }
}