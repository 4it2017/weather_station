package com.example.paul.weatherstation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by Paul on 05-Jul-17.
 */

public class MqttConnectionManagerService extends Service{

    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private final DatabaseHandler db = new DatabaseHandler(this);
    private final String temperatureTopic = "nodemcu/2916367/temperature";
    private final String humidityTopic = "nodemcu/2916367/humidity";
    private final String pressureTopic = "nodemcu/2916367/pressure";
    private final WeatherRecord weatherRecord = new WeatherRecord();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        options = createMqttConnectOptions();
        client = createMqttAndroidClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(isNetworkAvailable() && !client.isConnected())
            this.connect(client, options);
        return START_STICKY;
    }

    private MqttConnectOptions createMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("android");
        options.setPassword("android".toCharArray());
        return options;
    }

    private MqttAndroidClient createMqttAndroidClient() {
        String clientId = MqttClient.generateClientId();
        return new MqttAndroidClient(getApplicationContext(), "tcp://m20.cloudmqtt.com:16691",clientId);
    }

    private void connect(final MqttAndroidClient client, MqttConnectOptions options) {
        try {
            if (!client.isConnected()) {
                IMqttToken token = client.connect(options);
                //on successful connection, publish or subscribe as usual
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                            //Subscribes
                            int qos = 1;
                            try {
                                IMqttToken subToken = client.subscribe("nodemcu/#", qos);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {

                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                        // The subscription could not be performed, maybe the user was not
                                        // authorized to subscribe on the specified topic e.g. using wildcards

                                    }
                                });
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems

                    }
                });
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        Log.d("Insert: ", "Inserting ..");
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy | HH:mm:ss");
                        String strDate = sdf.format(c.getTime());

                        switch (topic) {
                            case temperatureTopic:
                                weatherRecord.setTemperature(message.toString());
                                addToDb();
                                break;
                            case humidityTopic:
                                weatherRecord.setHumidity(message.toString());
                                addToDb();
                                break;
                            case pressureTopic:
                                weatherRecord.setPressure(message.toString());
                                addToDb();
                                break;
                            default:
                                break;
                        }
                        weatherRecord.setTime(strDate);
                        db.addWeatherRecord(weatherRecord);
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private boolean isWeatherRecordLoaded(WeatherRecord weatherRecord){
        return weatherRecord.getPressure() != null
                && weatherRecord.getHumidity() != null
                && weatherRecord.getTemperature() != null;
    }

    private void addToDb(){
        if(isWeatherRecordLoaded(weatherRecord)){
            Log.d("Insert: ", "Inserting ..");
            weatherRecord.setTime(getCurrentTime());
            db.addWeatherRecord(weatherRecord);
            weatherRecord.clear();
        }
    }

    private String getCurrentTime(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy | HH:mm:ss");
        return sdf.format(c.getTime());
    }


}