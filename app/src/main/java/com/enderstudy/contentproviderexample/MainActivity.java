package com.enderstudy.contentproviderexample;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

// Static import, nifty Java feature allowing importing of only static members from a class
import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView contactNames;
    private static final int REQUEST_CODE_READ_CONTACTS = 1;
    private static boolean READ_CONTACTS_GRANTED = false;
    FloatingActionButton fab = null; // No scope so we can access from inner classes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contactNames = (ListView) findViewById(R.id.contact_names);

        // Check to see if we have permission to read contacts, uses appCompat v4 because we're targeting jellybean (4.2, api 17)
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasReadContactPermission);

        if(hasReadContactPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: permission granted");
            READ_CONTACTS_GRANTED = true;
        } else {
            Log.d(TAG, "onCreate: requesting permission");

            // Permissions being requested must be an array because it's possible to request/receive multiple permissions at the same time.
            ActivityCompat.requestPermissions(this, new String[]{READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "fab onClick: starts");

                if(READ_CONTACTS_GRANTED) {
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
                    if (cursor != null) {
                        List<String> contacts = new ArrayList<String>();
                        while (cursor.moveToNext()) {
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
                } else {
                    Snackbar.make(view, "Please grant permission to access contacts", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
        Log.d(TAG, "onCreate: ends");
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: starts");
        switch(requestCode) {
            case REQUEST_CODE_READ_CONTACTS: {
                // If the request was cancelled, the rest arrays are empty
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted by the user
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    READ_CONTACTS_GRANTED = true;
                } else {
                    // Permission was denied by the user, disable relevant functionality
                    Log.d(TAG, "onRequestPermissionsResult: permission denied");
                }
            }
        }

        Log.d(TAG, "onRequestPermissionsResult: ends");
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
