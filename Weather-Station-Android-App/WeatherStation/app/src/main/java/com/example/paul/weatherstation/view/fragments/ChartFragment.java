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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 19-Aug-17 at 4:08 PM.
 */

public class ChartFragment extends Fragment{
    private Context context;
    public DatabaseHandler db;
    private SwipeRefreshLayout swipeContainer;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_fragment, container, false);

        context = getActivity();
        db = new DatabaseHandler(context);

        final LineChart chart = (LineChart) view.findViewById(R.id.chart);
        chart.setOnTouchListener(new ChartTouchListener(chart) {
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
                    entries.add(new Entry(i, Float.parseFloat(weatherRecord.getValueY())));
                    ++i;
                    isListEmpty = false;
                }
            }
            if(!isListEmpty) {
                LineDataSet dataSet = new LineDataSet(entries, "Weather Records");
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                chart.invalidate();
                this.chartSetup(chart);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show();
        }

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chart.invalidate();
                swipeContainer.setRefreshing(false);
            }
        });


        return view;
    }

    public void chartSetup(LineChart chart){
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDoubleTapToZoomEnabled(true);
    }
}
