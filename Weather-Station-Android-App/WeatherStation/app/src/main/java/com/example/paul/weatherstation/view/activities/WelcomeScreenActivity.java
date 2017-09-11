package com.example.paul.weatherstation.view.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.EditTextPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paul.weatherstation.R;
import com.example.paul.weatherstation.helper.Validator;

public class WelcomeScreenActivity extends AppCompatActivity {

    EditText deviceIdHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Show only once
        SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
        if(pref.getBoolean("activity_executed", false)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            SharedPreferences.Editor ed = pref.edit();
            ed.putBoolean("activity_executed", true);
            ed.apply();
        }

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_welcome_screen);
        TextView welcomeTextView = (TextView) findViewById(R.id.welcome_text_view);
        Typeface roboto = Typeface.createFromAsset(this.getAssets(),
                "font/Roboto-Bold.ttf");
        welcomeTextView.setTypeface(roboto);



        deviceIdHolder = (EditText) findViewById(R.id.device_id_edit_text_view);

    }

    public void saveDeviceID(View view){
        String deviceID = deviceIdHolder.getText().toString();
        if(Validator.isDeviceIDValid(deviceID)){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else{
            Toast.makeText(this, "Something is wrong with that ID.. please check it again.", Toast.LENGTH_SHORT).show();
        }
    }
}
