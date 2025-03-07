package com.example.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static String lastState = "";  // 🔥 static 변수로 변경 (앱이 살아있는 동안 유지)

    @Override
    public void onReceive(Context context, Intent intent) {

        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (state == null || state.equals(lastState)) {
                // 🔥 상태가 변하지 않았으면 무시
                return;
            }
            lastState = state;  // 🔥 상태 업데이트

            Log.d(TAG, "📞 전화 상태 변경 감지됨: " + state);

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                handleRingingCall(context);
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                Log.d(TAG, "📲 통화 중!");
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                Log.d(TAG, "❌ 통화 종료됨!");
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
}
