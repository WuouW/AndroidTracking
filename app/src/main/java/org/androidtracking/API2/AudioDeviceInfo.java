package org.androidtracking.API2;

import org.androidtracking.DeviceInfo;
import org.json.JSONException;
import org.json.JSONObject;
import org.androidtracking.API2.FreqResponse.*;

import java.util.Iterator;
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
                signalGen.playTone(10);
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
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            threadPlay.join();
            threadRecord.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        signalGen.stopAudio();
        signalRecord.release();

        //处理频率响应生成的其他部分
        short[] recordedAudio = signalRecord.getAudioData();
        FreqResponseProcess processor = new FreqResponseProcess(sampleRate, frequencies);
        double[] normalizedFeatures = processor.process(recordedAudio);

        /*
        for(int i = 0; i < recordedAudio.length; i++){
            if(i < 5000)
                System.out.println("audio " + i + " : " + recordedAudio[i]);
        }*/

        AudioFeature AF = new AudioFeature(recordedAudio, normalizedFeatures, frequencies);
        JSONObject audioFeature = AF.calFeature();

        //打印归一化后的特征向量
        JSONObject audioInfo = new JSONObject();
        try {
            int idx = 1;
            for (double feature : normalizedFeatures) {
                System.out.println(feature);
                audioInfo.put("audioInfo "+idx, feature);
                idx++;
            }
            audioInfo = merge(audioInfo, audioFeature);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return audioInfo;
    }

    private JSONObject merge(JSONObject o1, JSONObject o2) throws JSONException {
        if(o2 == null)
            return o1;
        Iterator<String> keys = o2.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            o1.put(key, o2.get(key));
        }
        return o1;
    }
}