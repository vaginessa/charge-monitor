package com.stonecode.chargemonitor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.ArrayList;
import java.util.HashMap;

public class BeaconDetectActivity extends AppCompatActivity implements BeaconConsumer {

    private static String TAG = "MyActivity";
    private BeaconManager mBeaconManager;
    ArrayAdapter<String> departmentAdapter;
    ListView lvDepartment;
    public HashMap<String, Region> ssnRegionMap;
    public ArrayList<String> regionNames = new ArrayList<>();
    public ArrayList<String> regions = new ArrayList<>();

    @Override
    public void onResume() {
        super.onResume();

        mBeaconManager.bind(this);
    }

    public void onBeaconServiceConnect() {

        mBeaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(TAG, "sjI detected a beacon in the region with namespace id "
                        + region.getId1() + " and instance id: " + region.getId2());
            }

            @Override
            public void didExitRegion(Region region) {

            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                String regionName = region.getUniqueId();
                String beaconSSN = region.getId2().toHexString();
                Log.d(TAG, "didDetermineStateForRegion: " + i+regionName+beaconSSN);
                switch (i) {
                    case INSIDE:
                        Log.i("TAG", "Enter " + regionName);
                        if (!regionNames.contains(regionName)) {
                            regionNames.add(regionName);
                            regions.add(beaconSSN);
                            changeData();
                        }

                        break;
                    case OUTSIDE:
                        Log.i("TAG", "Outside " + regionName);
                        if (regionNames.contains(regionName)) {
                            regionNames.remove(regionName);
                            regions.remove(beaconSSN);
                            changeData();
                        }

                        break;
                }
            }

        });
        try {
            for (String key : ssnRegionMap.keySet()) {
                Region region = ssnRegionMap.get(key);
                mBeaconManager.startMonitoringBeaconsInRegion(region);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void changeData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                departmentAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_detect);


        ssnRegionMap = new HashMap<>();


        ssnRegionMap.put("0x000000000001", new Region("Air Conditioner", Identifier.parse("0x424531b6b1bc0a2fa89e"), Identifier.parse("0x000000000001"), null));
        ssnRegionMap.put("0x111122223333", new Region("Smart Meter", Identifier.parse("0x11112222333344445555"), Identifier.parse("0x111122223333"), null));
        ssnRegionMap.put("0x000000000002", new Region("Fridge", Identifier.parse("0xb8ed0f35da2d8a65c6ef"), Identifier.parse("0x000000000002"), null));
        ssnRegionMap.put("0x000000000003", new Region("Lighting",Identifier.parse("0x9a932f9cebea0d781574"), Identifier.parse("0x000000000003"), null));
        ssnRegionMap.put("0x000000000004", new Region("Washing Machine", Identifier.parse("0xf805e1875ba2df7b1359"),Identifier.parse("0x000000000004"), null));
        ssnRegionMap.put("0x000000000005", new Region("Television", Identifier.parse("0xdcdc608daaada6179199"),  Identifier.parse("0x000000000005"), null));
        ssnRegionMap.put("0x000000000006", new Region("Heater", Identifier.parse("0x911181170acb61798c0b"),  Identifier.parse("0x000000000006"), null));
        ssnRegionMap.put("0x000000000007", new Region("Geyser", Identifier.parse("0xc662d18ebceda1eca8be"),  Identifier.parse("0x000000000007"), null));

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());

        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        new BackgroundPowerSaver(this);

        lvDepartment = (ListView) findViewById(R.id.lv_Departments);
        departmentAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_department, regionNames);
        lvDepartment.setAdapter(departmentAdapter);

            }

}
