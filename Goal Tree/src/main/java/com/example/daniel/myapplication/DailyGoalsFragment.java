package com.example.daniel.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class DailyGoalsFragment extends Fragment implements Serializable {
    CheckBox sunday, monday, tuesday, wednesday, thursday, friday, saturday;
    Spinner hourSpinner, minuteSpinner, timeFrameSpinner;
    EditText timeFrameUI;
    ImageButton setGoals;
    ArrayAdapter[] adapters;
    Boolean[] weekdays;
    Calendar c = Calendar.getInstance();
    TextView activity;
    Integer timeFrameTotal, timeFrameDaysTotal, month = c.get(Calendar.MONTH),
            day = c.get(Calendar.DATE), year = c.get(Calendar.YEAR),
            weekDay = c.get(Calendar.DAY_OF_WEEK), todayWeekday = weekDay-1,
            daysThisMonth;
    Long goalMinutes;
    @SuppressLint("StaticFieldLeak")
    static TextView dailySum;

    public DailyGoalsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_goals, container, false);

        setItemsVisibility();

        findViews(view);

        setAdapters();

        setGoalsButton();

        MainActivity.storedMiliToHMS(MainActivity.storedMiliSumDaily, dailySum);

        return view;
    }


    private void setItemsVisibility(){
        MainActivity.dayProgBar.setVisibility(View.VISIBLE);
        MainActivity.chronometer.setVisibility(View.INVISIBLE);
        MainActivity.tree.setVisibility(View.VISIBLE);
    }

    private void findViews(View view){
        activity = view.findViewById(R.id.currentActivityGoals);
            activity.setText(TimeGraphFragment.activityType);
        sunday = view.findViewById(R.id.sunday);
        monday = view.findViewById(R.id.monday);
        tuesday = view.findViewById(R.id.tuesday);
        wednesday = view.findViewById(R.id.wednesday);
        thursday = view.findViewById(R.id.thursday);
        friday = view.findViewById(R.id.friday);
        saturday = view.findViewById(R.id.saturday);
        hourSpinner = view.findViewById(R.id.spinner4);
        minuteSpinner = view.findViewById(R.id.spinner3);
        timeFrameSpinner = view.findViewById(R.id.spinner5);
        timeFrameUI = view.findViewById(R.id.timeFrameUI);
        dailySum = view.findViewById(R.id.dailySum);
        setGoals = view.findViewById(R.id.setGoals);
    }

    private void setAdapters(){
        Integer[] goalArrays = {R.array.HourGoal, R.array.MinuteGoal, R.array.TimeFrame};
        Spinner[] spinners = {hourSpinner, minuteSpinner, timeFrameSpinner};
        adapters = new ArrayAdapter[3];

        for (int i = 0; i < goalArrays.length; i++){
            ArrayList<String> list = new ArrayList<>(Arrays.asList(getResources().getStringArray(goalArrays[i])));
            ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_layout2, list);
            spinners[i].setAdapter(adapter);
            adapters[i] = adapter;
        }
    }

    private void setGoalsButton(){
        setGoals.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                getSelectedWeekdays();

                getSelectedTimeFrame();

                getSelectedGoalTime();

                storeGoals();
            }
        });
    }

    private void getSelectedWeekdays(){
        boolean  sun=false, mon=false, tue=false, wed=false, thu=false, fri=false, sat=false;
        weekdays = new Boolean[]{sun, mon, tue, wed, thu, fri, sat};

        if (sunday.isChecked()) weekdays[0] = true;
        if (monday.isChecked()) weekdays[1] = true;
        if (tuesday.isChecked()) weekdays[2] = true;
        if (wednesday.isChecked())weekdays[3] = true;
        if (thursday.isChecked()) weekdays[4] = true;
        if (friday.isChecked()) weekdays[5] = true;
        if (saturday.isChecked()) weekdays[6] = true;
    }

    private void getSelectedTimeFrame(){
        timeFrameTotal = Integer.parseInt(timeFrameUI.getText().toString());
        String timeFrameType = timeFrameSpinner.getSelectedItem().toString();

        switch (timeFrameType) {
            case "Day":
                timeFrameDaysTotal = timeFrameTotal;
                break;
            case "Week":
                timeFrameDaysTotal = timeFrameTotal * 7;
                break;
            case "Month":
                timeFrameDaysTotal = timeFrameTotal * 30;
                break;
            default:
                timeFrameDaysTotal = timeFrameTotal * 365;
                break;
        }
    }

    private void getSelectedGoalTime(){
        Long goalHours = Long.parseLong(hourSpinner.getSelectedItem().toString());
        goalMinutes = Long.parseLong(minuteSpinner.getSelectedItem().toString());
        goalMinutes = (goalHours*60) + goalMinutes;
    }

    private void storeGoals(){
        for (int i = 0; i < timeFrameDaysTotal; i++){
            daysThisMonth = getDaysThisMonth();

            if (thisWeekdaySelected()) {
                storeGoalOnThisDate();
                nextDay();
            }
            else{
                nextDay();
            }
        }
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DATE);
        year = c.get(Calendar.YEAR);
        weekDay = c.get(Calendar.DAY_OF_WEEK);
        todayWeekday = weekDay-1;
    }

    private Integer getDaysThisMonth(){
        Integer[] monthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

        return monthDays[month];
    }

    private Boolean thisWeekdaySelected(){
        return weekdays[todayWeekday];
    }

    private void storeGoalOnThisDate(){
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(TimeGraphFragment.activityType, 0);
        SharedPreferences.Editor editor = settings.edit();
        String date = Integer.toString(month) + Integer.toString(day) + Integer.toString(year);
        editor.putLong("dayGoal" + date, goalMinutes);
        editor.apply();
    }

    private void nextDay(){
        if (lastDayOfMonth()) {
            setFirstDayNextMonth();
            updateWeekday();
        }
        else{
            day++;
            updateWeekday();
        }
    }

    private Boolean lastDayOfMonth(){
        return day >= daysThisMonth;
    }

    private void setFirstDayNextMonth(){
        month++;
        day = 1;
    }

    private void updateWeekday(){
        if (todayWeekday <6) todayWeekday++;
        else todayWeekday = 0;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
