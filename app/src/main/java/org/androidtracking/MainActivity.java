package org.androidtracking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.androidtracking.API1.BasicDeviceInfo;
import org.androidtracking.API2.AudioDeviceInfo;
import org.androidtracking.API3.SensorDeviceInfo;
import org.androidtracking.API4.WebAudioDeviceInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {
    JSONObject fingerprintInfo = new JSONObject();
    String fingerprint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleSSLHandshake();

        Fingerprint fp = new Fingerprint(this, this);

        // 权限检查与申请
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        // 介绍
        TextView textViewIntro = findViewById(R.id.introduction);
        textViewIntro.setText(
                "请先打开声音，然后点击“信息获取”按钮，当出现了“信息获取完成”的字样后，再点击“网络传输”按钮，传输完成后会出现“网络传输完成”的字样，即可退出。"
        );

        // 1. 信息获取部分
        Button buttonGetInfo = findViewById(R.id.buttonGetInfo);
        TextView textViewGetInfo = findViewById(R.id.textViewGetInfo);
        buttonGetInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewGetInfo.setVisibility(View.INVISIBLE);
                fp.generateFingerprintInfo((res) -> {
                    runOnUiThread(() -> {
                        fingerprintInfo = fp.getFingerprintInfo();
                        fingerprint = fp.getFingerprint();
                        textViewGetInfo.setVisibility(View.VISIBLE);
                    });
                });

            }
        });

        // 2. 网络传输部分
        Button buttonNetTrans = findViewById(R.id.buttonNetTrans);
        TextView textViewNetTrans = findViewById(R.id.textViewNetTrans);
        TextView textViewNetTransERR = findViewById(R.id.textViewNetTransERR);
        buttonNetTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewNetTrans.setVisibility(View.INVISIBLE);
                textViewNetTransERR.setVisibility(View.INVISIBLE);
                FileTransfer fileTransfer = new FileTransfer();
                fileTransfer.setDeviceInfo(fingerprintInfo);
                // 在另一个线程中进行网络传输
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int responseCode = fileTransfer.transfer();
                        if (responseCode == 200) {
                            // UI操作要在主线程中进行
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewNetTrans.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {  // 网络传输失败
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewNetTransERR.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                }).start();

                /* int responseCode = fileTransfer.transfer();
                if(responseCode == 200) {
                    textViewNetTrans.setVisibility(View.VISIBLE);
                }*/
            }
        });

        //TODO:可选：指纹的展示
    }

    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }


}