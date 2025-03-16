package com.example.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.Manifest;

import androidx.core.content.ContextCompat;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static String lastState = "";  // 🔥 static 변수로 변경 (앱이 살아있는 동안 유지)
    private final Handler handler = new Handler(Looper.getMainLooper());
    private CallRecordingObserver callRecordingObserver;

    private CallRecordingFileObserver fileObserver;
    private static final String RECORDING_PATH = "/storage/emulated/0/Recordings/Call/"; // 삼성폰 기준

    private void registerCallRecordingObserver() {
        if (fileObserver == null) {
            fileObserver = new CallRecordingFileObserver(RECORDING_PATH);
            fileObserver.startWatching();
            Log.d(TAG, "📡 통화 녹음 감지 시작 (FileObserver)!");
        }
    }

    private void unregisterCallRecordingObserver() {
        if (fileObserver != null) {
            fileObserver.stopWatching();
            fileObserver = null;
            Log.d(TAG, "📡 통화 녹음 감지 중지 (FileObserver)!");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (state == null || state.equals(lastState)) {
                return;
            }
            lastState = state;

            Log.d(TAG, "📞 전화 상태 변경 감지됨: " + state);

            if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                Log.d(TAG, "📲 통화 중!");
                registerCallRecordingObserver(); // 파일 생성 감지 시작
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                Log.d(TAG, "❌ 통화 종료됨!");
                unregisterCallRecordingObserver(); // 파일 생성 감지 중지
            }
        }
    }

    private void handleRingingCall(Context context) {
        String phoneNumber = getLastIncomingNumber(context);
        Log.d(TAG, "☎️ 전화가 오고 있음! 번호: " + phoneNumber);
    }

    private String getLastIncomingNumber(Context context) {
        Uri callUri = CallLog.Calls.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(
                callUri,
                null,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            String lastCallNumber = cursor.getString(numberIndex);
            cursor.close();
            return lastCallNumber;
        }
        return "알 수 없음";
    }

    private void registerCallRecordingObserver(Context context) {
        if (callRecordingObserver == null) {
            callRecordingObserver = new CallRecordingObserver(new Handler(Looper.getMainLooper()), context.getContentResolver());
            context.getContentResolver().registerContentObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    true,
                    callRecordingObserver
            );
            Log.d(TAG, "📡 통화 녹음 감지 시작!");
        }
    }

    private void unregisterCallRecordingObserver(Context context) {
        if (callRecordingObserver != null) {
            context.getContentResolver().unregisterContentObserver(callRecordingObserver);
            callRecordingObserver = null;
            Log.d(TAG, "📡 통화 녹음 감지 중지!");
        }
    }

}
