package com.example.test;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RealTimeFileStreamer {
    private static final String TAG = "RealTimeFileStreamer";
    private final File recordingFile;
    private long lastFileSize = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public RealTimeFileStreamer(File file) {
        this.recordingFile = file;
    }

    public void startStreaming() {
        FileObserver observer = new FileObserver(recordingFile.getAbsolutePath(), FileObserver.MODIFY) {
            @Override
            public void onEvent(int event, String path) {
                if (event == FileObserver.MODIFY) {
                    executor.execute(() -> checkAndSendNewData());
                }
            }
        };
        observer.startWatching();
    }

    private void checkAndSendNewData() {
        long currentSize = recordingFile.length();
        if (currentSize > lastFileSize) {
            long newBytes = currentSize - lastFileSize;
            lastFileSize = currentSize;
            extractNewAudioSegment(newBytes);
        }
    }

    private void extractNewAudioSegment(long newBytes) {
        try (RandomAccessFile raf = new RandomAccessFile(recordingFile, "r")) {
            raf.seek(lastFileSize - newBytes);

            byte[] newAudioData = new byte[(int) newBytes];
            raf.readFully(newAudioData);

            // 🔥 AI 모델에 전송
            sendToAIModel(newAudioData);
        } catch (IOException e) {
            Log.e(TAG, "🚨 오디오 읽기 실패", e);
        }
    }

    private void sendToAIModel(byte[] audioData) {
        Log.d(TAG, "🚀 AI 모델에 새로운 오디오 데이터 전송! 크기: " + audioData.length);
        // 여기에 AI 모델 호출하는 코드 추가
    }
}

