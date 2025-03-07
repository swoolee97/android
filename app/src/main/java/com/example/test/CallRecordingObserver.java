package com.example.test;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;

public class CallRecordingObserver extends ContentObserver {
    private static final String TAG = "CallRecordingObserver";
    private final ContentResolver contentResolver;

    public CallRecordingObserver(Handler handler, ContentResolver contentResolver) {
        super(handler);
        this.contentResolver = contentResolver;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "🎤 새 통화 녹음 파일이 감지됨!");
        // 여기서 AI 모델로 파일을 전송하면 됨
    }
}
