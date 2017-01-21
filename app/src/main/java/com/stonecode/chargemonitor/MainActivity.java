package com.stonecode.chargemonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private BeaconManager beaconManager;
    private static final String TAG = "ScanningActivity";
    private HashMap<String,String> beaconNameIdHM=new HashMap<>();
    private HashMap<String,String> beaconNameSpaceIdHM=new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconNameIdHM.put("0xabcd1234abcd","power meter");
        beaconNameIdHM.put("0x000000000001","AC");
        beaconNameIdHM.put("0x000000000002","Lighting");
        beaconNameIdHM.put("0x000000000003","Appliance");

      //  beaconNameSpaceIdHM.put("0xabcd1234abcd","abcd1234abcd1234abcd");
        beaconNameSpaceIdHM.put("0x000000000001","0x424531b6b1bc0a2fa89e");
        beaconNameSpaceIdHM.put("0x000000000002","0xb8ed0f35da2d8a65c6ef");
//        beaconNameSpaceIdHM.put("0x000000000003","CCCC0003CCCC0003CCCC");

        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
//        Identifier myBeaconNamespaceId = Identifier.parse("0x424531b6b1bc0a2fa89e");
//        Identifier myBeaconInstanceId = Identifier.parse("0x000000000001");
        for (Map.Entry<String, String> entry : beaconNameSpaceIdHM.entrySet()) {
            String instanceId=entry.getKey();
            String nameSpaceId=entry.getValue();
            String name=beaconNameIdHM.get(instanceId);

            Identifier myBeaconNamespaceId = Identifier.parse(nameSpaceId);
            Identifier myBeaconInstanceId = Identifier.parse(instanceId);

            Region region = new Region("sj", myBeaconNamespaceId, myBeaconInstanceId, null);

            Log.d(TAG, "sjonBeaconServiceConnect: "+region.toString());

            beaconManager.addMonitorNotifier(new MonitorNotifier() {
                @Override
                public void didEnterRegion(Region region) {
                    Log.d(TAG, "sjI detected a beacon in the region with namespace id "
                            + region.getId1() + " and instance id: " + region.getId2());
                }

                @Override
                public void didExitRegion(Region region) {
                    Log.d(TAG, "sjI lost a beacon in the region with namespace id "
                            + region.getId1() + " and instance id: " + region.getId2());
                }

                @Override
                public void didDetermineStateForRegion(int i, Region region) {
                    Log.d(TAG, "didDetermineStateForRegion: "+i+region.getId2());
                }
            });
            try {
                beaconManager.startMonitoringBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            beaconManager.addRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    if (beacons.size() > 0) {
                        Log.i(TAG, "sjThe first beacon I see is about " + beacons.iterator().next().getDistance() + " meters away."+beacons.iterator().next().getId2());
                    }
                }
            });
//            try {
//                beaconManager.startRangingBeaconsInRegion(region);
//            } catch (RemoteException e) {
//            }
        }
    }
}

