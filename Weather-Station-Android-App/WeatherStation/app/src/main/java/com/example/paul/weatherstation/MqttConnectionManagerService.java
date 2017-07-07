package com.example.paul.weatherstation;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.TextView;
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

import java.io.UnsupportedEncodingException;

import static android.R.id.message;

/**
 * Created by Paul on 05-Jul-17.
 */

public class MqttConnectionManagerService extends Service{

    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private static Activity activity;

    protected void initialize(Activity activity){
        MqttConnectionManagerService.activity = activity;
    }

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
        boolean needRefresh =(boolean) intent.getExtras().get("NEED_REFRESH");
        this.connect(client, options, needRefresh);
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

    public void connect(final MqttAndroidClient client, MqttConnectOptions options, final boolean needRefresh) {

        try {
            if (!client.isConnected()) {
                IMqttToken token = client.connect(options);
                //on successful connection, publish or subscribe as usual
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Toast.makeText(getApplicationContext(), "Connected successfully", Toast.LENGTH_SHORT).show();

                            //Subscribes
                            int qos = 1;
                            try {
                                IMqttToken subToken = client.subscribe("nodemcu/#", qos);
                                subToken.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        Toast.makeText(getApplicationContext(), "Subscribed successfully", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken,
                                                          Throwable exception) {
                                        // The subscription could not be performed, maybe the user was not
                                        // authorized to subscribe on the specified topic e.g. using wildcards
                                        Toast.makeText(getApplicationContext(), "Subscribed failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Toast.makeText(getApplicationContext(), "Connection failed, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {

                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        if(topic.equals("nodemcu/temperature")){
                            byte[] bytes = message.getPayload();
                            String s = new String(bytes);
                            Toast.makeText(MqttConnectionManagerService.this, "Recieved new temperature", Toast.LENGTH_SHORT).show();
                            TextView temperatureText = (TextView) MqttConnectionManagerService.activity.findViewById(R.id.temperature_value_text);
                            temperatureText.setText(s);
                        }
                        else if(topic.equals("nodemcu/humidity")){
                            byte[] bytes = message.getPayload();
                            String s = new String(bytes);
                            Toast.makeText(MqttConnectionManagerService.this, "Recieved new temperature", Toast.LENGTH_SHORT).show();
                            TextView humidityText = (TextView) MqttConnectionManagerService.activity.findViewById(R.id.humidity_level_text);
                            humidityText.setText(s);
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        if(needRefresh) {
            String topic = "nodemcu/requests";
            String payload = "NEED REFRESH!";
            byte[] encodedPayload;
            encodedPayload = payload.getBytes();
            MqttMessage message = new MqttMessage(encodedPayload);
            try {
                client.publish(topic, message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Toast.makeText(activity, "Publish successful", Toast.LENGTH_SHORT).show();
        }
    }
}