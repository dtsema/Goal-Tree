package com.example.daniel.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class StopwatchFragment extends Fragment {
    ArrayAdapter<String> adapter;
    List<String> list;
    SharedPreferences storedDailyGoalsFile, activityFile;
    SharedPreferences.Editor dayGoalEditor, activityFileEditor;
    public long mLastStopTime, goalMinutes, goalHours, goalTreeFrame, dayGoalMin;
    TextView hourInputGoal, minuteInputGoal;
    public Spinner activitySpinner;
    private boolean isRunning, startTimer, framesAdded;
    private long goalProgressBarFrame;
    Calendar c = Calendar.getInstance();
    int month = c.get(Calendar.MONTH), day = c.get(Calendar.DATE), year = c.get(Calendar.YEAR),
            ANIMATION_INTERVAL_TREE, ANIMATION_INTERVAL_BAR_DAILY;
    String activityType, userInput, date = Integer.toString(month)+Integer.toString(day)+Integer.toString(year);
    DailyGoalsFragment mDailyGoalsFragment;
    EditText activityInput;
    TimeGraphFragment mTimeGraphFragment;

    public StopwatchFragment() {
    }

    public static StopwatchFragment newInstance(DailyGoalsFragment dailyGoalsFragment, TimeGraphFragment timeGraphFragment) {
        StopwatchFragment fragment = new StopwatchFragment();
        Bundle args = new Bundle();
        args.putSerializable("DAILY_GOALS", dailyGoalsFragment);
        args.putSerializable("TIME_GRAPH", timeGraphFragment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_stopwatch, container, false);

        loadGoalInputs(view);

        setItemsVisibility();

        clearActivityInputOnClick(view);

        createActivitySpinner(view);

        getFragmentArgs();

        createStartButton(view);

        createPauseButton(view);

        createStopButton(view);

        createActivityArray();

        createAddActivityButton(view);

        createDeleteActivityButton(view);

        return view;
    }
    private void loadGoalInputs(View view){
        hourInputGoal = view.findViewById(R.id.hourInputGoal);
        minuteInputGoal = view.findViewById(R.id.minuteInputGoal);
    }

    private void setItemsVisibility(){
        MainActivity.dayProgBar.setVisibility(View.INVISIBLE);
        MainActivity.chronometer.setVisibility(View.VISIBLE);
    }

    private void clearActivityInputOnClick(View view){
        activityInput = view.findViewById(R.id.activityInput);
        activityInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityInput.getText().clear();
            }
        });
    }

    private void createActivitySpinner(View view){
        activitySpinner = view.findViewById(R.id.activitySpinner);
        ArrayList<String> urls = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.activities_array)));
        adapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_layout, urls);
        adapter.setDropDownViewResource( R.layout.spinner_layout);
        activitySpinner.setAdapter(adapter);
        activitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                storeLastSelectedActivity();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }
    private void storeLastSelectedActivity(){
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences("lastSelected", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastSelected", activitySpinner.getSelectedItem().toString());
        editor.apply();
    }



    private void getFragmentArgs(){
        mTimeGraphFragment = (TimeGraphFragment) getArguments().getSerializable("TIME_GRAPH");
        mDailyGoalsFragment = (DailyGoalsFragment) getArguments().getSerializable("DAILY_GOALS");
    }

    private void createStartButton(View view){
        ImageButton start = view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRunning) {
                    getTodayGoal();
                    if (startTimer) {
                        setAnimations();
                        startTimer();
                    }
                }
            }
        });
    }

    private void getTodayGoal(){
        storedDailyGoalsFile = getStored(MainActivity.activityType);
        dayGoalMin = storedDailyGoalsFile.getLong("dayGoal" + date, 0);
        startTimer = false;

        if (goalMinHrInputted()) {
            storeGoalInMins();
            startTimer = true;
        } else if (goalHrInputted()) {
            startTimer = false;
            createAlert("Please enter minutes");

        } else if (goalMinInputted()) {
            startTimer = false;
            createAlert("Please enter hours");
        } else {
            startTimer = true;
            convertMinToHrMin();
        }
        System.out.println(dayGoalMin + " dgm");
    }

    private SharedPreferences getStored(String file){
      return getActivity().getApplicationContext().getSharedPreferences(file, 0);
    }

    private Boolean goalMinHrInputted(){
        return !hourInputGoal.getText().toString().equals("") && !minuteInputGoal.getText().toString().equals("");
    }

    private void storeGoalInMins(){
        goalHours = Integer.parseInt(hourInputGoal.getText().toString());
        goalMinutes = Integer.parseInt(minuteInputGoal.getText().toString());
        dayGoalEditor = storedDailyGoalsFile.edit();

        dayGoalEditor.putLong("dayGoal" + date, ((goalHours*60) + goalMinutes));
        dayGoalEditor.apply();

        dayGoalMin= storedDailyGoalsFile.getLong("dayGoal" + date, 0);
    }

    private Boolean goalHrInputted(){
        return !hourInputGoal.getText().toString().equals("") && minuteInputGoal.getText().toString().equals("");
    }

    private void createAlert(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private Boolean goalMinInputted(){
        return hourInputGoal.getText().toString().equals("") && !minuteInputGoal.getText().toString().equals("");
    }

    private void convertMinToHrMin(){
        if (dayGoalMin >= 60) {
            goalHours = dayGoalMin / 60;
            goalMinutes = dayGoalMin % 60;
        } else {
            goalHours = 0;
            goalMinutes = dayGoalMin;
        }
    }

    private void setAnimations(){
        ANIMATION_INTERVAL_BAR_DAILY = getAnimationInterval(dayGoalMin, 110);
        ANIMATION_INTERVAL_TREE = getAnimationInterval(dayGoalMin, 301);

        activityType = activitySpinner.getSelectedItem().toString();
        activityFile = getStored(activityType);

        long storedMiliSumDaily = activityFile.getLong(date, 0);

        if (dayGoalMin > 0) {
            goalProgressBarFrame = setCurrentFrame(storedMiliSumDaily, dayGoalMin, 110);

            goalTreeFrame = setCurrentFrame(storedMiliSumDaily, dayGoalMin, 301);
        }
        if (!framesAdded) {
            addFrames();
        }
        else {
            resetFrames();
        }
        if (dayGoalMin > 0) {
            MainActivity.mTreeAnimationContainer.start();
            MainActivity.mBarAnimationContainer.start();
        }
    }

    private Integer getAnimationInterval(Long goal, Integer framesTotal){
        return (int) ((goal *60000)/ framesTotal);
    }

    private Long setCurrentFrame(long timeSum, Long goalTime, Integer frames){
        return ((int)(timeSum) * frames) / (goalTime*60000);
    }

    private void addFrames(){
        TreeAnimationContainer.newDuration = false;
        MainActivity.mTreeAnimationContainer.addAllFrames(MainActivity.IMAGE_RESOURCES,
                ANIMATION_INTERVAL_TREE);
        MainActivity.mTreeAnimationContainer.setIndex((int) goalTreeFrame);



        MainActivity.mBarAnimationContainer.addAllFrames(MainActivity.IMAGE_RESOURCES1,
                ANIMATION_INTERVAL_BAR_DAILY);
        MainActivity.mBarAnimationContainer.setIndex((int) goalProgressBarFrame);

        framesAdded = true;
    }

    private void resetFrames(){
        TreeAnimationContainer.newDuration = false;
        MainActivity.mTreeAnimationContainer.removeAllFrames();
        MainActivity.mTreeAnimationContainer.addAllFrames(MainActivity.IMAGE_RESOURCES,
                ANIMATION_INTERVAL_TREE);
        MainActivity.mTreeAnimationContainer.setIndex((int) goalTreeFrame);

        MainActivity.mBarAnimationContainer.removeAllFrames();
        MainActivity.mBarAnimationContainer.addAllFrames(MainActivity.IMAGE_RESOURCES1,
                ANIMATION_INTERVAL_BAR_DAILY);
        MainActivity.mBarAnimationContainer.setIndex((int) goalProgressBarFrame);
    }

    private void startTimer(){
        if (mLastStopTime == 0)
            MainActivity.chronometer.setBase(SystemClock.elapsedRealtime());
        else {
            long intervalOnPause = (SystemClock.elapsedRealtime() - mLastStopTime);
            MainActivity.chronometer.setBase(MainActivity.chronometer.getBase() + intervalOnPause);
        }
        MainActivity.chronometer.start();
        isRunning = true;
    }

    private void createPauseButton(View view){
        ImageButton pause = view.findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.chronometer.stop();
                MainActivity.mTreeAnimationContainer.stop();
                MainActivity.mBarAnimationContainer.stop();
                isRunning=false;
                mLastStopTime = SystemClock.elapsedRealtime();
            }
        });
    }

    private void createStopButton(View view){
        ImageButton stop = view.findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.chronometer.stop();
                MainActivity.chronometer.setText(R.string.timeZero);

                MainActivity.mTreeAnimationContainer.stop();
                MainActivity.mBarAnimationContainer.stop();

                isRunning=false;
                mLastStopTime = 0;

                long elapsedMillis = (SystemClock.elapsedRealtime() -  MainActivity.chronometer.getBase());
                long elapsedMillis2 = (SystemClock.elapsedRealtime() -  MainActivity.chronometer.getBase());

                activityType = activitySpinner.getSelectedItem().toString();

                MainActivity.storedMiliSum = activityFile.getLong("Sum Time", 0);
                MainActivity.storedMiliSumDaily = activityFile.getLong(date, 0);

                activityFileEditor = activityFile.edit();
                activityFileEditor.putLong("Sum Time",  elapsedMillis+MainActivity.storedMiliSum);
                activityFileEditor.putLong(date,  elapsedMillis2+MainActivity.storedMiliSumDaily);

                activityFileEditor.apply();

                MainActivity.storedMiliSum = activityFile.getLong("Sum Time", 0);
                MainActivity.storedMiliSumDaily = activityFile.getLong(date, 0);

                if (TimeGraphFragment.timeSum!=null){
                    MainActivity.storedMiliToHMS(MainActivity.storedMiliSum, TimeGraphFragment.timeSum);}

                if (DailyGoalsFragment.dailySum!=null) {
                    MainActivity.storedMiliToHMS(MainActivity.storedMiliSumDaily, DailyGoalsFragment.dailySum);
                }
            }
        });
    }



    private void createActivityArray(){
        try {
            JSONArray  jsonarray = loadJSONArray(getActivity().getApplicationContext(), "ActivityList", "list");
            list = new ArrayList<>();
            for (int i=0; i<jsonarray.length(); i++) {
                list.add(jsonarray.getString(i));
                adapter.add(jsonarray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createAddActivityButton(View view) {
        FloatingActionButton addActivity = view.findViewById(R.id.fab);
        addActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput = activityInput.getText().toString();
                if(activityEntered()){
                    displayAddedText(view);
                    saveActivityToList();
                }
            }});
    }

    private Boolean activityEntered(){
        return !userInput.trim().equals("") &&!userInput.isEmpty();
    }

    private void displayAddedText(View view){
        Snackbar.make(view, "Added " + userInput + " to your Activities", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void saveActivityToList(){
        adapter.setNotifyOnChange(true);
        adapter.add(userInput);
        activityInput.getText().clear();
        list.add(userInput);
        JSONArray array = new JSONArray(list);
        saveJSONArray(getActivity().getApplicationContext(), "ActivityList", "list", array);
    }

    private void createDeleteActivityButton(View view){
        FloatingActionButton deleteActivity = view.findViewById(R.id.fab2);
        deleteActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (spinnerItemNotNull()){
                  createAlertDialogue();
                }}
        });
    }

    private Boolean spinnerItemNotNull(){
        return !list.isEmpty() && !activitySpinner.getSelectedItem().toString().equals("");
    }

    private void createAlertDialogue(){
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Delete Activity");
        alertDialog.setMessage("Are you sure you would like to permanently delete this activity and all of its stored data?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteActivity();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void deleteActivity(){
        String deleteString = activitySpinner.getSelectedItem().toString();
        SharedPreferences settings = getActivity().getApplicationContext().getSharedPreferences(deleteString, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
        String activity = activitySpinner.getSelectedItem().toString();
        adapter.remove(activity);
        adapter.setNotifyOnChange(true);

        for (int i = 0; i < list.size(); i++){
            if (list.get(i).equals(deleteString)){
                list.remove(i);
            }
        }
        JSONArray array = new JSONArray(list);
        saveJSONArray(getActivity().getApplicationContext(), "ActivityList", "list", array);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static JSONArray loadJSONArray(Context c, String prefName, String key) throws JSONException {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        return new JSONArray(settings.getString(key, "[]"));
    }

    public static void saveJSONArray(Context c, String prefName, String key, JSONArray array) {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, array.toString());
        editor.apply();
    }


}
