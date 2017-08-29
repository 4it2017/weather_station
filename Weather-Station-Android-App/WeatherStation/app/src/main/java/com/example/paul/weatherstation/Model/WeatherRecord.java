package com.example.paul.weatherstation.model;

import java.util.Date;

/**
 * Created by Paul on 07-Aug-17 at 7:22.
 */

public class WeatherRecord {
    private int id;
    private String temperature;
    private String humidity;
    private String pressure;
    private Date time;

    public WeatherRecord(){

    }

    public WeatherRecord(int id, String temperature, String humidity, String pressure, Date time){
        this.id = id;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
        this.time = time;
    }

    public WeatherRecord(String temperature, String humidity, String pressure, Date time){
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

    public Date getTimeAsDate() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void clear(){
        this.humidity = null;
        this.pressure = null;
        this.temperature = null;
        this.time = null;
    }

    public Date getValueX(){
        return time;
    }

    public String getValueY(){
        return temperature;
    }
}
