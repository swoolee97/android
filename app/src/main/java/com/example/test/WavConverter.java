package com.example.test;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavConverter {

    public static void saveToWavFile(byte[] audioData, File outputFile) {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // WAV 헤더 추가
            fos.write(createWavHeader(audioData.length));
            // 오디오 데이터 추가
            fos.write(audioData);
            fos.close();

            Log.d(TAG, "✅ WAV 파일 저장 완료: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "❌ WAV 파일 저장 실패: " + e.getMessage());
        }
    }

    private static byte[] createWavHeader(int dataSize) {
        int totalDataLen = dataSize + 36;
        int sampleRate = 16000;  // 샘플레이트 (16kHz)
        int channels = 1;  // 모노
        int byteRate = sampleRate * channels * 2;

        ByteBuffer buffer = ByteBuffer.allocate(44);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // "RIFF" Chunk
        buffer.put("RIFF".getBytes());
        buffer.putInt(totalDataLen);
        buffer.put("WAVE".getBytes());

        // "fmt " Sub-chunk
        buffer.put("fmt ".getBytes());
        buffer.putInt(16);  // Sub-chunk size
        buffer.putShort((short) 1);  // Audio format (1 = PCM)
        buffer.putShort((short) channels);
        buffer.putInt(sampleRate);
        buffer.putInt(byteRate);
        buffer.putShort((short) (channels * 2));  // Block align
        buffer.putShort((short) 16);  // Bits per sample

        // "data" Sub-chunk
        buffer.put("data".getBytes());
        buffer.putInt(dataSize);

        return buffer.array();
    }

}
