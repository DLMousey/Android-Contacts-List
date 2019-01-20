package com.enderstudy.contentproviderexample;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    FloatingActionButton fab = null; // No scope so we can access from inner classes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        contactNames = (ListView) findViewById(R.id.contact_names);

        // Check to see if we have permission to read contacts, uses appCompat v4 because we're targeting jellybean (4.2, api 17)
        int hasReadContactPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS);
        if(hasReadContactPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    String[] projection = {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY};

                    /*
                        Use a content resolver that lets us get data from other apps, in this case
                        it lets us retrieve contact data from the device. Content resolver is
                        generic, it has access to all content providers registered on the device,
                        we just need to give it a hint (the content_uri for contacts) of which
                        providers it should hit up for data
                    */
                    ContentResolver contentResolver = getContentResolver();
                    Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                            projection, // List of columns to retrieve
                            null, // A filter of which rows to return (eg. SQL WHERE), null so get all
                            null, // Selection args, param binding
                            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY); // Sort order

                    if (cursor != null) { // No guarantee the resolver will return data, must sanity check
                        List<String> contacts = new ArrayList<String>();
                        while (cursor.moveToNext()) {
                            contacts.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)));
                        }

                        cursor.close(); // Got our data, close the cursor to save memory

                        // Create an array adapter so we can plug contactNames into the view
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                MainActivity.this, R.layout.contact_detail, R.id.name, contacts
                        );

                        // Plug the adapter into the contactNames view
                        contactNames.setAdapter(adapter);
                    }
                } else {
                    Snackbar.make(view, "This app can't display your contact records unless you...", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Grant Access", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, READ_CONTACTS)) {
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_CONTACTS}, REQUEST_CODE_READ_CONTACTS);
                                } else {
                                    // User has checked the "Don't ask again" box but keeps hitting the button
                                    Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);

                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri);
                                    MainActivity.this.startActivity(intent);
                                }
                            }
                        }
                    ).show();
                }
            }
        });
    }
}
