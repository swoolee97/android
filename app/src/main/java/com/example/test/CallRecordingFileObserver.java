package com.example.test;

import android.os.FileObserver;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CallRecordingFileObserver extends FileObserver {
    private static final String TAG = "CallRecordingObserver";
    private final File observedDirectory;
    private long lastFileSize = 0;
    private long lastSentTime = 0;
    private static final long INTERVAL_MS = 30 * 1000; // 30초 기준 (밀리초)
    private static final long MIN_SIZE_TO_SEND = 512 * 1024; // 512KB (옵션: 크기 기준 전송)

    public CallRecordingFileObserver(String path) {
        super(path, FileObserver.CREATE | FileObserver.MODIFY | FileObserver.CLOSE_WRITE);
        this.observedDirectory = new File(path);
    }

    @Override
    public void onEvent(int event, String fileName) {
        if (fileName == null) return;

        File newFile = new File(observedDirectory, fileName);

        switch (event) {
            case FileObserver.CREATE:
                Log.d(TAG, "📌 새로운 녹음 파일 감지됨: " + newFile.getAbsolutePath());
                lastFileSize = 0;
                lastSentTime = System.currentTimeMillis();
                break;

            case FileObserver.MODIFY:
                Log.d(TAG, "🎤 녹음 파일 변경됨: " + newFile.getAbsolutePath());
                processNewData(newFile);
                break;

            case FileObserver.CLOSE_WRITE:
                Log.d(TAG, "✅ 녹음 완료됨: " + newFile.getAbsolutePath());
                saveDebugWav(newFile); // 🔥 디버깅용 WAV 파일 저장
                break;
        }
    }

    private void processNewData(File file) {
        if (!file.exists()) return;

        long currentSize = file.length();
        long currentTime = System.currentTimeMillis();

        if ((currentTime - lastSentTime >= INTERVAL_MS) || (currentSize - lastFileSize >= MIN_SIZE_TO_SEND)) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                raf.seek(lastFileSize);
                byte[] newData = new byte[(int) (currentSize - lastFileSize)];
                raf.readFully(newData);

                // 🔥 디버깅용 데이터 저장
                saveDebugFile(newData);

                // 🔥 AI 모델에 전송
                sendToAI(newData, file);

                lastFileSize = currentSize;
                lastSentTime = currentTime;
            } catch (Exception e) {
                Log.e(TAG, "❌ 파일 읽기 오류: " + e.getMessage());
            }
        }
    }

    private void sendToAI(byte[] audioData, File originalFile) {
        Log.d(TAG, "🚀 AI 모델에 데이터 전송: " + audioData.length + " 바이트");

        // 🔥 FFmpeg를 이용한 `.m4a` → `.wav` 변환 후 전송
        File wavFile = new File(observedDirectory, "output.wav");
        convertM4AToWav(originalFile, wavFile);

        // 🔥 AI 모델에 WAV 파일 전송
        sendWavToAI(wavFile);
    }

    private void saveDebugFile(byte[] audioData) {
        try {
            File debugFile = new File(observedDirectory, "debug_audio.raw");
            FileOutputStream fos = new FileOutputStream(debugFile, true);
            fos.write(audioData);
            fos.close();
            Log.d(TAG, "✅ 디버깅용 오디오 파일 저장 완료: " + debugFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "❌ 디버깅용 파일 저장 실패: " + e.getMessage());
        }
    }

    public void convertM4AToWav(File inputFile, File outputFile) {
        String command = "-i " + inputFile.getAbsolutePath() + " -acodec pcm_s16le -ar 16000 -ac 1 -f wav " + outputFile.getAbsolutePath();
//        .execute(command);
        Log.d(TAG, "✅ FFmpegKit 변환 완료: " + outputFile.getAbsolutePath());
    }


    private void sendWavToAI(File wavFile) {
        Log.d(TAG, "🚀 AI 모델에 WAV 파일 전송: " + wavFile.getAbsolutePath());
        // AI 모델로 전송하는 로직 추가 (예: HTTP POST)
    }

    private void saveDebugWav(File inputFile) {
        File debugWavFile = new File(observedDirectory, "debug_audio.wav");
        convertM4AToWav(inputFile, debugWavFile);
        Log.d(TAG, "✅ 디버깅용 WAV 파일 저장 완료: " + debugWavFile.getAbsolutePath());
    }

}
