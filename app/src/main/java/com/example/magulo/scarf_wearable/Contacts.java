package com.example.magulo.scarf_wearable;

import android.Manifest;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.view.View;
import android.widget.AdapterView;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Contacts extends ActionBarActivity {
    private Button button;
    private Button save_pref;
    String phoneNumber;
    EditText phone_input;
    private SharedPreferences shared_pref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        phone_input = (EditText)findViewById(R.id.phone_input);
        save_pref = (Button) findViewById(R.id.button2);
        shared_pref = getApplicationContext().getSharedPreferences(
                "phone_storage", Context.MODE_PRIVATE);
        phone_input.setText(shared_pref.getString("phone_number", "not entered"));
        save_pref.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                phoneNumber = phone_input.getText().toString();
                editor = shared_pref.edit();
                editor.putString("phone_number", phoneNumber);

                editor.commit();
                if (phoneNumber.equals("1235550199")) {
                    Toast.makeText(getApplicationContext(), "Please enter a phone number", Toast.LENGTH_LONG).show();
                }
            }
        });
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + shared_pref.getString("phone_number", phoneNumber)));//area code and number, no spaces
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
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
