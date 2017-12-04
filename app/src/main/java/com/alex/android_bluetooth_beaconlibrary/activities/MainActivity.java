package com.alex.android_bluetooth_beaconlibrary.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alex.android_bluetooth_beaconlibrary.R;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String TAG = "MonitoringActivity";

    private BeaconManager beaconManager;
    private static final int PERM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION}, PERM);
        } else {
            beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
            beaconManager.bind(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(final Region region) {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout linearLayout = MainActivity.this.findViewById(R.id.linearLayout);
                        TextView tv = new TextView(MainActivity.this);
                        tv.setText(region.toString());
                        linearLayout.addView(tv);
                    }
                }));
                Log.i(TAG, "##### I just saw an beacon for the first time!");
                Toast.makeText(MainActivity.this, "I just saw an beacon for the first time!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void didExitRegion(Region region) {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout linearLayout = MainActivity.this.findViewById(R.id.linearLayout);
                        TextView tv = new TextView(MainActivity.this);
                        tv.setText("Bye bye beacon");
                        linearLayout.addView(tv);
                    }
                }));
                Log.i(TAG, "##### I no longer see an beacon");
                Toast.makeText(MainActivity.this, "I no longer see an beacon", Toast.LENGTH_LONG).show();
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "##### I have just switched from seeing/not seeing beacons: " + state);
                Toast.makeText(MainActivity.this, "I have just switched from seeing/not seeing beacons: " + state, Toast.LENGTH_LONG).show();
            }
        });

        try {
            Identifier uuid = Identifier.parse("e2c56db5-dffb-48d2-b060d0f5a71096e0");
            Identifier major = Identifier.parse("0");

            Region region = new Region("Beacon3", uuid, major, Identifier.parse("2"));

            beaconManager.startMonitoringBeaconsInRegion(region);

            Toast.makeText(MainActivity.this, "Chocapic : " + region, Toast.LENGTH_LONG).show();
        } catch (RemoteException e) {
        }
    }
}
