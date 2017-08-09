package com.example.paul.weatherstation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    MqttAndroidClient mqttAndroidClient;
    MqttConnectOptions mqttConnectOptions;
    private final String serverUri = "tcp://m20.cloudmqtt.com:16691";
    private final String refreshTopic = "nodemcu/requests";
    private final String temperatureTopic = "nodemcu/2916367/temperature";
    private final String humidityTopic = "nodemcu/2916367/humidity";
    private final String pressureTopic = "nodemcu/2916367/pressure";
    private final WeatherRecord weatherRecord = new WeatherRecord();
    private final DatabaseHandler db = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Set Last Values To Views

        refreshTemperatureView(db.getLastWeatherRecord().getTemperature());
        refreshHumidityView(db.getLastWeatherRecord().getHumidity());

        //Refresh Action

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
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
        this.mqttConnectOptions = createMqttConnectOptions();

        this.mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(MainActivity.this, "Connection lost!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                switch (topic) {
                    case temperatureTopic:
                        refreshTemperatureView(message.toString());
                        weatherRecord.setTemperature(message.toString());
                        addToDb();
                        break;
                    case humidityTopic:
                        refreshHumidityView(message.toString());
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
                    Snackbar snackbar = Snackbar
                            .make(drawer, "Connection Successful!", Snackbar.LENGTH_LONG);

                    snackbar.show();

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Something went wrong connecting to server.", Toast.LENGTH_SHORT).show();
                    exception.printStackTrace();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        //Handle back button to close the drawer if it's opened and do the default action otherwise
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    public void refreshTemperatureView(String s){
        TextView temperatureText = (TextView) findViewById(R.id.temperature_value_text);
        temperatureText.setText(s);
    }

    public void refreshHumidityView(String s){
        TextView humidityText = (TextView) findViewById(R.id.humidity_level_text);
        humidityText.setText(s);
    }

    private MqttConnectOptions createMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setCleanSession(false);
        options.setUserName("android");
        options.setPassword("android".toCharArray());
        return options;
    }

    private MqttAndroidClient createMqttAndroidClient() {
        String clientId = MqttClient.generateClientId();
        return new MqttAndroidClient(getApplicationContext(), this.serverUri,clientId);
    }

    public void subscribeToTopics(){
        try {
            mqttAndroidClient.subscribe("nodemcu/#", 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
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
}
