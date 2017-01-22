package com.stonecode.chargemonitor;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class BeaconDetectActivity extends AppCompatActivity implements BeaconConsumer {

    private final Handler mHandler = new Handler();
    private Runnable t1;

    private static String TAG = "MyActivity";
    private BeaconManager mBeaconManager;
    ArrayAdapter<String> departmentAdapter;
    ListView lvDepartment;
    public HashMap<String, Region> ssnRegionMap;
    public ArrayList<String> regionNames = new ArrayList<>();
    public ArrayList<String> regions = new ArrayList<>();

    String acBeacon = "0";
    String fridgeBeacon = "0";
    String tvBeacon = "0";
    String heaterBeacon = "0";
    String wmBeacon = "0";
    String lightBeacon = "0";
    String smartMeterBeacon = "0";

    ParseUser currUser;

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

            long recordTime[] = new long[8];

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                String regionName = region.getUniqueId();
                String beaconSSN = region.getId2().toHexString();
                int x = returnId(regionName);

                Log.d(TAG, "didDetermineStateForRegion: " + i + regionName + beaconSSN);
                switch (i) {
                    case INSIDE:
                        Log.i("TAG", "Enter " + regionName);
                        if (!regionNames.contains(regionName)) {
                            regionNames.add(regionName);
                            regions.add(beaconSSN);
                            changeData();

                            Log.d(TAG, "!region");

                            Calendar c = Calendar.getInstance();
                            long currTime = c.getTimeInMillis();

                            BeaconToParse data = new BeaconToParse(x, 1, String.valueOf(currTime - recordTime[x]));
                            recordTime[x] = currTime;
                            data.sendToParse();
                        }

                        break;
                    case OUTSIDE:
                        Log.i("TAG", "Outside " + regionName);
                        if (regionNames.contains(regionName)) {
                            regionNames.remove(regionName);
                            regions.remove(beaconSSN);
                            changeData();
                        }

                        Log.d(TAG, "X:::" + x);
                        BeaconToParse data = new BeaconToParse(x, 0, "0");
                        recordTime[x] = 0;
                        data.sendToParse();

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

    public int returnId(String appl) {

        int i = 1;
        if (appl.equals("Air Conditioner")) {
            i = 1;
        }

        if (appl.equals("Smart Meter")) {
            i = 2;
        }

        if (appl.equals("Fridge")) {
            i = 3;
        }

        if (appl.equals("Lighting")) {
            i = 4;
        }

        if (appl.equals("Washing Machine")) {
            i = 5;
        }

        if (appl.equals("Television")) {
            i = 6;
        }

        if (appl.equals("Heater")) {
            i = 7;
        }

        return i;
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

        new getBeaconStatus().execute();
        ssnRegionMap.put("0x000000000001", new Region("Air Conditioner", Identifier.parse("0x424531b6b1bc0a2fa89e"), Identifier.parse("0x000000000001"), null));
        ssnRegionMap.put("0x111122223333", new Region("Smart Meter", Identifier.parse("0x11112222333344445555"), Identifier.parse("0x111122223333"), null));
        ssnRegionMap.put("0x000000000002", new Region("Fridge", Identifier.parse("0xb8ed0f35da2d8a65c6ef"), Identifier.parse("0x000000000002"), null));
        ssnRegionMap.put("0x000000000003", new Region("Lighting", Identifier.parse("0x9a932f9cebea0d781574"), Identifier.parse("0x000000000003"), null));
        ssnRegionMap.put("0x000000000004", new Region("Washing Machine", Identifier.parse("0xf805e1875ba2df7b1359"), Identifier.parse("0x000000000004"), null));
        ssnRegionMap.put("0x000000000005", new Region("Television", Identifier.parse("0xdcdc608daaada6179199"), Identifier.parse("0x000000000005"), null));
        ssnRegionMap.put("0x000000000006", new Region("Heater", Identifier.parse("0x911181170acb61798c0b"), Identifier.parse("0x000000000006"), null));
        ssnRegionMap.put("0x000000000007", new Region("Geyser", Identifier.parse("0xc662d18ebceda1eca8be"), Identifier.parse("0x000000000007"), null));

        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());

        mBeaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        new BackgroundPowerSaver(this);

        Calendar c = Calendar.getInstance();
        Log.d(TAG, "currTime:  " + c.getTime());

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        Log.d(TAG, "formattedDate: " + formattedDate);
        lvDepartment = (ListView) findViewById(R.id.lv_Departments);
        departmentAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_department, regionNames) {
            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
//                Log.d(TAG,"getViewENterd");
                return super.getView(position, convertView, parent);
            }
        };

        lvDepartment.setAdapter(departmentAdapter);
        t1 = new Runnable() {
            @Override
            public void run() {
                new getBeaconStatus().execute();
                mHandler.postDelayed(t1, 30 * 1000);
            }
        };
        mHandler.post(t1);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mBeaconManager.unbind(this);
//    }


    public class getBeaconStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            currUser = ParseUser.getCurrentUser();
            try {
                currUser = currUser.fetch();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            acBeacon = currUser.getString("BtnAirconditioner");
            fridgeBeacon = currUser.getString("BtnFridge");
            lightBeacon = currUser.getString("BtnWashingMachine");
            heaterBeacon = currUser.getString("BtnHeater");
            tvBeacon = currUser.getString("BtnTV");
            wmBeacon = currUser.getString("BtnWashingMachine");
            smartMeterBeacon = currUser.getString("BtnSmartMeter");
            Log.d(TAG, "backC: " + acBeacon + "\n" + fridgeBeacon);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // SUSHANT SHITS HERE (EVERYDAY) along with his mozo guys

            ArrayList<String> regionNameCopy = new ArrayList<>();

            for (String item : regionNames) {
                switch (item) {
                    case "Air Conditioner":
                        if (acBeacon.equals("1")) regionNameCopy.add(item + " is on");
                        else regionNameCopy.add(item + " is off");
                        break;
                    case "Smart Meter":
                        if (smartMeterBeacon.equals("1")) regionNameCopy.add(item + " is on");
                        else regionNameCopy.add(item + " is off");
                        break;
                    case "Fridge":
                        if (fridgeBeacon.equals("1")) regionNameCopy.add(item + " is on");
                        else regionNameCopy.add(item + " is off");
                        break;
                    case "Lighting":
                        if (lightBeacon.equals("1")) regionNameCopy.add(item + " is on");
                        else regionNameCopy.add(item + " is off");
                        break;
                    case "Washing Machine":
                        if (wmBeacon.equals("1")) regionNameCopy.add(item + " is on");
                        else regionNameCopy.add(item + " is off");
                        break;
                    case "Television":
                        if (tvBeacon.equals("1")) regionNameCopy.add(item + " is on");
                        else regionNameCopy.add(item + " is off");
                        break;
                    case "Heater":
                        if (heaterBeacon.equals("1")) regionNameCopy.add(item + " is on");
                        else regionNameCopy.add(item + " is off");
                        break;

                }
            }
            departmentAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item_department, regionNameCopy) {
                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    Log.d(TAG, "getViewENterd");

//                    for(int i=0;i<regionNames.size();i++)
//                    {
//                        if (regionNames.get(position).equals("Air Conditioner") && acBeacon.equals("0")) {
//                            Log.d(TAG, "ac is offf");
//                            regionNames.remove(position);
//                            regionNames.add("Air Conditioner is off");
//                            departmentAdapter.notifyDataSetChanged();
////                            TextView tt = (TextView) convertView.findViewById(R.id.txt1);
////                            tt.setTextColor(Color.RED);
//                        } else if (regionNames.get(position).equals("Air Conditioner") && acBeacon.equals("1")) {
//                            regionNames.remove(position);
//                            regionNames.add("Air Conditioner is ON");
//                            departmentAdapter.notifyDataSetChanged();
//
////                            TextView tt = (TextView) convertView.findViewById(R.id.txt1);
////                            tt.setTextColor(Color.BLUE);
//                        }
//                    }

                    return super.getView(position, convertView, parent);
                }
            };

            lvDepartment.setAdapter(departmentAdapter);
//           changeData();
        }
    }
}
