package com.enderstudy.contentproviderexample;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView contactNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactNames = (ListView) findViewById(R.id.contact_names);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab onClick: starts");
                String[] projection = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};

                /*
                    Use a content resolver that lets us get data from other apps,
                    in this case it lets us retrieve contact data from the device.
                    Content resolver is generic, it has access to all content providers
                    registered on the device, we just need to give it a hint (the content_uri for contacts)
                    of which providers it should hit up for data
                */
                ContentResolver contentResolver = getContentResolver();
                Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                        projection, // List of columns to retrieve
                        null, // A filter of which rows to return (eg. SQL WHERE), null so get all
                        null, // Selection args, param binding
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY); // Sort order

                // No guarantee the resolver will return data, must sanity check
                if(cursor != null) {
                    List<String> contacts = new ArrayList<String>();
                    while(cursor.moveToNext()) {
                        contacts.add(
                            cursor.getString(
                                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                            )
                        );
                    }

                    cursor.close(); // Close the cursor to save memory

                    // Create an array adapter so we can plug contactNames into the view
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            MainActivity.this, R.layout.contact_detail, R.id.name, contacts
                    );

                    // Plug the adapter into the contactNames view
                    contactNames.setAdapter(adapter);
                    Log.d(TAG, "fab onClick: ends");
                }
            }
        });
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
