package com.project.plaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MoistureMenu extends AppCompatActivity {

    //Global variables
    Model m;
    AtomicBoolean hasQuit;
    Lock netLock = new ReentrantLock();

    //Defining objects used for timed code
    HandlerThread timerThread;
    Handler timerHandler;
    Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            //Create variables for screen objects
            ProgressBar waterBar = (ProgressBar) findViewById(R.id.moiWaterBar);
            GraphView moiGraph = findViewById(R.id.moiMoistureGraph);


            //Get current water level, set corresponding bar
            netLock.lock();
            try {
                waterBar.setProgress(m.getVal("w"));
            } catch (Exception e) {
                noConnection();
            } finally {
                netLock.unlock();
            }

            //Get and display graph data
            moiGraph.setTitle("Soil Moisture");
            LabelFormatter toTime = new LabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {

                        long millisecs = (long) value * 1000;
                        Date date = new Date(millisecs);
                        DateFormat format = new SimpleDateFormat("HH:mm");
                        format.setTimeZone(TimeZone.getTimeZone("Europe/Budapest"));
                        return format.format(date);
                    }
                    else return String.valueOf(value);
                }

                @Override
                public void setViewport(Viewport viewport) {

                }
            };
            moiGraph.getGridLabelRenderer().setLabelFormatter(toTime);


            netLock.lock();
            try {
                LineGraphSeries<DataPoint> ser = new LineGraphSeries<DataPoint>(m.getPlotData("m"));
                ser.setColor(Color.parseColor("#F4E99B"));
                moiGraph.addSeries( ser );
            } catch (Exception e) {
                noConnection();
            } finally {
                netLock.unlock();
            }
            timerHandler.postDelayed(timerTask, 300000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moisture_menu);

        //Get global model
        m = Model.getInstance();
        hasQuit = new AtomicBoolean(false);

        //Initiate timed task
        timerThread = new HandlerThread("Timer Thread");
        timerThread.start();
        timerHandler = new Handler(timerThread.getLooper());
        timerHandler.post(timerTask);

        //Create variables for screen objects
        SeekBar moistureBar = (SeekBar) findViewById(R.id.moiMoistureBar);
        TextView moistureValue = findViewById(R.id.moiMoistureValue);
        Button s_button = (Button) findViewById(R.id.moiSaveButton);
        Button c_button = (Button) findViewById(R.id.moiCancelButton);


        //Get initial current moisture level, set seekbar
        netLock.lock();
        try {
            moistureBar.setProgress( m.getAttr("m"));
        } catch (Exception e) {
            noConnection();
        } finally {
            netLock.unlock();
        }

        //Set moisture value text
        String value = String.valueOf(" " + moistureBar.getProgress()) + " %";
        moistureValue.setText( value );


        //Bind displayed text to seekbar value
        moistureBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                String value = String.valueOf(" " + moistureBar.getProgress()) + " %";
                moistureValue.setText( value );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        //Configure save button's on click event
        s_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Get and send goal moisture level
                SeekBar bar = (SeekBar) findViewById(R.id.moiMoistureBar);
                netLock.lock();
                try {
                    m.setAttr("m", bar.getProgress());
                } catch (Exception e) {
                    noConnection();
                } finally {
                    netLock.unlock();
                }

                exitAct();
            }
        });


        //Configure cancel button's on click event
        c_button.setOnClickListener(new View.OnClickListener() {
            //Return to start menu
            public void onClick(View v) {
                exitAct();
            }
        });
    }

    //Called when we wish to return to start menu
    protected void exitAct() {
        if(!hasQuit.get()) {
            hasQuit.set(true);

            //Stop timed task and its thread
            timerHandler.removeCallbacksAndMessages(null);
            timerThread.quit();


            //Start main menu, and stop current activity
            Intent ex_int = new Intent(this, AutoMenu.class );
            startActivity(ex_int);
            this.finish();
        }


    }

    //Called when connection is lost
    private void noConnection() {

        //Ensure the exit code only gets called once
        if(!hasQuit.get()) {
            hasQuit.set(true);

            //Stop timed task and its thread
            timerHandler.removeCallbacksAndMessages(null);
            timerThread.quit();

            //Start main menu, and stop current activity
            Intent noConInt = new Intent(this, NoConnectionScreen.class);
            startActivity(noConInt);
            this.finish();
        }
    }
}