package com.example.paul.weatherstation.helper;

import android.view.View;
import android.widget.ImageView;

import com.example.paul.weatherstation.R;

import java.util.Calendar;

/**
 * Created by Paul on 26-Aug-17 at 1:35 PM.
 */

public class WeatherImageChangerHelper {

    private ImageView comfortLevelImage;
    private ImageView weatherDescriptionImage;

    public WeatherImageChangerHelper(View view){
        this.comfortLevelImage = (ImageView) view.findViewById(R.id.comfort_level_image_description);
        this.weatherDescriptionImage = (ImageView) view.findViewById(R.id.weather_image_description);
    }

    public void updateImages(int temperature, int humidity){

        //Weather Image
        boolean freezing = temperature < 0;
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (humidity > 80) {
            if(freezing){
                this.weatherDescriptionImage.setImageResource(R.drawable.ic_021_snow_2);
            } else {
                this.weatherDescriptionImage.setImageResource(R.drawable.ic_rainy);
            }
        } else if (humidity > 60){
            this.weatherDescriptionImage.setImageResource(R.drawable.ic_026_cloud_13);
        } else {
            if(timeOfDay >= 21 || timeOfDay < 6){
                this.weatherDescriptionImage.setImageResource(R.drawable.ic_023_moon);
            } else if(timeOfDay >= 16 && timeOfDay < 18) {
                this.weatherDescriptionImage.setImageResource(R.drawable.ic_019_sunset);
            } else if(timeOfDay >= 18 && timeOfDay < 21){
                this.weatherDescriptionImage.setImageResource(R.drawable.ic_016_sunset_3);
            } else {
                this.weatherDescriptionImage.setImageResource(R.drawable.ic_015_sun);
            }
        }

        //Comfort Level
        if((temperature > 20 && temperature < 30 ) && ( humidity > 30 && humidity < 60 )) {
            this.comfortLevelImage.setImageResource(R.drawable.ic_emoticon_square_smile);
        } else if((( temperature > 30 && temperature < 35 ) || (temperature > 15 && temperature < 20)) && (( humidity > 10 && humidity < 30 ) || ( humidity > 60 && humidity < 80 ))){
            this.comfortLevelImage.setImageResource(R.drawable.ic_emoticon_square_face_with_straight_mouth);
        } else if((( temperature > 30 && temperature < 35 ) || (temperature > 15 && temperature < 20)) && ( humidity > 30 && humidity < 60 )){
            this.comfortLevelImage.setImageResource(R.drawable.ic_emoticon_square_face_with_straight_mouth);
        } else if((temperature > 20 && temperature < 30 ) && (( humidity > 10 && humidity < 30 ) || ( humidity > 60 && humidity < 80 ))){
            this.comfortLevelImage.setImageResource(R.drawable.ic_emoticon_square_face_with_straight_mouth);
        } else {
            this.comfortLevelImage.setImageResource(R.drawable.ic_sad_emoticon_square_face);
        }
    }
}
