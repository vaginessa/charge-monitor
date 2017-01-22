package com.stonecode.chargemonitor;

import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import static android.content.ContentValues.TAG;

/**
 * Created by dhruv on 21/1/17.
 */

public class BeaconToParse {

    int appliance_id;
    int deviceStatus;
    String timeStatus;
    String appliance;

    // send the appliance id according to below

    public BeaconToParse(int appliance_id, int deviceStatus, String timeStatus) {
        this.appliance_id = appliance_id;
        this.timeStatus = timeStatus;
        this.deviceStatus = deviceStatus;
//        sendToParse();
    }

    public void sendToParse() {

        final ParseUser currentUser = ParseUser.getCurrentUser();
        switch (appliance_id) {
            case 1:
                appliance = "Airconditioner";
                break;
            case 2:
                appliance = "SmartMeter";
                break;
            case 3:
                appliance = "Fridge";
                break;
            case 4:
                appliance = "Lighting";
                break;
            case 5:
                appliance = "WashingMachine";
                break;
            case 6:
                appliance = "TV";
                break;
            case 7:
                appliance = "Heater";
                break;
        }

        Log.d("beasonToPrse", appliance);
        Log.d("beacon", currentUser.toString());

        currentUser.put(appliance, String.valueOf(deviceStatus));
        currentUser.put("deviceStatus", String.valueOf(deviceStatus));
        currentUser.put("timeStatus", timeStatus);

        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
//                    Log.d("errorin",e.printStackTrace());
                }
//                Log.d(TAG, "e::: " + e);
//                Log.d(TAG,"error:"+currentUser.get("Fridge").toString());
            }
        });
    }
}