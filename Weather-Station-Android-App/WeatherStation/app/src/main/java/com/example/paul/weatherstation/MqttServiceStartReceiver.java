package com.example.paul.weatherstation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Paul on 05-Jul-17.
 */

public class MqttServiceStartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MqttConnectionManagerService.class));
    }

}