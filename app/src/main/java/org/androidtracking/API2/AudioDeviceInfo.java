package org.androidtracking.API2;

import org.androidtracking.DeviceInfo;
import org.json.JSONObject;
import org.androidtracking.API2.FreqResponse.*;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class AudioDeviceInfo implements DeviceInfo {
    private final int sampleRate; //采样率

    private final int[] frequencies; //频率范围：14kHz~21kHz

    public AudioDeviceInfo() {
        sampleRate = 44100;

        //int startFrequency = 14000;
        int startFrequency = 14000;
        int endFrequency = 21000;
        int interval = 100;
        int numFrequencies = (endFrequency-startFrequency) / interval + 1;
        frequencies = new int[numFrequencies];
        int i = 0;
        for (int freq = startFrequency; freq <= endFrequency; freq += interval) {
            frequencies[i++] = freq;
        }

        System.out.println("init: down\n");
    }

    @Override
    public JSONObject getInfo() {
        //TODO
        SignalGen signalGen = new SignalGen(sampleRate, frequencies);
        SignalRecord signalRecord = new SignalRecord(sampleRate);

        System.out.println("init gen and record: down\n");

        CyclicBarrier barrier = new CyclicBarrier(2); // 屏障数（用来保障同步）

        Thread threadPlay = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                signalGen.playTone(1);
            }
        });
        Thread threadRecord = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    barrier.await();
                } catch (BrokenBarrierException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
                signalRecord.recordAudio(1);
            }
        });

        threadPlay.start();
        threadRecord.start();

        try {
            threadPlay.join();
            threadRecord.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        signalGen.stopAudio();
        signalRecord.release();

        //signalGen.playTone(1);
        //signalRecord.recordAudio(1);
        //signalGen.stopAudio();
        //signalRecord.release();

        //处理频率响应生成的其他部分
        short[] recordedAudio = signalRecord.getAudioData();
        FreqResponseProcess processor = new FreqResponseProcess(sampleRate, frequencies);
        double[] normalizedFeatures = processor.process(recordedAudio);

        //打印归一化后的特征向量
        for (double feature : normalizedFeatures) {
            System.out.print(feature + " ");
        }

        return null;
    }
}
