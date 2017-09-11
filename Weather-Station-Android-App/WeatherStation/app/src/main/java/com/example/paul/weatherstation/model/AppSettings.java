package com.example.paul.weatherstation.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;

import com.example.paul.weatherstation.R;

/**
 * Created by Paul on 23-Aug-17 at 4:56 PM.
 */
public class AppSettings {

    private String _mqttServerUri;
    private String _mqttUserName;
    private String _mqttPassword;
    private String _deviceId;

    public static AppSettings ReadSettings(Context context) {

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        AppSettings sett =  new AppSettings();
        sett._mqttServerUri = SP.getString("mqtt_server_uri", "");
        sett._mqttUserName =  SP.getString("mqtt_username", "");
        sett._mqttPassword =  SP.getString("mqtt_password", "");
        sett._deviceId =  SP.getString("device_id", "");

        return sett;
    }

    public String getMqttServerUri() {
        return _mqttServerUri;
    }

    public String getMqttUserName() {
        return _mqttUserName;
    }


    public String getMqttPassword() {
        return _mqttPassword;
    }

    public String getSubscriptionTopic() {
        return  "nodemcu/"+ _deviceId ;
    }

    public String getDeviceId(){
        return this._deviceId;
    }

    public String getClientId (){
        return "android" + _deviceId;
    }

}
