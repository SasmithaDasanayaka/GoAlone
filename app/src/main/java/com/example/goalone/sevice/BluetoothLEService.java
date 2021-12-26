package com.example.goalone.sevice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.example.goalone.MainActivity;
import com.example.goalone.Model.Device;
import com.example.goalone.R;
import com.example.goalone.VerificationActivity;
import com.example.goalone.fragment.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class BluetoothLEService {
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    Intent enableBleIntent;
    private final int REQUEST_ENABLE_BTLE = 1;
    final int RECYCLE_DEVICE_TIMEOUT = 30000;


    private final static String default_notification_channel_id = "default";
    private final static String channelDescription = "default";
    private final static String channelName = "default";

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

    public BluetoothLEService(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

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

        FirebaseUser user = VerificationActivity.mAuth.getCurrentUser();

        String phoneNumber = user.getPhoneNumber();

        advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addServiceData(pUuid, Objects.requireNonNull(phoneNumber.substring(3)).getBytes(Charset.forName("UTF-8")))
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

                    ScanRecord scanRecord = result.getScanRecord();
                    String user = "UnKnown";

                    if (scanRecord != null) {
                        for (Map.Entry<ParcelUuid, byte[]> entry : scanRecord.getServiceData().entrySet()) {
                            System.out.println(entry.getKey().toString() + " " + new String(entry.getValue(), Charset.forName("UTF-8")));
                            if (entry.getKey().toString().equals(mainActivity.getString(R.string.device_uuid))) {
                                user = "+94" + new String(entry.getValue(), Charset.forName("UTF-8"));
                                System.out.println("we received:" + user);
                            }
                        }
                    }

                    System.out.println("############ found " + result.getDevice().getAddress() + " " + result.getDevice().getName());
                    addDevice(new Device(
                            user,
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
                                System.out.println("############ stopping");
                            }
                        });

                        bluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertisingCallback);
                        serviceHandler.postDelayed(scanRunnable, TOGGLE_TIMEOUT);
                    }
                };
                if (isAdvertiseAble) serviceHandler.postDelayed(advertiseRunnable, TOGGLE_TIMEOUT);

            }
        };
        serviceHandler.post(scanRunnable);
        serviceHandler.postDelayed(garbageRunnable,5000);

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
        serviceHandler.removeCallbacks(garbageRunnable);
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
        boolean d1 = false;
        if (device.getUser() != null) {
            d1 = true;
        }
        for (Device device1 : mainActivity.getHomeFragment().getDevices()) {
            boolean d2 = false;
            if (device1.getUser() != null) {
                d2 = true;
            }
            if (d1 && d2) {
                if (device1.getUser().equals(device.getUser())) {
                    in = true;
                    inDevice = device1;
                }
            } else {
                if (device1.getMacAddress().equals(device.getMacAddress())) {
                    in = true;
                    inDevice = device1;
                }
            }
        }
        if (!in) {
            if (SettingsFragment.vibrateOnOff) {
                System.out.println("###################################### Vibrating...");
                final Vibrator vibrator = (Vibrator) mainActivity.getSystemService(Context.VIBRATOR_SERVICE);
                final VibrationEffect vibrationEffect;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrationEffect = VibrationEffect.createOneShot(1000, 150);

                    vibrator.cancel();
                    vibrator.vibrate(vibrationEffect);

                }
            }
            if (SettingsFragment.ringingOnOff) {
                System.out.println("###################################### Ringing...");
                Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(mainActivity.getApplicationContext(), notificationSound);
                mp.start();
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(mainActivity.getApplicationContext(), default_notification_channel_id)
                                .setSmallIcon(R.drawable.high)
                                .setContentTitle("Warning!")
                                .setContentText("Keep the distance")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManager mNotificationManager = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);

                mNotificationManager.notify((int) System.currentTimeMillis(),
                        mBuilder.build());
            }
            if (SettingsFragment.notificationOnOff) {
                System.out.println("###################################### Notification sent...");

                createNotificationChannel();

//                Intent intent = new Intent(mainActivity, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                PendingIntent pendingIntent = PendingIntent.getActivity(mainActivity, 0, intent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(mainActivity, default_notification_channel_id)
                        .setSmallIcon(R.drawable.high)
                        .setContentTitle("Warning!")
                        .setContentText("Keep the distance")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        // Set the intent that will fire when the user taps the notification
//                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mainActivity);

                //setTap();
                notificationManager.notify(1, builder.build());
            }
            device.addRssi(rssi);
            device.setAverageDistance(calculateAverageDistance(device));

            if (device.getUser() != "UnKnown") {
                FirebaseDatabase d = FirebaseDatabase.getInstance();
                d.getReference().child("users").child(device.getUser()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            device.setuName(Objects.requireNonNull(task.getResult()).getValue(String.class));
                            mainActivity.getHomeFragment().updateDevices();
                        }
                    }
                });
            }

            mainActivity.getHomeFragment().addDevice(device);
        } else {
            inDevice.addRssi(rssi);
            inDevice.setAverageDistance(calculateAverageDistance(inDevice));
            inDevice.setLastIdentifiedTime(System.currentTimeMillis());
            mainActivity.getHomeFragment().updateDevices();
        }

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channelName;
            String description = channelDescription;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(default_notification_channel_id, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = mainActivity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    Runnable garbageRunnable = new Runnable() {
        @Override
        public void run() {
            Iterator<Device> itr = mainActivity.getHomeFragment().getDevices().iterator();
            while (itr.hasNext()) {
                Device d = itr.next();
                if (System.currentTimeMillis() - d.getLastIdentifiedTime() > RECYCLE_DEVICE_TIMEOUT) {
                    itr.remove();
                    clearDevice(d);
                }
            }
            serviceHandler.postDelayed(this, 5000);

        }
    };

    public void clearDevice(Device device) {
        FirebaseDatabase d = FirebaseDatabase.getInstance();
        d.getReference().child("recent").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())).push().setValue(device);
        mainActivity.getHomeFragment().updateDevices();
    }


//    private void setTap(){
//        Intent intent = new Intent(BluetoothLEService.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_channel_id)
//                .setSmallIcon(R.drawable.high)
//                .setContentTitle( "Warning!" )
//                .setContentText( "Keep the distance" )
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                // Set the intent that will fire when the user taps the notification
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//    }


}
