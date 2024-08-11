package org.androidtracking.API1;

import static android.Manifest.permission.USE_ICC_AUTH_WITH_DEVICE_IDENTIFIER;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.InputDevice;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import androidx.core.app.ActivityCompat;

import org.androidtracking.DeviceInfo;
import org.androidtracking.Fingerprint;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

public class BasicDeviceInfo implements DeviceInfo {
    private Context context;
    private Activity activity;

    public void setContextActivity(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }


    @Override
    public JSONObject getInfo() {
        //TODO
        JSONObject basicInfo = new JSONObject();
        try {
            /*--------------------AudioManager--------------------*/
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            basicInfo.put("max volumn", new int[] {
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM),
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM),
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF)
            });

            /*--------------------SensorManager--------------------*/
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            basicInfo.put("sensors", sensorManager.getSensorList(Sensor.TYPE_ALL));

            /*--------------------PackageManager--------------------*/
            PackageManager packageManager = context.getPackageManager();
            basicInfo.put("packages", packageManager.getInstalledPackages(0));

            /*--------------------ActivityManager--------------------*/
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memInfo);
            basicInfo.put("memory", memInfo);
            basicInfo.put("isUserAMonkey", ActivityManager.isUserAMonkey());

            /*------------------------StatFs------------------------*/
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
            basicInfo.put("totalBytes", statFs.getTotalBytes());
            basicInfo.put("blockSize", statFs.getBlockSizeLong());
            basicInfo.put("availableBlocks", statFs.getAvailableBlocksLong());
            basicInfo.put("blockCount", statFs.getBlockCountLong());
            basicInfo.put("freeBlocks", statFs.getFreeBlocksLong());
            basicInfo.put("freeBytes", statFs.getFreeBytes());

            /*--------------------InputDevice--------------------*/
            int[] deviceIDs = InputDevice.getDeviceIds();
            List<String> deviceNames = new ArrayList<>();
            List<Integer> vendorIDs = new ArrayList<>();
            List<Integer> keyboardTypes = new ArrayList<>();
            for (int deviceID : deviceIDs) {
                InputDevice device = InputDevice.getDevice(deviceID);
                if(device != null) {
                    deviceNames.add(device.getName());
                    vendorIDs.add(device.getVendorId());
                    keyboardTypes.add(device.getKeyboardType());
                }
            }
            basicInfo.put("deviceNames", deviceNames);
            basicInfo.put("vendorIDs", vendorIDs);
            basicInfo.put("keyboardTypes", keyboardTypes);

            /*--------------------ConfigurationInfo--------------------*/
            ConfigurationInfo configInfo = new ConfigurationInfo();
            basicInfo.put("GlEsVersion", configInfo.getGlEsVersion());

            /*--------------------DevicePolicyManager--------------------*/
            DevicePolicyManager devicePolicyManager =
                    (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            basicInfo.put("StorageEncryptionStatus", devicePolicyManager.getStorageEncryptionStatus());

            /*--------------------Security--------------------*/
            basicInfo.put("providers", Security.getProviders());

            /*--------------------KeyguardManager--------------------*/
            KeyguardManager keyguardManager =
                    (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            basicInfo.put("isKeyguardSecure", keyguardManager.isKeyguardSecure());

            /*--------------------RingtoneManager--------------------*/
            RingtoneManager ringtoneManager = new RingtoneManager(context);
            basicInfo.put("RingtoneUri", new Uri[] {
                    ringtoneManager.getRingtoneUri(RingtoneManager.TYPE_RINGTONE),
                    ringtoneManager.getRingtoneUri(RingtoneManager.TYPE_NOTIFICATION),
                    ringtoneManager.getRingtoneUri(RingtoneManager.TYPE_ALARM),
                    ringtoneManager.getRingtoneUri(RingtoneManager.TYPE_ALL)
            });
            basicInfo.put("RingtoneUri", new Ringtone[] {
                    ringtoneManager.getRingtone(RingtoneManager.TYPE_RINGTONE),
                    ringtoneManager.getRingtone(RingtoneManager.TYPE_NOTIFICATION),
                    ringtoneManager.getRingtone(RingtoneManager.TYPE_ALARM),
                    ringtoneManager.getRingtone(RingtoneManager.TYPE_ALL)
            });

            /*--------------------AssetManager--------------------*/
            AssetManager assetManager = context.getAssets();
            basicInfo.put("locales1", assetManager.getLocales());

            /*--------------------Configuration--------------------*/
            Configuration configuration = context.getResources().getConfiguration();
            basicInfo.put("locales2", configuration.getLocales());

            /*--------------------Locale--------------------*/
            Locale locale = context.getResources().getConfiguration().getLocales().get(0);
            basicInfo.put("language", locale.getLanguage());
            basicInfo.put("country", locale.getCountry());

            /*--------------------InputMethodSubtype--------------------*/
            InputMethodManager inputMethodManager =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            InputMethodSubtype inputMethodSubtype = inputMethodManager.getCurrentInputMethodSubtype();
            basicInfo.put("languageType", inputMethodSubtype.getLanguageTag());

            /*--------------------TimeZone--------------------*/
            TimeZone timeZone = TimeZone.getDefault();
            basicInfo.put("TimeZoneDisplayName", timeZone.getDisplayName());
            basicInfo.put("TimeZoneID", timeZone.getID());

            /*--------------------Properties--------------------*/
            Properties properties = System.getProperties();
            basicInfo.put("java.home", properties.getProperty("java.home"));
            basicInfo.put("java.library.path", properties.getProperty("java.library.path"));
            basicInfo.put("java.specification.version", properties.getProperty("java.specification.version"));
            basicInfo.put("java.vm.version", properties.getProperty("java.vm.version"));
            basicInfo.put("java.vm.specification.version", properties.getProperty("java.vm.specification.version"));
            basicInfo.put("os.arch", properties.getProperty("os.arch"));
            basicInfo.put("os.name", properties.getProperty("os.name"));
            basicInfo.put("os.version", properties.getProperty("os.version"));

            /*--------------------Runtime--------------------*/
            Runtime runtime = Runtime.getRuntime();
            basicInfo.put("availableProcessors", runtime.availableProcessors());

            /*--------------------Camera--------------------*/
            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            List<CameraCharacteristics> cameraCharacteristics = new ArrayList<>();
            try {
                String[] cameraIdList = cameraManager.getCameraIdList();
                for (String cameraId : cameraIdList) {
                    cameraCharacteristics.add(cameraManager.getCameraCharacteristics(cameraId));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            basicInfo.put("cameraCharacteristics", cameraCharacteristics);

            /*--------------------PowerProfile--------------------*/
            Object batteryCapacity = null;
            try {
                @SuppressLint("PrivateApi")
                Class<?> powerProfileClass = Class.forName("com.android.internal.os.PowerProfile");
                Object powerProfile = powerProfileClass.getConstructor(Context.class).newInstance(context);
                Method getBatteryCapacityMethod = powerProfileClass.getMethod("getBatteryCapacity", String.class);
                batteryCapacity = getBatteryCapacityMethod.invoke(powerProfile, (Object) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            basicInfo.put("batteryCapacity", batteryCapacity);

            /*--------------------Settings--------------------*/
            // Global
            basicInfo.put("ADB_ENABLED", Settings.Global.getString(context.getContentResolver(), Settings.Global.ADB_ENABLED));
            basicInfo.put("WIFI_ON", Settings.Global.getString(context.getContentResolver(), Settings.Global.WIFI_ON));
            basicInfo.put("ACCESSIBILITY_DISPLAY_INVERSION_ENABLED", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_DISPLAY_INVERSION_ENABLED));
            basicInfo.put("ACCESSIBILITY_ENABLED", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED));
            basicInfo.put("ANDROID_ID", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
            basicInfo.put("ACCELEROMETER_ROTATION", Settings.System.getString(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION));
            basicInfo.put("BLUETOOTH_DISCOVERABILITY", Settings.System.getString(context.getContentResolver(), Settings.System.BLUETOOTH_DISCOVERABILITY));
            basicInfo.put("BLUETOOTH_DISCOVERABILITY_TIMEOUT", Settings.System.getString(context.getContentResolver(), Settings.System.BLUETOOTH_DISCOVERABILITY_TIMEOUT));
            basicInfo.put("RINGTONE", Settings.System.getString(context.getContentResolver(), Settings.System.RINGTONE));
            basicInfo.put("SCREEN_BRIGHTNESS", Settings.System.getString(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
            basicInfo.put("SCREEN_BRIGHTNESS_MODE", Settings.System.getString(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE));
            basicInfo.put("TEXT_AUTO_CAPS", Settings.System.getString(context.getContentResolver(), Settings.System.TEXT_AUTO_CAPS));
            basicInfo.put("TEXT_AUTO_PUNCTUATE", Settings.System.getString(context.getContentResolver(), Settings.System.TEXT_AUTO_PUNCTUATE));
            basicInfo.put("TEXT_AUTO_REPLACE", Settings.System.getString(context.getContentResolver(), Settings.System.TEXT_AUTO_REPLACE));
            basicInfo.put("TEXT_SHOW_PASSWORD", Settings.System.getString(context.getContentResolver(), Settings.System.TEXT_SHOW_PASSWORD));
            basicInfo.put("TIME_12_24", Settings.System.getString(context.getContentResolver(), Settings.System.TIME_12_24));
            basicInfo.put("VIBRATE_ON", Settings.System.getString(context.getContentResolver(), Settings.System.VIBRATE_ON));


            /*--------------------Display--------------------*/
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            // size range
            Point sizeRangeMin = new Point();
            Point sizeRangeMax = new Point();
            display.getCurrentSizeRange(sizeRangeMin, sizeRangeMax);
            // metrics & real metrics
            DisplayMetrics metrics = new DisplayMetrics();
            DisplayMetrics realMetrics = new DisplayMetrics();
            display.getMetrics(metrics);
            display.getRealMetrics(realMetrics);
            // real size
            Point realSize = new Point();
            display.getRealSize(realSize);
            // rect
            Rect rect = new Rect();
            display.getRectSize(rect);
            /*write*/
            basicInfo.put("sizeRange", new Point[] {sizeRangeMin, sizeRangeMax});
            basicInfo.put("metrics", metrics);
            basicInfo.put("real metrics", realMetrics);
            basicInfo.put("real size", realSize);
            basicInfo.put("rect", rect);

            /*--------------------Resources--------------------*/
            Resources resources = context.getResources();
            basicInfo.put("displayMetrics", resources.getDisplayMetrics());

            /*--------------------WifiInfo--------------------*/
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            basicInfo.put("BSSID", wifiInfo.getBSSID());
            basicInfo.put("IpAddress", wifiInfo.getIpAddress());
            basicInfo.put("SSID", wifiInfo.getSSID());
            basicInfo.put("MacAddress", wifiInfo.getMacAddress());
            basicInfo.put("networkID", wifiInfo.getNetworkId());

            /*--------------------ConnectivityManager--------------------*/
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            Network[] networks = connectivityManager.getAllNetworks();
            List<LinkProperties> linkProperties = new ArrayList<>();
            for (Network network : networks){
                LinkProperties linkProperty = connectivityManager.getLinkProperties(network);
                if (linkProperty != null) {
                    linkProperties.add(linkProperty);
                }
            }
            basicInfo.put("linkProperties", linkProperties);
            basicInfo.put("isActiveNetworkMetered", connectivityManager.isActiveNetworkMetered());
            basicInfo.put("ActiveNetworkInfo", connectivityManager.getActiveNetwork());
            basicInfo.put("DefaultProxy", connectivityManager.getDefaultProxy());

            /*--------------------WifiManager--------------------*/
            basicInfo.put("connection info", wifiManager.getConnectionInfo());
            basicInfo.put("5GHzBand", wifiManager.is5GHzBandSupported());
            basicInfo.put("DeviceToApRtt", wifiManager.isDeviceToApRttSupported());
            basicInfo.put("EnhancedPowerReporting", wifiManager.isEnhancedPowerReportingSupported());
            basicInfo.put("P2p", wifiManager.isP2pSupported());
            basicInfo.put("PreferredNetworkOffload", wifiManager.isPreferredNetworkOffloadSupported());
            basicInfo.put("ScanAvailable", wifiManager.isScanAlwaysAvailable());
            basicInfo.put("Tdls", wifiManager.isTdlsSupported());

            /*--------------------TelephoneManager--------------------*/
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
                //basicInfo.put("deviceID", telephonyManager.getDeviceId());
                //basicInfo.put("IMEI", telephonyManager.getImei());
                //basicInfo.put("meid", telephonyManager.getMeid());
                basicInfo.put("networkOperator", telephonyManager.getNetworkOperator());
                basicInfo.put("networkOperatorName", telephonyManager.getNetworkOperatorName());
                basicInfo.put("simOperator", telephonyManager.getSimOperator());
                basicInfo.put("simOperatorName", telephonyManager.getSimOperatorName());
                //basicInfo.put("simSerialNumber", telephonyManager.getSimSerialNumber());
                //basicInfo.put("networkSpecifier", telephonyManager.getNetworkSpecifier());
                basicInfo.put("dataNetworkType", telephonyManager.getDataNetworkType());
                basicInfo.put("MmsUAProfUrl", telephonyManager.getMmsUAProfUrl());
                basicInfo.put("MmsUserAgent", telephonyManager.getMmsUserAgent());
                //basicInfo.put("Nai", telephonyManager.getNai());
                basicInfo.put("networkCountryIso", telephonyManager.getNetworkCountryIso());
                basicInfo.put("phoneCount", telephonyManager.getPhoneType());
                basicInfo.put("phoneType", telephonyManager.getPhoneCount());
                //basicInfo.put("ActiveModemCount", telephonyManager.getActiveModemCount());
                basicInfo.put("simState", telephonyManager.getSimState());
                //basicInfo.put("subscriberID", telephonyManager.getSubscriberId());
                basicInfo.put("VoiceMailAlphaTag", telephonyManager.getVoiceMailAlphaTag());
                basicInfo.put("VoiceMailNumber", telephonyManager.getVoiceMailNumber());
                basicInfo.put("IccCard", telephonyManager.hasIccCard());
                basicInfo.put("HearingAidCompatibility", telephonyManager.isHearingAidCompatibilitySupported());
                basicInfo.put("NetworkRoaming", telephonyManager.isNetworkRoaming());
                basicInfo.put("SmsCapable", telephonyManager.isSmsCapable());
                basicInfo.put("TtyMode", telephonyManager.isTtyModeSupported());
                basicInfo.put("voice", telephonyManager.isVoiceCapable());
                basicInfo.put("worldPhone", telephonyManager.isWorldPhone());
            }

            /*--------------------AccountManager--------------------*/
            AccountManager accountManager = AccountManager.get(context);
            basicInfo.put("accounts", accountManager.getAccounts());

            /*--------------------BluetoothAdapter--------------------*/
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
                }
                basicInfo.put("blueToothAddress", bluetoothAdapter.getAddress());
                basicInfo.put("BondedDevices", bluetoothAdapter.getBondedDevices());
                basicInfo.put("blueToothName", bluetoothAdapter.getName());
            }

            /*--------------------Build--------------------*/
            basicInfo.put("Build.BOARD", Build.BOARD);
            basicInfo.put("Build.BOOTLOADER", Build.BOOTLOADER);
            basicInfo.put("Build.BRAND", Build.BRAND);
            basicInfo.put("Build.DEVICE", Build.DEVICE);
            basicInfo.put("Build.DISPLAY", Build.DISPLAY);
            basicInfo.put("Build.FINGERPRINT", Build.FINGERPRINT);
            basicInfo.put("Build.HARDWARE", Build.HARDWARE);
            basicInfo.put("Build.HOST", Build.HOST);
            basicInfo.put("Build.ID", Build.ID);
            basicInfo.put("Build.MANUFACTURER", Build.MANUFACTURER);
            basicInfo.put("Build.MODEL", Build.MODEL);
            basicInfo.put("Build.PRODUCT", Build.PRODUCT);
            basicInfo.put("Build.SERIAL", Build.SERIAL);
            basicInfo.put("Build.SUPPORTED_32_BIT_ABIS", Arrays.toString(Build.SUPPORTED_32_BIT_ABIS));
            basicInfo.put("Build.SUPPORTED_64_BIT_ABIS", Arrays.toString(Build.SUPPORTED_64_BIT_ABIS));
            basicInfo.put("Build.SUPPORTED_ABIS", Arrays.toString(Build.SUPPORTED_ABIS));
            basicInfo.put("Build.TIME", Build.TIME);
            basicInfo.put("Build.TYPE", Build.TYPE);
            basicInfo.put("Build.UNKNOWN", Build.UNKNOWN);
            basicInfo.put("Build.USER", Build.USER);
            basicInfo.put("Build.VERSION.BASE_OS", Build.VERSION.BASE_OS);
            basicInfo.put("Build.VERSION.CODENAME", Build.VERSION.CODENAME);
            basicInfo.put("Build.VERSION.INCREMENTAL", Build.VERSION.INCREMENTAL);
            basicInfo.put("Build.VERSION.PREVIEW_SDK_INT", Build.VERSION.PREVIEW_SDK_INT);
            basicInfo.put("Build.VERSION.RELEASE", Build.VERSION.RELEASE);
            basicInfo.put("Build.VERSION.SDK_INT", Build.VERSION.SDK_INT);
            basicInfo.put("Build.VERSION.SECURITY_PATCH", Build.VERSION.SECURITY_PATCH);

            /*--------------------UserManager--------------------*/
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            basicInfo.put("userProfiles", userManager.getUserProfiles());
            basicInfo.put("systemUser", userManager.isSystemUser());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return basicInfo;
    }
}
