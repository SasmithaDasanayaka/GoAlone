package com.example.goalone.sevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import com.example.goalone.MainActivity;
import com.example.goalone.R;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class BluetoothLEService {
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    Intent enableBleIntent;
    private final int REQUEST_ENABLE_BTLE = 1;

    Runnable scanRunnable;
    Runnable advertiseRunnable;
    Handler serviceHandler = new Handler();

    BluetoothLeAdvertiser bluetoothLeAdvertiser;
    boolean isAdvertiseAble = true;
    AdvertiseData advertiseData;
    AdvertiseSettings advertiseSettings;
    final int TOGGLE_TIMEOUT = 10000;

    public void initBluetoothLEService(MainActivity context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        isAdvertiseAble = bluetoothAdapter.isMultipleAdvertisementSupported();

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
            System.out.println(entry.getKey().toString()+" "+ Arrays.toString(entry.getValue()));
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
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(isAdvertiseAble) bluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
                        bluetoothLeScanner.startScan(leScanCallback);
                        System.out.println("############ searching");
                        advertiseRunnable = new Runnable() {
                            @Override
                            public void run() {
                                bluetoothLeScanner.stopScan(leScanCallback);
                                if(isAdvertiseAble) bluetoothLeAdvertiser.startAdvertising(advertiseSettings,advertiseData,advertisingCallback);

                            }
                        };
                    }
                });
                if(isAdvertiseAble) serviceHandler.postDelayed(advertiseRunnable, TOGGLE_TIMEOUT);

            }
        };
        serviceHandler.post(scanRunnable);
    }

    public void stopScanLeDevice() {
        serviceHandler.removeCallbacks(scanRunnable);
        serviceHandler.removeCallbacks(advertiseRunnable);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(leScanCallback);
                if(isAdvertiseAble) bluetoothLeAdvertiser.stopAdvertising(advertisingCallback);
                System.out.println("############ stopping");
            }
        });
    }
}
