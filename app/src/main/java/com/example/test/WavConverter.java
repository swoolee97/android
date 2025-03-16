package com.example.test;

import java.io.*;

public class WavConverter {

    private static final int SAMPLE_RATE = 16000; // 샘플레이트 (16kHz)
    private static final int CHANNELS = 1; // 모노 (1), 스테레오 (2)
    private static final int BITS_PER_SAMPLE = 16; // 16비트 오디오

    public static void writeWavFile(byte[] pcmData, File outputWavFile) {
        try (FileOutputStream fos = new FileOutputStream(outputWavFile)) {
            // 1️⃣ WAV 헤더 생성
            byte[] wavHeader = createWavHeader(pcmData.length, SAMPLE_RATE, CHANNELS, BITS_PER_SAMPLE);

            // 2️⃣ WAV 파일에 헤더 + PCM 데이터 쓰기
            fos.write(wavHeader);
            fos.write(pcmData);

            System.out.println("✅ WAV 파일 변환 완료: " + outputWavFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ WAV 파일 변환 오류: " + e.getMessage());
        }
    }

    private static byte[] createWavHeader(int pcmDataSize, int sampleRate, int channels, int bitsPerSample) {
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int totalDataLen = pcmDataSize + 36;
        byte[] header = new byte[44];

        // "RIFF" 헤더
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        // 파일 전체 크기 (44바이트 WAV 헤더 + PCM 데이터)
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);

        // "WAVE" 포맷
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // "fmt " 서브청크
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';

        // fmt 서브청크 크기 (16바이트)
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;

        // 오디오 포맷 (PCM = 1)
        header[20] = 1;
        header[21] = 0;

        // 채널 수
        header[22] = (byte) channels;
        header[23] = 0;

        // 샘플 레이트
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);

        // 바이트 레이트 (샘플레이트 * 채널 수 * 비트 깊이 / 8)
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // 블록 정렬 (채널 수 * 비트 깊이 / 8)
        header[32] = (byte) (channels * bitsPerSample / 8);
        header[33] = 0;

        // 비트 깊이 (16bit)
        header[34] = (byte) bitsPerSample;
        header[35] = 0;

        // "data" 서브청크
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';

        // PCM 데이터 크기
        header[40] = (byte) (pcmDataSize & 0xff);
        header[41] = (byte) ((pcmDataSize >> 8) & 0xff);
        header[42] = (byte) ((pcmDataSize >> 16) & 0xff);
        header[43] = (byte) ((pcmDataSize >> 24) & 0xff);

        return header;
    }
}
