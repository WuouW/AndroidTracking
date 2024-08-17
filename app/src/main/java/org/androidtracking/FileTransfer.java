package org.androidtracking;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileTransfer {
    JSONObject deviceInfo;

    HttpURLConnection urlConnection = null;

    public void setDeviceInfo(JSONObject deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public int transfer(){
        String urlString = "https://82.156.25.3/receive_data.php";

        CookieHandler.setDefault( new CookieManager( null, CookiePolicy.ACCEPT_ALL ) );

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);

            /*Thread threadNet = new Thread(new Runnable(){
                @Override
                public void run() {
                    try (OutputStream os = urlConnection.getOutputStream()) {
                        byte[] input = deviceInfo.toString().getBytes("utf-8");
                        //os.write(input, 0, input.length);
                        os.write(input);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
            });
            threadNet.start();

            try {
                threadNet.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = deviceInfo.toString().getBytes("utf-8");
                //os.write(input, 0, input.length);
                os.write(input);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            return urlConnection.getResponseCode();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
