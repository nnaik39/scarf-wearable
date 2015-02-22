package com.example.magulo.scarf_wearable;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.graphics.Color;
import android.app.Activity;
import android.widget.ImageView;

import java.util.Random;

public class TempSetter extends ActionBarActivity {
    private Button next;
    private ImageView tempView;
    int tempInput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_setter);
        Random rand = new Random();
        tempInput = rand.nextInt(200) + 1;
        tempView = (ImageView) findViewById(R.id.tempView);
//        tempInput = (Integer) 200;
        if (100 < tempInput) {
            tempView.animate().scaleYBy(1.5f).setDuration(5000); }
//        setContentView(R.layout.activity_temp_setter);
                next = (Button) findViewById(R.id.next);
                next.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent_1 = new Intent(TempSetter.this, Contacts.class);
                        startActivity(intent_1);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_temp_setter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
