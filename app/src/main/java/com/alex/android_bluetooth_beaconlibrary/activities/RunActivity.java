package com.alex.android_bluetooth_beaconlibrary.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alex.android_bluetooth_beaconlibrary.R;
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

public class RunActivity extends AppCompatActivity implements BeaconConsumer {

    protected static final String TAG = "RunActivity";

    private BeaconManager beaconManager;
    private int nbBeacon;
    private LinearLayout listInfos;
    private TextView tvBeaconInside;
    private Region regionStart;
    private Button btnReset;
    private long startChrono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);

        nbBeacon = 0;

        listInfos = findViewById(R.id.linearLayout);
        addText("Find a beacon to start …");
        tvBeaconInside = findViewById(R.id.beaconInside);

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RunActivity.this, RunActivity.class));
                finish();
            }
        });

        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    public void addText(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        listInfos.addView(tv);
    }

    public void switchBeaconInside(String s) {
        tvBeaconInside.setText(s);
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
                        if (nbBeacon == 0) {
                            addText("Ready when you are");
                            regionStart = region;
                        } else {

                            if (region.getUniqueId().equals(regionStart.getUniqueId()))
                                return;

                            addText("Good job !");
                            printResult();
                        }
                    }
                }));
            }

            @Override
            public void didExitRegion(Region region) {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (nbBeacon == 0) {
                            addText("RUN !");
                            startChrono = SystemClock.elapsedRealtime();
                            nbBeacon++;
                        }
                    }
                }));

            }

            @Override
            public void didDetermineStateForRegion(final int state, final Region region) {
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (state == MonitorNotifier.INSIDE) {
                            RunActivity.this.switchBeaconInside("Beacon inside : " + region.getUniqueId());
                        } else if (state == MonitorNotifier.OUTSIDE) {
                            RunActivity.this.switchBeaconInside("No beacon available");
                        } else {
                            RunActivity.this.switchBeaconInside("Unknown state : " + state);
                        }
                    }
                }));
            }
        });

        try

        {
            Identifier uuid = Identifier.parse("e2c56db5-dffb-48d2-b060d0f5a71096e0");
            Identifier major = Identifier.parse("0");

            Region region1 = new Region("Beacon2", uuid, major, Identifier.parse("2"));
            Region region2 = new Region("Beacon3", uuid, major, Identifier.parse("3"));

            beaconManager.startMonitoringBeaconsInRegion(region1);
            beaconManager.startMonitoringBeaconsInRegion(region2);

        } catch (
                RemoteException e)

        {
            e.printStackTrace();
        }
    }

    private void printResult() {
        final long timeElapsed = SystemClock.elapsedRealtime() - startChrono;
        addText("Your time : " + ((double) timeElapsed / 1000) + "s");

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("message");
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Long timeFirebase = dataSnapshot.getValue(Long.class);
                if (timeFirebase == null) {
                    Toast.makeText(RunActivity.this, "Problem reading timeFirebase", Toast.LENGTH_LONG).show();
                    return;
                }
                if (timeElapsed < timeFirebase) {
                    addText("Congratulation you beat the high score that was " + ((double) timeFirebase / 1000) + "!!!");
                    FirebaseDatabase.getInstance().getReference("message").setValue(timeElapsed);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(RunActivity.this, "Failed reading Firebase.", Toast.LENGTH_LONG).show();
            }
        });
    }
}
