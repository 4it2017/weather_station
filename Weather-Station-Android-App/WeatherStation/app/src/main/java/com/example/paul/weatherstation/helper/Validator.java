package com.example.paul.weatherstation.helper;

/**
 * Created by Paul on 11-Sep-17 at 5:00 PM.
 */

public class Validator {

    public static boolean isDeviceIDValid(String deviceID){
        return deviceID.matches("[0-9]+");
    }
}
