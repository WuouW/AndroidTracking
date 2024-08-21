package org.androidtracking.API4;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioFingerprint {

    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

    public interface Callback {
        void onFingerprintGenerated(String fingerprint);
    }

    public static void generateFingerprint(final Callback callback) {
//        new Thread(() -> {
        @SuppressLint("MissingPermission") AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);

        short[] buffer = new short[BUFFER_SIZE];
        audioRecord.startRecording();

        int read;
        long sum = 0;

        for (int i = 0; i < SAMPLE_RATE / BUFFER_SIZE; i++) {
            read = audioRecord.read(buffer, 0, BUFFER_SIZE);

            for (int j = 0; j < read; j++) {
                sum += Math.abs(buffer[j]);
            }
        }

        audioRecord.stop();
        audioRecord.release();

        String fingerprint = Long.toString(sum);

        if (callback != null) {
            callback.onFingerprintGenerated(fingerprint);
        }
//        }).start();
    }
}
