package com.example.paul.weatherstation.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.paul.weatherstation.model.DatabaseHandler;
import com.example.paul.weatherstation.model.WeatherRecord;
import com.example.paul.weatherstation.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Paul on 19-Aug-17 at 4:08 PM.
 */

public class ChartFragment extends Fragment{
    private Context context;
    public DatabaseHandler db;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_fragment, container, false);

        context = getActivity();
        db = new DatabaseHandler(context);

        final LineChart chart = (LineChart) view.findViewById(R.id.graph);
        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        try {
            List<WeatherRecord> weatherRecords = db.getAllWeatherRecords();
            List<Entry> entries = new ArrayList<>();
            boolean isListEmpty = true;
            for (WeatherRecord weatherRecord : weatherRecords){
                int i = 0;
                if(weatherRecord.getValueY()!= null) {
                    entries.add(new Entry(weatherRecord.getValueX().getTime(), Float.parseFloat(weatherRecord.getValueY())));
                    ++i;
                    isListEmpty = false;
                }
            }
            if(!isListEmpty) {
                LineDataSet dataSet = new LineDataSet(entries, "Weather Records");
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setDrawGridLines(false);
                xAxis.setValueFormatter(createDateFormatter());
                xAxis.setDrawLabels(true);
                xAxis.setCenterAxisLabels(true);
                xAxis.setLabelRotationAngle(90f); // rotates label so we can see it all
                this.chartSetup(chart);
                chart.invalidate();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    public void chartSetup(LineChart chart){
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
    }

    public WeatherRecord[] getFirstAndLastWeatherRecord(){
        WeatherRecord[] weatherRecords = new WeatherRecord[2];
        boolean first = true;
        for (WeatherRecord weatherRecord : db.getAllWeatherRecords()){
            if(first){
                weatherRecords[0] = weatherRecord;
                first = false;
            }
                weatherRecords[1] = weatherRecord;
        }
        return weatherRecords;
    }

    IAxisValueFormatter createDateFormatter() {
        IAxisValueFormatter formatter = new IAxisValueFormatter() {


            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date((long) value);

                SimpleDateFormat fmt;

                fmt = new SimpleDateFormat("MMM d H:mm"); //TODO remove after tests and add switch
                fmt.setTimeZone(TimeZone.getDefault()); // sets time zone... I think I did this properly...


                String s = fmt.format(date);


                return s;
            }

            // we don't draw numbers, so no decimal digits needed
            public int getDecimalDigits() {
                return 0;
            }


        };

        return formatter;
    }
}
