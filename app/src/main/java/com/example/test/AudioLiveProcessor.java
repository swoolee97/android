package com.example.test;

import android.os.Handler;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AudioLiveProcessor {
    private static final String TAG = "AudioLiveProcessor";
    private final File recordingFile;
    private final Handler handler = new Handler();
    private long lastSize = 0;
    private boolean isProcessing = false;

    public AudioLiveProcessor(File file) {
        this.recordingFile = file;
    }

    public void startProcessing() {
        handler.postDelayed(this::checkAndProcess, 5000);  // 5초마다 체크
    }

    private void checkAndProcess() {
        if (!recordingFile.exists()) {
            Log.e(TAG, "❌ 녹음 파일이 존재하지 않음!");
            return;
        }

        long currentSize = recordingFile.length();

        if (currentSize > lastSize) {
            long newBytes = currentSize - lastSize;
            Log.d(TAG, "📌 새로운 데이터 감지! 추가된 크기: " + newBytes + " bytes");

            // 새로 추가된 부분만 잘라서 처리
            extractNewAudioSegment(newBytes);

            // 마지막 읽은 위치 갱신
            lastSize = currentSize;
        }

        // 계속 반복 (녹음이 끝날 때까지)
        handler.postDelayed(this::checkAndProcess, 5000);
    }

    private void extractNewAudioSegment(long newBytes) {
        try (RandomAccessFile raf = new RandomAccessFile(recordingFile, "r")) {
            raf.seek(lastSize);  // 이전 위치부터 읽기 시작

            byte[] newAudioData = new byte[(int) newBytes];
            raf.readFully(newAudioData);

            // 🛠️ 새로 추가된 오디오 데이터 처리 (AI 전달)
            sendToAIModel(newAudioData);
        } catch (IOException e) {
            Log.e(TAG, "🚨 오디오 읽기 실패", e);
        }
    }

    private void sendToAIModel(byte[] audioData) {
        Log.d(TAG, "🚀 AI 모델에 새로운 오디오 데이터 전송! 크기: " + audioData.length);
        // AI 모델에 실시간 데이터 전달 (네트워크 요청 또는 로컬 호출)
    }
}

