package com.huto.pynpoint.pynpoint;

import java.util.Calendar;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;


public class MainActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver = new AddressResultReceiver(null);
    private String mAddressOutput = null;
    private TextView Output;
    private boolean recording = false;


    static private DbPynpoint local_db;
    static private MainActivity main;
    static private GoogleMap mMap;
    static boolean googleApiConnected = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        local_db = new DbPynpoint(getBaseContext());
        main = this;

        buildGoogleApiClient();

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
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
    private void displayInfo(String msg){
        if (Output == null){
            Log.i("Display", "Is null, recreate");
            Output = (TextView) findViewById(R.id.textView);
            if (Output == null)
                return;
        }
        Output.setText(msg);
    }

    class UpdateUI implements Runnable
    {
        String updateString;

        public UpdateUI(String updateString) {
            this.updateString = updateString;
        }
        public void run() {
            displayInfo(updateString);
        }
    }
    //Record position Button
    public void buttonRecord(View view) {

        Log.i("Button", "Click");
        if(!googleApiConnected) {
            displayInfo("Google Api not connected");
        }
        else{
            if(!recording) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                Log.i("Oupsi", String.valueOf(mLastLocation.getTime()));
                Log.i("Oupsi", String.valueOf(mLastLocation.getLongitude()));
                Intent intent = new Intent(this, FetchAddressIntentService.class);
                intent.putExtra(Constants.RECEIVER, mResultReceiver);
                intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
                startService(intent);
                recording = true;
                displayInfo("Looking up the address");
            }
            else{
                displayInfo("Already Recording");
            }
        }
    }

    public void showBtn(View view){
        ListView lv = (ListView) findViewById(R.id.listView);
        showEntries(lv);
    }

    static public void showEntries(ListView lv){
        SQLiteDatabase db = local_db.getReadableDatabase();
        String[] projection = {
                LocationRecord.Record._ID,
                LocationRecord.Record.COLUMN_NAME_ADDRESS,
                LocationRecord.Record.COLUMN_NAME_TIME
        };
        Cursor c = db.query(
                LocationRecord.TABLE_NAME,
                projection, null, null,
                null, null, null);

        int count_item = c.getCount();
        if (lv == null){
            Log.i("Show entries", "LV is null");
            return;
        }

        CursorAdapter ca = new CursorAdapter(main, c) {
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context).inflate(R.layout.item_record, parent, false);
            }

            public void bindView(View view, Context context, Cursor cursor) {
                // Find fields to populate in inflated template
                TextView tvId = (TextView) view.findViewById(R.id.record_id);
                TextView tvAddress = (TextView) view.findViewById(R.id.address);
                TextView tvTime = (TextView) view.findViewById(R.id.time);
                // Extract properties from cursor
                String address = cursor.getString(cursor.getColumnIndexOrThrow(LocationRecord.Record.COLUMN_NAME_ADDRESS));
                long rid = cursor.getLong(cursor.getColumnIndexOrThrow(LocationRecord.Record._ID));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(LocationRecord.Record.COLUMN_NAME_TIME));
                // Populate fields with extracted properties
                tvAddress.setText(address);
                tvId.setText(String.valueOf(rid));
                tvTime.setText(time);
            }
        };
        lv.setAdapter(ca);
    }

    public void dropTable(View view){
        SQLiteDatabase db = local_db.getReadableDatabase();
        local_db.resetTable(db);
    }

    /*********************************************
    /*      Google Api connection management     *
    /*********************************************/
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    public void onConnected(Bundle bundle) {
        googleApiConnected = true;
    }
    public void onConnectionSuspended(int i) {
        Log.i("Google API", "Connection suspend");
    }
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("Google API", "Connection failed ");
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        //Constructor
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        //Return the page position
        public Fragment getItem(int position) {
            return PlaceholderFragment.newInstance(position + 1);
        }
        // Number of pages
        public int getCount() {
            return 3;
        }

        //Give the page title
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        //Constant
        private static final String ARG_SECTION_NUMBER = "section_number";
        //Create a new page for the section number
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            Log.i("Fragment", String.valueOf(sectionNumber));
            fragment.setArguments(args);
            return fragment;
        }
        //Empty constructor
        public PlaceholderFragment() {}
        //Inflate the active layout for the given fram
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Bundle args = this.getArguments();
            int sectionNumber = args.getInt(ARG_SECTION_NUMBER);
            //Display map in first page
            if (sectionNumber == 1){
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_maps);
                View rootView = inflater.inflate(R.layout.activity_maps, container, false);
                SupportMapFragment map = (SupportMapFragment) rootView.findViewById(R.id.map);
                if(map ==null){
                    Log.i("MAp??", "map is null");
                }
                else {
                    setUpMapIfNeeded(map);
                }
                return rootView;
            }
            //Display recorder in second page
            if (sectionNumber == 2){
                View rootView = inflater.inflate(R.layout.fragment_main, container, false);
                return rootView;
            }
            View rootView = inflater.inflate(R.layout.fragment_show, container, false);
            ListView lv = (ListView) rootView.findViewById(R.id.LVshow);
            showEntries(lv);
            return rootView;

        }

        private void setUpMapIfNeeded(SupportMapFragment map) {
            // Do a null check to confirm that we have not already instantiated the map.
            if (mMap == null) {
                // Try to obtain the map from the SupportMapFragment.
                mMap = map.getMap();
                // Check if we were successful in obtaining the map.
                if (mMap != null) {
                    setUpMap();
                }
            }
        }

        private void setUpMap() {
            mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        }


    }


    /***********************************************
    /* Receive the result of the address lookup    *
    /***********************************************/
    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i("Button", "Address success");

            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.i("Address", "Success!!!");
                SQLiteDatabase db = local_db.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(LocationRecord.Record.COLUMN_NAME_ENTRY_ID, 0);
                values.put(LocationRecord.Record.COLUMN_NAME_ADDRESS, mAddressOutput);
                values.put(LocationRecord.Record.COLUMN_NAME_LATITUDE, mLastLocation.getLatitude());
                values.put(LocationRecord.Record.COLUMN_NAME_LONGITUDE, mLastLocation.getLongitude());
                Calendar c = Calendar.getInstance();
                values.put(LocationRecord.Record.COLUMN_NAME_TIME, System.currentTimeMillis()/1000);
                values.put(LocationRecord.Record.COLUMN_NAME_TYPE, "Unknown");

                long newRowId;
                newRowId = db.insert(
                        LocationRecord.TABLE_NAME,
                        LocationRecord.COLUMN_NAME_NULLABLE,
                        values);
                Log.i("DB insert", String.valueOf(newRowId));
            }
            runOnUiThread(new UpdateUI(mAddressOutput));
            recording = false;

        }
    }

}
