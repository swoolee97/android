package com.example.test;

import android.util.Log;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.Session;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AudioProcessor {
    private static final String TAG = "AudioProcessor";
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void processLiveRecording(File inputFile) {
        String outputDir = inputFile.getParent();  // 같은 폴더에 저장
        String outputFilePattern = outputDir + "/split_%03d.wav";  // 파일명 패턴

        Log.d(TAG, "🎤 실시간 녹음 감지됨: " + inputFile.getAbsolutePath());

        // moov atom 확인 후 변환
        checkMoovAtomAndProcess(inputFile, outputFilePattern);
    }

    private static void checkMoovAtomAndProcess(File inputFile, String outputFilePattern) {
        String filePath = inputFile.getAbsolutePath();

        // ffmpeg으로 moov atom 확인
        String checkCmd = "-i \"" + filePath + "\" -f null -";
        FFmpegKit.executeAsync(checkCmd, session -> {
            ReturnCode returnCode = session.getReturnCode();
            if (ReturnCode.isSuccess(returnCode)) {
                // ✅ 파일이 완전히 저장됨 → 변환 시작
                startSegmentConversion(filePath, outputFilePattern);
            } else {
                // 🚨 moov atom not found → 2초 후 다시 시도
                Log.e(TAG, "🚨 moov atom not found, 파일이 완전히 저장되지 않음: " + filePath);
                scheduler.schedule(() -> checkMoovAtomAndProcess(inputFile, outputFilePattern), 2, TimeUnit.SECONDS);
            }
        });
    }

    private static void startSegmentConversion(String inputFilePath, String outputDir) {
        String outputFilePattern = outputDir + "/split_%03d.wav"; // 파일 이름 패턴

        String cmd = "-i \"" + inputFilePath + "\" -c copy -f segment -segment_time 30 -reset_timestamps 1 \"" + outputFilePattern + "\"";

        FFmpegKit.executeAsync(cmd, session -> {
            ReturnCode returnCode = session.getReturnCode();
            if (ReturnCode.isSuccess(returnCode)) {
                Log.d(TAG, "✅ 30초 단위로 오디오가 분할됨!");
                sendLatestSegment(outputDir);
            } else {
                Log.e(TAG, "❌ 오디오 분할 실패: " + session.getAllLogsAsString());
            }
        });
    }

    private static void sendLatestSegment(String outputDir) {
        File dir = new File(outputDir);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".wav"));

        if (files != null && files.length > 0) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified)); // 최신 파일 찾기
            File latestFile = files[files.length - 1];

            Log.d(TAG, "🚀 최신 파일 전송: " + latestFile.getName());
            sendToOnDeviceAI(latestFile);
        }
    }

    private static void sendToOnDeviceAI(File audioFile) {
        // AI에 오디오 파일을 전송하는 로직 (예: HTTP 요청, 내부 SDK 호출 등)
        Log.d(TAG, "🎯 온디바이스 AI에 파일 전송: " + audioFile.getAbsolutePath());
    }


    private static void uploadSplitFiles(String directoryPath) {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles((d, name) -> name.startsWith("split_") && name.endsWith(".wav"));

        if (files != null) {
            for (File file : files) {
                uploadToAIModel(file);
            }
        }
    }

    private static void uploadToAIModel(File file) {
        Log.d(TAG, "📡 AI 모델에 업로드: " + file.getAbsolutePath());
        // Retrofit 또는 OkHttp를 사용하여 AI 서버에 업로드 가능
    }
}
