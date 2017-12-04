package com.alex.android_bluetooth_beaconlibrary.activities;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alex.android_bluetooth_beaconlibrary.R;
import com.alex.android_bluetooth_beaconlibrary.models.Data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.Objects;

public class RunActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String TAG = "RunActivity";

    private BeaconManager beaconManager;
    private int nbBeacon;
    private Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        chronometer = RunActivity.this.findViewById(R.id.chrono);
        nbBeacon = 0;

        LinearLayout linearLayout = RunActivity.this.findViewById(R.id.linearLayout);
        TextView tv = new TextView(RunActivity.this);
        tv.setText(R.string.start);
        linearLayout.addView(tv);

        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
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
                        LinearLayout linearLayout = RunActivity.this.findViewById(R.id.linearLayout);
                        TextView tv = new TextView(RunActivity.this);
                        if (nbBeacon == 0) {
                            tv.setText(R.string.ready);
                        } else {
                            chronometer.stop();
                            String result = "Good job !";
                            tv.setText(result);
                            printResult();
                        }
                        linearLayout.addView(tv);
                    }
                }));
            }

            @Override
            public void didExitRegion(Region region) {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout linearLayout = RunActivity.this.findViewById(R.id.linearLayout);
                        TextView tv = new TextView(RunActivity.this);
                        if (nbBeacon == 0) {
                            tv.setText(R.string.run);
                            chronometer.setFormat("Time : %s");
                            chronometer.start();
                            nbBeacon++;
                        }
                        linearLayout.addView(tv);
                    }
                }));
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "##### I have just switched from seeing/not seeing beacons: " + state);
                Toast.makeText(RunActivity.this, "I have just switched from seeing/not seeing beacons: " + state, Toast.LENGTH_SHORT).show();
            }
        });

        try {
            Identifier uuid = Identifier.parse("e2c56db5-dffb-48d2-b060d0f5a71096e0");
            Identifier major = Identifier.parse("0");

            Region region1 = new Region("Beacon2", uuid, major, Identifier.parse("2"));
            Region region2 = new Region("Beacon3", uuid, major, Identifier.parse("3"));

            beaconManager.startMonitoringBeaconsInRegion(region1);
            beaconManager.startMonitoringBeaconsInRegion(region2);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void printResult() {
        final long time = SystemClock.elapsedRealtime() - chronometer.getBase();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("message");
        database.setValue(time);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Data value = dataSnapshot.getValue(Data.class);

                Toast.makeText(RunActivity.this, "Value : " + String.valueOf(value), Toast.LENGTH_SHORT).show();

                long time2 = Long.valueOf(value.getMessage());

                Toast.makeText(RunActivity.this, "Time Firebase : " + time2, Toast.LENGTH_SHORT).show();

                if (time < time2) {
                    runOnUiThread(new Thread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout linearLayout = RunActivity.this.findViewById(R.id.linearLayout);
                            TextView tv = new TextView(RunActivity.this);
                            tv.setTextSize(25);
                            tv.setText("Congratulation you beat the high score !!!");
                            linearLayout.addView(tv);
                        }
                    }));
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }
}
