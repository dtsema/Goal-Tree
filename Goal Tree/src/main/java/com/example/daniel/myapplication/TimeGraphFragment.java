package com.example.daniel.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


public class TimeGraphFragment extends Fragment implements Serializable {

    ArrayList<Integer> dates;
    ArrayAdapter[] adapters;
    GraphView graph;
    DataPoint[] dp;
    Spinner monthStart, monthEnd, dayStart, dayEnd, year, yearEnd;
    public static String activityType;
    Integer i =0,startMonthNumber, startDayNumber, endMonthNumber, endDayNumber, label;
    @SuppressLint("StaticFieldLeak")
    static TextView timeSum;
    TextView activity;
    ImageButton graphButton;

    public TimeGraphFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_timegraph, container, false);

        setItemVisibility();

        findViews(view);

        createMonthAdapters();

        setMonthAdapters();

        formatGraph();

        graphButton();

        getActivityAndSum();

        return view;
    }

    private void setItemVisibility(){
        MainActivity.tree.setVisibility(View.VISIBLE);
        MainActivity.chronometer.setVisibility(View.GONE);
        MainActivity.dayProgBar.setVisibility(View.INVISIBLE);
    }

    private void findViews(View view){
        activity = view.findViewById(R.id.activity);
        timeSum = view.findViewById(R.id.timeSum);
        monthStart = view.findViewById(R.id.month);
        dayStart = view.findViewById(R.id.day);
        year = view.findViewById(R.id.year);
        monthEnd = view.findViewById(R.id.month1);
        dayEnd = view.findViewById(R.id.day1);
        yearEnd = view.findViewById(R.id.year1);
        graphButton = view.findViewById(R.id.graph_button);
        graph = view.findViewById(R.id.graph);
    }

    private void createMonthAdapters(){
        Integer[] monthArrays = {R.array.Months, R.array.Months1, R.array.Months2, R.array.Months3, R.array.Months4, R.array.Months5,
                R.array.Months6, R.array.Months7, R.array.Months8, R.array.Months9, R.array.Months10, R.array.Months11, R.array.Days31,
                R.array.Days30, R.array.Days28, R.array.Years};

        adapters = new ArrayAdapter[16];

        for (int i = 0; i < monthArrays.length; i++){
            ArrayList<String> list = new ArrayList<>(Arrays.asList(getResources().getStringArray(monthArrays[i])));
            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_layout2, list);



            adapters[i] = adapter;
        }

        monthStart.setAdapter(adapters[0]);
        dayStart.setAdapter(adapters[12]);
        year.setAdapter(adapters[15]);
        monthEnd.setAdapter(adapters[0]);
        dayEnd.setAdapter(adapters[12]);
        yearEnd.setAdapter(adapters[15]);
    }

    private void setMonthAdapters(){
        monthStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                int pos = monthStart.getSelectedItemPosition();
                if(pos ==1){
                    dayStart.setAdapter(adapters[14]);
                }
                else if (pos ==3 ||pos ==5 ||pos ==8 ||pos ==10){
                    dayStart.setAdapter(adapters[13]);
                }
                else{
                    dayStart.setAdapter(adapters[12]);
                }
                monthEnd.setAdapter(adapters[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        monthEnd.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String month = monthEnd.getSelectedItem().toString();
                switch (month) {
                    case "February":
                        dayEnd.setAdapter(adapters[14]);
                        break;
                    case "April":
                    case "June":
                    case "September":
                    case "November":
                        dayEnd.setAdapter(adapters[13]);
                        break;
                    default:
                        dayEnd.setAdapter(adapters[12]);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void formatGraph(){
        graph.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        graph.getGridLabelRenderer().setGridColor(Color.WHITE);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);
    }

    private void graphButton(){
        dates = new ArrayList<>();
        dp=new DataPoint[0];
        graphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDateRange();
                if(invalidDate()) {
                    createAlert();
                }
                else{
                    createGraph();
                    formatLabels();
                }
            }
        });
    }

    private void getDateRange(){
        startMonthNumber = monthStart.getSelectedItemPosition();
        startDayNumber = dayStart.getSelectedItemPosition()+1;
        endMonthNumber = monthEnd.getSelectedItemPosition() +startMonthNumber;
        endDayNumber = dayEnd.getSelectedItemPosition()+1;
    }

    private Boolean invalidDate(){
        return startMonthNumber.equals(endMonthNumber) && startDayNumber>endDayNumber;
    }

    private void createAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Please select a valid date range");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();}

    private void createGraph(){
        graph.removeAllSeries();
        dp = getDateDataPoint(startDayNumber,endDayNumber,startMonthNumber,endMonthNumber);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dp);
        series.setDrawDataPoints(true);

        graph.addSeries(series);
        graph.getViewport().setMinX(startDayNumber);
        graph.getViewport().setMaxX(label);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScrollable(true);
    }

    private void formatLabels(){
        graph.getGridLabelRenderer().setNumHorizontalLabels(dp.length);
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {

                if (isValueX) {
                    return super.formatLabel(getDateValue(value), true);
                } else {
                    if (toMins(value)==0){
                        return super.formatLabel(toHours(value), false) + ":" + super.formatLabel(toMins(value), false) + super.formatLabel(toMins(value), false) ;
                    }
                    else if (toMins(value)<10){
                        return super.formatLabel(toHours(value), false) + ":0" + super.formatLabel(toMins(value), false) ;
                    }
                    else{
                        return super.formatLabel(toHours(value), false) + ":" + super.formatLabel(toMins(value), false) ;
                    }
                }
            }
        });
    }

    private void getActivityAndSum(){
        MainActivity.storedMiliToHMS(MainActivity.storedMiliSum, timeSum);
        activity.setText(activityType);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private int toHours(double value){
        return (int) (value/3600000);
    }

    private int toMins (double value){
        return (int) (value/60000);
    }

    public Double getDateValue(Double value){
        Double addBy;
        Double maxMonthDate;
        if (dates.get(i) == 0 ||dates.get(i) == 2 ||dates.get(i) == 4 ||dates.get(i) == 6 ||
                dates.get(i) == 7 ||dates.get(i) == 9 ||dates.get(i) == 11) {
            maxMonthDate = 31.0;
        }
        else if( dates.get(i)==1) {
            maxMonthDate = 28.0;
        }
        else{
            maxMonthDate = 30.0;
        }
        if (value.equals(maxMonthDate))i++;

        if (value>maxMonthDate) {
            addBy = value - maxMonthDate;
            value= addBy;

            if (value>maxMonthDate){
                addBy = value - maxMonthDate;
                value= addBy;
            }
        }
        return value;
    }

    private DataPoint[] getDateDataPoint(int dayStart, int dayEnd, int monthStart, int monthEnd){
        int a;
        int dpPos=0;
        Integer[] monthDays = {31,28,31,30,31,30,31,31,30,31,30,31};

        if (monthStart!=monthEnd){
            a = monthDays[monthStart]-(dayStart-1);

            for (int i = monthStart+1; i <=monthEnd; i++){
                if (i==monthEnd){
                a+=dayEnd;
                break;
                }
            a+=monthDays[i];
            }
       }
        else {
            a=dayEnd - (dayStart-1);
        }
        DataPoint[] dp =new DataPoint[a];
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(activityType, 0);
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        label = dayStart;
        for (int j = monthStart; j<=monthEnd;j++) {
            dates.add(j);
            if (j ==monthStart){
                for (int i = dayStart; i <= monthDays[j]; i++, dpPos++, label++){
                    if (dpPos==a) break;
                    String date = Integer.toString(j)+Integer.toString(i)+Integer.toString(year);
                    Long storedMinuteSumDaily = settings.getLong(date, 0);
                    dp[dpPos]=new DataPoint(label, storedMinuteSumDaily);
                }
            }
            else{
                for (int i = 1; i<monthDays[j]+1; i++, dpPos++, label++){
                    if (j == monthEnd && i > dayEnd){
                        break;
                    }
                    String date = Integer.toString(j)+Integer.toString(i)+Integer.toString(year);
                    Long storedMinuteSumDaily = settings.getLong(date, 0);
                    dp[dpPos]=new DataPoint(label, storedMinuteSumDaily);
                }
            }
        }
        return dp;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
