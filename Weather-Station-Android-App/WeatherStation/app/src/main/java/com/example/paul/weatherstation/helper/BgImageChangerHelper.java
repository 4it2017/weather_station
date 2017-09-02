package com.example.paul.weatherstation.helper;

import android.text.Layout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.paul.weatherstation.R;

import java.util.Random;

/**
 * Created by Paul on 02-Sep-17 at 2:55 PM.
 */

public class BgImageChangerHelper {

    public void pickRandomBg(LinearLayout layout){
        Random rand = new Random();
        int value = rand.nextInt(2) + 1;

        switch (value){
            case 1:{
                layout.setBackgroundResource(R.drawable.blue);
                break;
            }
            case 2:{
                layout.setBackgroundResource(R.drawable.building_planes);
                break;
            }
            default:
                layout.setBackgroundResource(R.color.backgroundColor);
        }
    }
}
