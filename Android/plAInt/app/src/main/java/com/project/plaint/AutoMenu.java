package com.project.plaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;


@RequiresApi(api = Build.VERSION_CODES.O)
public class AutoMenu extends AppCompatActivity {

    //Global variables
    Model m;


    //Method called when entering the screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_menu);

        //Create global model instance, if it doesn't exist
        if (Model.getInstance() == null) {
            try {
                Model.setInstance(new Model("192.168.100.40", 12345));
            } catch (Exception e) {
                noConnection();
            }
        }
        m = Model.getInstance();


        //Change to manual menu if manual control is enabled
        boolean isManual = false;
        try {
            isManual = m.queryMan("c");
        } catch (Exception e) {
            noConnection();
        }
        if (isManual) {
            Intent man_int = new Intent(AutoMenu.this, ManualMenu.class);
            startActivity(man_int);
            this.finish();
        }


        //Move to moisture menu upon button pressed
        ImageView m_button = findViewById(R.id.mainMoistureButton);
        m_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent moistInt = new Intent(AutoMenu.this, MoistureMenu.class);
                startActivity(moistInt);
                AutoMenu.this.finish();
            }
        });

        //Move to humidity menu upon button pressed
        ImageView h_button = findViewById(R.id.mainHumidityButton);
        h_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent humidInt = new Intent(AutoMenu.this, HumidityMenu.class);
                startActivity(humidInt);
                AutoMenu.this.finish();
            }
        });

        //Move to temperature menu upon button pressed
        ImageView t_button = findViewById(R.id.mainTempButton);
        t_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent tempInt = new Intent(AutoMenu.this, TempMenu.class);
                startActivity(tempInt);
                AutoMenu.this.finish();
            }
        });

        //Move to light menu upon button pressed
        ImageView l_button = findViewById(R.id.mainLightButton);
        l_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent lighInt = new Intent(AutoMenu.this, LightMenu.class);
                startActivity(lighInt);
                AutoMenu.this.finish();
            }
        });

        //Move to fan menu upon button pressed
        ImageView f_button = findViewById(R.id.mainFanButton);
        f_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent fanInt = new Intent(AutoMenu.this, FanMenu.class);
                startActivity(fanInt);
                AutoMenu.this.finish();
            }
        });

        //Move to manual menu upon button pressed
        ImageView man_button = findViewById(R.id.mainManualButton);
        man_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    m.switchMode(true);
                } catch (Exception e) {
                    noConnection();
                }
                Intent manInt = new Intent(AutoMenu.this, ManualMenu.class);
                startActivity(manInt);
                AutoMenu.this.finish();
            }
        });

    }

    //Change to no connection screen if connection is lost
    private void noConnection() {
        Intent noConInt = new Intent(this, NoConnectionScreen.class);
        startActivity(noConInt);
        this.finish();
    }





}