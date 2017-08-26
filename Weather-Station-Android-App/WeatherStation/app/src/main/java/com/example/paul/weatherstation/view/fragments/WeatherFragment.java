package com.example.paul.weatherstation.view.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.paul.weatherstation.view.customViews.ViewPagerSnackbar;
import com.example.paul.weatherstation.helper.WeatherImageChangerHelper;
import com.example.paul.weatherstation.model.DatabaseHandler;
import com.example.paul.weatherstation.model.WeatherRecord;
import com.example.paul.weatherstation.R;
import com.example.paul.weatherstation.model.AppSettings;
import com.example.paul.weatherstation.view.activities.SettingsActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Paul on 21-Aug-17 at 5:15 PM.
 */

public class WeatherFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    MqttAndroidClient mqttAndroidClient;
    MqttConnectOptions mqttConnectOptions;
//    private final String serverUri = "tcp://m20.cloudmqtt.com:16691";
    private final String refreshTopic = "nodemcu/requests";
    private String deviceId ;
    private String temperatureTopic;
    private String humidityTopic;
    private String pressureTopic;
    private final WeatherRecord weatherRecord = new WeatherRecord();
    private Context context;
    public DatabaseHandler db;
    private AppSettings settings;
    private TextView temperatureText;
    private TextView humidityText;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.weather_fragment, container, false);
        temperatureText = (TextView) view.findViewById(R.id.temperature_value_text);
        humidityText = (TextView) view.findViewById(R.id.humidity_level_text);
        context = getActivity();
        db = new DatabaseHandler(context);
        settings = AppSettings.ReadSettings(context);
        deviceId = settings.getDeviceId();
        temperatureTopic = "nodemcu/" + deviceId + "/temperature";
        humidityTopic = "nodemcu/" + deviceId + "/humidity";
        pressureTopic = "nodemcu/" + deviceId + "/pressure";

        //Set Last Values To Views
        if(db.getLastWeatherRecord() != null) {
            temperatureText.setText(db.getLastWeatherRecord().getTemperature());
            humidityText.setText(db.getLastWeatherRecord().getHumidity());
        } else {
            temperatureText.setText("-");
            humidityText.setText("-");
        }

        //Update Images

        this.updateComfortAndWeatherImages(temperatureText.getText().toString(),humidityText.getText().toString());

        //Refresh Action

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        swipeRefreshLayout.setColorSchemeColors(
                Color.RED, Color.GREEN, Color.BLUE, Color.CYAN);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                publishRefreshMessage();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //mqtt

        this.mqttAndroidClient = this.createMqttAndroidClient();
        boolean canIConnect = true;

        try {
            this.mqttConnectOptions = createMqttConnectOptions();
        } catch (Exception e) {
            canIConnect = false;
            this.showCustomSnackbar();
        }
        if(canIConnect){
            connectToMqtt();
        }

        return view;
    }



    private MqttConnectOptions createMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(settings.getMqttUserName());
        options.setPassword(settings.getMqttPassword().toCharArray());
        return options;
    }

    private MqttAndroidClient createMqttAndroidClient() {
        String clientId = settings.getClientId();
        return new MqttAndroidClient(getContext(), settings.getMqttServerUri(),clientId);
    }

    public void subscribeToTopics(){
        try {
            mqttAndroidClient.subscribe("nodemcu/#", 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private void publishRefreshMessage(){

        try {
            mqttAndroidClient.publish(refreshTopic, getRefreshMessage());

        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private MqttMessage getRefreshMessage(){
        String payload = "NEED REFRESH!";
        byte[] encodedPayload;
        encodedPayload = payload.getBytes();
        return new MqttMessage(encodedPayload);
    }

    private boolean isWeatherRecordLoaded(WeatherRecord weatherRecord){
        return weatherRecord.getPressure() != null
                && weatherRecord.getHumidity() != null
                && weatherRecord.getTemperature() != null;
    }

    private String getCurrentTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy | HH:mm:ss");
        return sdf.format(c.getTime());
    }

    private void addToDb(){
        if(isWeatherRecordLoaded(weatherRecord)){
            this.updateComfortAndWeatherImages(temperatureText.getText().toString(),humidityText.getText().toString());
            Log.d("Insert: ", "Inserting ..");
            weatherRecord.setTime(getCurrentTime());
            db.addWeatherRecord(weatherRecord);
            weatherRecord.clear();
            addWeatherRecordsToLog();
        }
    }

    private void addWeatherRecordsToLog(){
        //Adding weatherRecords to Log
        Log.d("Reading: ", "Reading all contacts..");
        List<WeatherRecord> weatherRecordList = db.getAllWeatherRecords();

        for (WeatherRecord weatherRecord : weatherRecordList) {
            String log = "Id: " + weatherRecord.getID() + " ,Time: " + weatherRecord.getTime() + " ,Temperature: " + weatherRecord.getTemperature() + " ,Humidity:" + weatherRecord.getHumidity() + " ,Pressure:" + weatherRecord.getPressure();
            // Writing Contacts to log
            Log.d("Name: ", log);
            System.out.println("Name: " + log);
        }
    }

    private void connectToMqtt(){

        this.mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(context, "Connection lost!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic.equals(temperatureTopic)) {
                    temperatureText.setText(message.toString());
                    weatherRecord.setTemperature(message.toString());
                    addToDb();

                } else if (topic.equals(humidityTopic)) {
                    humidityText.setText(message.toString());
                    weatherRecord.setHumidity(message.toString());
                    addToDb();

                } else if (topic.equals(pressureTopic)) {
                    weatherRecord.setPressure(message.toString());
                    addToDb();

                } else {
                    //
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getContext(), "Something went wrong connecting to server.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void updateComfortAndWeatherImages(String temperature, String humidity){
        Float actualTemperature = Float.parseFloat(temperature);
        Float actualHumidity = Float.parseFloat(humidity);
        new WeatherImageChangerHelper(this.view).updateImages(Math.round(actualTemperature), Math.round(actualHumidity));
    }

    private void showCustomSnackbar(){
        CoordinatorLayout layout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);
        ViewPagerSnackbar snackbar = ViewPagerSnackbar
                .make(layout, "Please set-up your connections.", ViewPagerSnackbar.LENGTH_INDEFINITE)
                .setAction("SETTINGS", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(WeatherFragment.this.getContext(), SettingsActivity.class);
                        startActivity(intent);
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        // get snackbar view
        View snackbarView = snackbar.getView();

        // change snackbar text color
        int snackbarTextId = android.support.design.R.id.snackbar_text;
        TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
