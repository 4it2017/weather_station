package com.example.paul.weatherstation.View;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paul.weatherstation.R;

public class NoInternetConnection extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet_connection);

        Button button= (Button) findViewById(R.id.angry_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recheckNetworkConnection();
            }
        });
    }

    private void recheckNetworkConnection(){
        if(isOnline()){
            finish();
        } else {
            Toast.makeText(this, "Sorry, still no network connection..", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        return !(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable());
    }
}
