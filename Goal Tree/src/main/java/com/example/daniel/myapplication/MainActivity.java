package com.example.daniel.myapplication;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    static MainActivity mainActivity;
    @SuppressLint("StaticFieldLeak")
    static TimeGraphFragment timeGraphFragment;
     StopwatchFragment stopwatchFragment;
     GoalTreeFragment goalTreeFragment;
    @SuppressLint("StaticFieldLeak")
    static DailyGoalsFragment dailyGoalsFragment;
    public static TreeAnimationContainer mTreeAnimationContainer;
    public static BarAnimationContainer mBarAnimationContainer;
    public static int[] IMAGE_RESOURCES, IMAGE_RESOURCES1;
    public static int goalProgressBarFrameDaily;
    static long storedMiliSum, storedMiliSumDaily;
    @SuppressLint("StaticFieldLeak")
    public static ImageView tree, dayProgBar;
    static String activityType, date;
    Integer todayGoal;
    static SharedPreferences timeSumSettings;
    public TextView timeSum, dailySum;
    @SuppressLint("StaticFieldLeak")
    public static Chronometer chronometer;
    static Handler handler;
    TypedArray treeAnimation, progressBarAnimation;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.timeGraphTab:
                    if(activitySelected()){
                        setActivityType();
                        transaction.replace(R.id.mainActivityFragment, timeGraphFragment).commit();
                        return true;
                    }
                    else {
                        createAlert();
                    }
                case R.id.stopwatchTab:
                    transaction.replace(R.id.mainActivityFragment, stopwatchFragment).commit();
                    return true;

                case R.id.treeTab:
                    transaction.replace(R.id.mainActivityFragment, goalTreeFragment).commit();
                    return true;

                case R.id.dailyGoalsTab:
                    transaction.replace(R.id.mainActivityFragment, dailyGoalsFragment).commit();
                    return true;
            }
                    return false;
        }
    };


    private Boolean activitySelected(){
        return stopwatchFragment.activitySpinner.getSelectedItem() != null;
    }

    private void setActivityType(){
        TimeGraphFragment.activityType= stopwatchFragment.activitySpinner.getSelectedItem().toString();
    }

    private void createAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Error");
        alertDialog.setMessage("Please create an activity first");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();}



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();

        createFragments();

        loadChronometer();

        getDate();

        getLastSelectedActivityType();

        getTotalTimeTracked();

        getTodayTimeTracked();

        getTodayGoal();

        loadTreeFrames();

        loadProgressBarFrames();

        setCurrentProgressFrame();

        loadStopwatchFragment();

    }

    private void createFragments(){
        stopwatchFragment = StopwatchFragment.newInstance(dailyGoalsFragment, timeGraphFragment);
        timeGraphFragment = new TimeGraphFragment();
        goalTreeFragment = new GoalTreeFragment();
        dailyGoalsFragment = new DailyGoalsFragment();
        BottomNavigationView botNav = findViewById(R.id.bottom_navigation);
        botNav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mainActivity = MainActivity.this;
    }

    private void loadChronometer(){
        chronometer = findViewById(R.id.chronometer);
    }

    private void getDate(){
        Calendar c = Calendar.getInstance();
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        int year = c.get(Calendar.YEAR);
        date = Integer.toString(month)+Integer.toString(day)+Integer.toString(year);
    }

    private void getLastSelectedActivityType(){
        SharedPreferences lastSelectedSettings = getApplicationContext().getSharedPreferences("lastSelected", 0);
        activityType = lastSelectedSettings.getString("lastSelected", "");
    }

    private void getTotalTimeTracked(){
        timeSumSettings =getApplicationContext().getSharedPreferences(activityType, 0);

        storedMiliSum = timeSumSettings.getLong("Sum Time", 0);
    }

    private void getTodayTimeTracked(){
        storedMiliSumDaily = timeSumSettings.getLong(date, 0);
    }

    private void getTodayGoal(){
        long todayGoalL = timeSumSettings.getLong("dayGoal"+date, 0);
        todayGoal = (int) (long) todayGoalL;
    }

    private void loadTreeFrames(){
        treeAnimation = getResources().obtainTypedArray(
                R.array.tree_animation);
        int count = treeAnimation.length();
        int[] ids = new int[count];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = treeAnimation.getResourceId(i, 0);
        }
        IMAGE_RESOURCES = ids;
        tree = findViewById(R.id.imageView);
        mTreeAnimationContainer = TreeAnimationContainer.getInstance(tree);
    }

    private void loadProgressBarFrames(){
        progressBarAnimation = getResources().obtainTypedArray(
                R.array.goalProgBar);
        int count1 = progressBarAnimation.length();
        int[] ids1 = new int[count1];
        for (int i = 0; i < ids1.length; i++) {
            ids1[i] = progressBarAnimation.getResourceId(i, 0);
        }
        IMAGE_RESOURCES1 = ids1;
        dayProgBar = findViewById(R.id.dayProgBar);
    }

    private void setCurrentProgressFrame() {
        goalProgressBarFrameDaily = 0;
        if (todayGoal > 0) {
            goalProgressBarFrameDaily = (int) ((storedMiliSumDaily / 60000) * 110) / todayGoal;
        }
        if (goalProgressBarFrameDaily < progressBarAnimation.length()) {
            dayProgBar.setImageResource(progressBarAnimation.getResourceId(goalProgressBarFrameDaily, 0));
        }
        mBarAnimationContainer = BarAnimationContainer.getInstance(dayProgBar);
    }

    private void loadStopwatchFragment(){
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mainActivityFragment, stopwatchFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void storedMiliToHMS (long storedMiliSum, TextView timeView)
    {
        if (storedMiliSum>0L) {
            int hours = (int) (storedMiliSum / 3600000);
            int minutes = (int) (storedMiliSum /60000);
            int seconds = (int) (storedMiliSum/1000);

            if (minutes >=60) {
                minutes/=60;
                hours+=1;}
            if (seconds >= 60) {
                int  plusMinutes=seconds/60;
                minutes = plusMinutes;
                seconds = seconds - (plusMinutes*60);


            }

            String min = String.format(Locale.US, "%02d", minutes);
            if (seconds<10){
                if (hours<10){
                    timeView.setText(String.format("0%s:%s:0%s", Integer.toString(hours), min, Integer.toString(seconds)));
                }
                else timeView.setText(String.format("%s:%s:0%s", Integer.toString(hours), min, Integer.toString(seconds)));

            }

            else {
                if (hours<10){
                    timeView.setText(String.format("0%s:%s:%s", Integer.toString(hours), min, Integer.toString(seconds)));
                }
                else timeView.setText(String.format("%s:%s:%s", Integer.toString(hours), min, Integer.toString(seconds)));
            }

        }else  timeView.setText(R.string.timeZero);
    }



}
