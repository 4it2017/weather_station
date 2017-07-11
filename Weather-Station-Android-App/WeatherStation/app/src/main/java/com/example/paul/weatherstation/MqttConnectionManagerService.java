package com.example.paul.weatherstation;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
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


/**
 * Created by Paul on 05-Jul-17.
 */

public class MqttConnectionManagerService extends Service{

    private MqttAndroidClient client;
    private MqttConnectOptions options;
    private String refreshTopic = "nodemcu/requests";
    private String temperatureTopic = "nodemcu/temperature";
    private String humidityTopic = "nodemcu/humidity";
    private static MainActivity activity;

    public void initialize(MainActivity activity){
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

    private void connect(final MqttAndroidClient client, MqttConnectOptions options, final boolean needRefresh)
    {
        try {
            if (!client.isConnected())
            {
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
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
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

                        if(topic.equals(MqttConnectionManagerService.this.temperatureTopic)){
                            Toast.makeText(MqttConnectionManagerService.this, "Recieved new temperature", Toast.LENGTH_SHORT).show();
                            activity.refreshTemperatureView(getMessageText(message));
                        }
                        else if(topic.equals(MqttConnectionManagerService.this.humidityTopic)){
                            Toast.makeText(MqttConnectionManagerService.this, "Recieved new humidity", Toast.LENGTH_SHORT).show();
                            activity.refreshHumidityView(getMessageText(message));
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
            try {
                client.publish(this.refreshTopic, this.getRefreshMessage());
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, "Publish successful", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMessageText(MqttMessage mqttMessage){
        return new String(mqttMessage.getPayload());
    }

    private MqttMessage getRefreshMessage(){
        String payload = "NEED REFRESH!";
        byte[] encodedPayload;
        encodedPayload = payload.getBytes();
        return new MqttMessage(encodedPayload);
    }
}