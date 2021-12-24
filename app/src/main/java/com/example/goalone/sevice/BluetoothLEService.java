package com.example.goalone.sevice;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.goalone.MainActivity;
import com.example.goalone.Model.Device;
import com.example.goalone.R;
import com.example.goalone.VerificationActivity;
import com.example.goalone.fragment.SettingsFragment;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;


public class BluetoothLEService {
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    Intent enableBleIntent;
    private final int REQUEST_ENABLE_BTLE = 1;

    private final static String default_notification_channel_id = "default";

    Runnable scanRunnable;
    Runnable advertiseRunnable;
    Handler serviceHandler = new Handler();

    BluetoothLeAdvertiser bluetoothLeAdvertiser;
    boolean isAdvertiseAble = true;
    AdvertiseData advertiseData;
    AdvertiseSettings advertiseSettings;
    MainActivity mainActivity;

    final int TOGGLE_TIMEOUT = 10000;
    final int MEASURED_POWER = -69;
    final int N = 2;
    final int AVERAGE_COUNT = 3;

    public void initBluetoothLEService(MainActivity context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        isAdvertiseAble = bluetoothAdapter.isMultipleAdvertisementSupported();
        mainActivity = context;

        if (!bluetoothAdapter.isEnabled()) {
            enableBleIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivityForResult(enableBleIntent, REQUEST_ENABLE_BTLE);
        }

        advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString(context.getString(R.string.device_uuid)));
        System.out.println("**************** " + pUuid.toString());

        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceData(pUuid, "1234".getBytes(Charset.forName("UTF-8")))
                .build();

        for (Map.Entry<ParcelUuid, byte[]> entry : advertiseData.getServiceData().entrySet()) {
            System.out.println(entry.getKey().toString() + " " + Arrays.toString(entry.getValue()));
        }
        System.out.println(advertiseData.toString());
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    System.out.println("############ found " + result.getDevice().getAddress() + " " + result.getDevice().getName());
                    addDevice(new Device(
                            result.getDevice().getAddress(),
                            System.currentTimeMillis(),
                            Device.Threat.LEVEL3
                    ), result.getRssi());
                }
            };

    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e("BLE", "Advertising onStartFailure: " + errorCode);
            super.onStartFailure(errorCode);
        }
    };

    public void startScanLeDevice() {
        scanRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdvertiseAble) bluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothLeScanner.startScan(leScanCallback);
                        System.out.println("############ searching");
                    }
                });
                advertiseRunnable = new Runnable() {
                    @Override
                    public void run() {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                bluetoothLeScanner.stopScan(leScanCallback);
                                if (isAdvertiseAble)
                                    bluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
                                System.out.println("############ stopping");
                            }
                        });
                        if (isAdvertiseAble)
                            bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertisingCallback);
                        serviceHandler.postDelayed(scanRunnable, TOGGLE_TIMEOUT);
                    }
                };
                if (isAdvertiseAble) serviceHandler.postDelayed(advertiseRunnable, TOGGLE_TIMEOUT);

            }
        };
        serviceHandler.post(scanRunnable);
    }

    public void stopScanLeDevice() {
        serviceHandler.removeCallbacks(scanRunnable);
        serviceHandler.removeCallbacks(advertiseRunnable);
        if (isAdvertiseAble) bluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(leScanCallback);
                System.out.println("############ stopping");
            }
        });
    }

    public double calculateAverageDistance(Device device) {
        double sum = 0;
        int size = device.getRssis().size();
        for (int rssi :
                device.getRssis().subList(Math.max(size - AVERAGE_COUNT, 0), size - 1)) {
            sum += rssi;
        }
        return calculateDistance(sum / AVERAGE_COUNT);
    }

    public double calculateDistance(double rssi) {
        return (double) Math.round(Math.pow(10, ((double) (MEASURED_POWER - rssi) / (10 * N))) * 100) / 100;
    }

    public void addDevice(Device device, int rssi) {
        boolean in = false;
        Device inDevice = null;
        for (Device device1 : mainActivity.getHomeFragment().getDevices()) {
            if (device1.getMacAddress().equals(device.getMacAddress())) {
                in = true;
                inDevice = device1;
            }
        }
        if (!in) {
            if(SettingsFragment.notificationOnOff){
                Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(mainActivity.getApplicationContext(), notificationSound);
                mp.start();
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(mainActivity.getApplicationContext(), default_notification_channel_id)
                                .setSmallIcon(R.drawable. ic_launcher_foreground )
                                .setContentTitle( "Test" )
                                .setContentText( "Hello! This is my first push notification" );
                NotificationManager mNotificationManager = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(( int ) System. currentTimeMillis () ,
                        mBuilder.build());
            }
            device.addRssi(rssi);
            device.setAverageDistance(calculateAverageDistance(device));
            mainActivity.getHomeFragment().addDevice(device);
        } else {
            inDevice.addRssi(rssi);
            inDevice.setAverageDistance(calculateAverageDistance(inDevice));
            mainActivity.getHomeFragment().updateDevices();
        }

    }
}
