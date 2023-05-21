package com.project.plaint;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NoConnectionScreen extends AppCompatActivity {

    //Global variables
    Model m;

    //Method called on entering the screen
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_connection_screen);

        //Set retry button's on pressed event
        Button s_button = (Button) findViewById(R.id.noconRetryButton);
        s_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Try to establish new connection
                try {
                    Model.setInstance(new Model("192.168.100.40", 12345));
                    m = Model.getInstance();
                }
                catch (Exception e) {
                    return;
                }

                //On successful connection go to appropriate menu
                boolean isManual = false;
                try {
                    isManual = m.queryMan("c");
                } catch (Exception e) {
                    return;
                }

                if (isManual) {
                    Intent man_int = new Intent(NoConnectionScreen.this, ManualMenu.class);
                    startActivity(man_int);
                    NoConnectionScreen.this.finish();
                }

                else {
                    Intent ex_int = new Intent(NoConnectionScreen.this, AutoMenu.class );
                    startActivity(ex_int);
                    NoConnectionScreen.this.finish();
                }
            }
        });
    }
}