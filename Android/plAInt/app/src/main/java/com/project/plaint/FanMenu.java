package com.project.plaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

@RequiresApi(api = Build.VERSION_CODES.O)
public class FanMenu extends AppCompatActivity {

    Model m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_menu);

        //Get global communication class instance
        m = Model.getInstance();

        //Create variables for screen objects
        TimePicker startTime = findViewById(R.id.fanStartTime);
        TimePicker endTime = findViewById(R.id.fanEndTime);
        Button s_button = (Button) findViewById(R.id.fanSaveButton);
        Button c_button = (Button) findViewById(R.id.fanCancelButton);

        //Set time pickers' display mode
        startTime.setIs24HourView(true);
        endTime.setIs24HourView(true);

        //Get current start and end times
        int start = 0;
        int end = 0;
        try {
            start = m.getAttr("fs");
            end = m.getAttr("fe");
        } catch (Exception e) {
            noConnection();
        }


        //Convert and set the received times
        startTime.setHour(start/3600);
        startTime.setMinute((start%3600)/60);
        endTime.setHour(end/3600);
        endTime.setMinute((end%3600)/60);


        //Configure save button's on click event
        s_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Set the configured times
                try {
                    m.setAttr("fs", startTime.getHour() * 3600 + startTime.getMinute() * 60 );
                    m.setAttr("fe", endTime.getHour() * 3600 + endTime.getMinute() * 60 );
                } catch (Exception e) {
                    noConnection();
                }

                //Return to main menu
                exitAct();
            }
        });

        //Configure cancel button's on click event
        c_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                exitAct();
            }
        });

    }


    //Called when returning to main menu
    protected void exitAct() {
        //Start main menu, and stop current activity
        Intent ex_int = new Intent(this, AutoMenu.class );
        startActivity(ex_int);
        this.finish();
    }

    //Called when connection is lost
    private void noConnection() {
        //Start no connection screen, and stop current activity
        Intent noConInt = new Intent(this, NoConnectionScreen.class);
        startActivity(noConInt);
        this.finish();
    }
}