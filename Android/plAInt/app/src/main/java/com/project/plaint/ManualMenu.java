package com.project.plaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@RequiresApi(api = Build.VERSION_CODES.O)
public class ManualMenu extends AppCompatActivity {

    //Global variables
    Model m;
    AtomicBoolean hasQuit;
    Lock netLock = new ReentrantLock();
    HandlerThread timerThread;


    //Handler, and runnable run by it
    Handler timerHandler;
    Runnable timerTask = new Runnable() {
        @Override
        public void run() {

            //Make variables for screen objects
            ProgressBar waterBar = (ProgressBar) findViewById(R.id.manWaterBar);

            ProgressBar moiBar = (ProgressBar) findViewById(R.id.manMoistureBar);
            TextView moiVal = (TextView) findViewById(R.id.manMoistureValue);

            ProgressBar humBar = (ProgressBar) findViewById(R.id.manHumidityBar);
            TextView humVal = (TextView) findViewById(R.id.manHumidityValue);

            ProgressBar tempBar = (ProgressBar) findViewById(R.id.manTemperatureBar);
            TextView tempVal = (TextView) findViewById(R.id.manTemperatureValue);


            //Get current water level, set corresponding bar
            netLock.lock();
            try {
                waterBar.setProgress(m.getVal("w"));
            } catch (Exception e) {
                noConnection();
            } finally {
                netLock.unlock();
            }


            //Get current moisture level, set corresponding bar and value
            int moi = 0;
            netLock.lock();
            try {
                moi = m.getVal("m");
            } catch (Exception e) {
                noConnection();
            } finally {
                netLock.unlock();
            }
            moiBar.setProgress(moi);
            String targetText = " " + String.valueOf(moi) + " %";
            moiVal.setText(targetText);


            //Get current humidity value, set corresponding bar and value
            int hum = 0;
            netLock.lock();
            try {
                hum = m.getVal("h");
            } catch (Exception e) {
                noConnection();
            } finally {
                netLock.unlock();
            }
            humBar.setProgress(hum);
            targetText = " " + String.valueOf(hum) + " %";
            humVal.setText(targetText);


            //Get current temperature, set corresponding bar and value
            int temp = 0;
            netLock.lock();
            try {
                temp = m.getVal("t");
            } catch (Exception e) {
                noConnection();
            } finally {
                netLock.unlock();
            }
            tempBar.setProgress(temp);
            targetText = " " + String.valueOf(temp) + " °C";
            tempVal.setText(targetText);

            timerHandler.postDelayed(timerTask, 3000);
        }
    };





    //Method called when entering the screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_menu);

        //Get global instance of model
        m = Model.getInstance();

        //Initialize bool tracking quitting
        hasQuit = new AtomicBoolean(false);

        //Initialize thread for running timed task
        timerThread = new HandlerThread("Tick Thread");
        timerThread.start();

        //Initialize handler on timer thread, start timed runnable
        timerHandler = new Handler(timerThread.getLooper());
        timerHandler.post(timerTask);




        //Create variables for displayed switches
        SwitchCompat moiSwitch = (SwitchCompat) findViewById(R.id.manMoistureSwitch);
        SwitchCompat humSwitch = (SwitchCompat) findViewById(R.id.manHumiditySwitch);
        SwitchCompat tempSwitch = (SwitchCompat) findViewById(R.id.manTemperatureSwitch);
        SwitchCompat lightSwitch = (SwitchCompat) findViewById(R.id.manLightSwitch);
        SwitchCompat fanSwitch = (SwitchCompat) findViewById(R.id.manFanSwitch);

        //Query and set switch states
        netLock.lock();
        try {
            moiSwitch.setChecked(m.queryMan("m"));
            humSwitch.setChecked(m.queryMan("h"));
            tempSwitch.setChecked(m.queryMan("t"));
            lightSwitch.setChecked(m.queryMan("l"));
            fanSwitch.setChecked(m.queryMan("f"));
        } catch (Exception e) {
            noConnection();
        } finally {
            netLock.unlock();
        }



        //Set events for each switch changing

        moiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Toggle watering module with communication class
                netLock.lock();
                try {
                    m.toggleMan("m", isChecked);
                } catch (Exception e) {
                    noConnection();
                } finally {
                    netLock.unlock();
                }
            }
        });

        humSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Toggle humidifier with communication class
                netLock.lock();
                try {
                    m.toggleMan("h", isChecked);
                } catch (Exception e) {
                    noConnection();
                } finally {
                    netLock.unlock();
                }
            }
        });

        tempSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Toggle heating module with communication class
                netLock.lock();
                try {
                    m.toggleMan("t", isChecked);
                } catch (Exception e) {
                    noConnection();
                } finally {
                    netLock.unlock();
                }
            }
        });

        lightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Toggle lights with communication class
                netLock.lock();
                try {
                    m.toggleMan("l", isChecked);
                } catch (Exception e) {
                    noConnection();
                } finally {
                    netLock.unlock();
                }
            }
        });

        fanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Toggle fans with communication class
                netLock.lock();
                try {
                    m.toggleMan("f", isChecked);
                } catch (Exception e) {
                    noConnection();
                } finally {
                    netLock.unlock();
                }
            }
        });



        //Set auto menu button press event
        Button s_button = (Button) findViewById(R.id.manAutoButton);
        s_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                netLock.lock();
                try {
                    m.switchMode(false);

                } catch (Exception e) {
                    noConnection();
                } finally {
                    netLock.unlock();
                }

                if (!hasQuit.get()) {
                    hasQuit.set(true);

                    timerHandler.removeCallbacksAndMessages(null);
                    timerThread.quit();

                    Intent ex_int = new Intent(ManualMenu.this, AutoMenu.class );
                    startActivity(ex_int);
                    ManualMenu.this.finish();
                }

            }
        });
    }

    //Method called upon losing connection
    private void noConnection() {
        //Ensure the closing code gets called only once
        if (!hasQuit.get()) {
            hasQuit.set(true);

            timerHandler.removeCallbacksAndMessages(null);
            timerThread.quit();

            Intent ex_int = new Intent(this, NoConnectionScreen.class );
            startActivity(ex_int);
            ManualMenu.this.finish();
        }

    }
}