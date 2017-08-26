package com.example.paul.weatherstation.model;

/**
 * Created by Paul on 07-Aug-17 at 7:22.
 */

public class WeatherRecord {
    private int id;
    private String temperature;
    private String humidity;
    private String pressure;
    private String time;

    public WeatherRecord(){

    }

    public WeatherRecord(int id, String temperature, String humidity, String pressure, String time){
        this.id = id;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.time = time;
    }

    public WeatherRecord(String temperature, String humidity, String pressure, String time){
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.time = time;
    }

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void clear(){
        this.humidity = null;
        this.pressure = null;
        this.temperature = null;
        this.time = null;
    }

    public String getValueX(){
        return time;
    }

    public String getValueY(){
        return temperature;
    }
}
