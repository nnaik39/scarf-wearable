package com.example.magulo.scarf_wearable;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {
    private ImageView nyan_cat;
    //to rename vars go to refactor -> rename
    private Button showCat;
    private Button setup_2;
    private ImageView imageView;
    private Button button_1;
//    private Button setup_3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        imageView = (ImageView) findViewById(R.id.imageView);
//       showCat = (Button) findViewById(R.id.button_1);
//       showCat = (Button) findViewById(R.id.showCat);
//        nyan_cat = (ImageView) findViewById(R.id.imageView);
        setup_2 = (Button) findViewById(R.id.button_1);
        setup_2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, TempSetter.class);
                    startActivity(intent);
            }
        });
//        showCat.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (nyan_cat.isShown() == true)
//                    nyan_cat.setVisibility(View.INVISIBLE);
//                else
//                    nyan_cat.setVisibility(View.VISIBLE);
//            }
//        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
