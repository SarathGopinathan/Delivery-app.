package com.example.shadesix.w2d;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shadesix.w2d.Fragments.FragmentDelivered;
import com.example.shadesix.w2d.Fragments.FragmentProfile;
import com.example.shadesix.w2d.Fragments.FragmentRemaining;
import com.example.shadesix.w2d.Fragments.FragmentRoute_Map;
import com.example.shadesix.w2d.Utils.Constant;
import com.example.shadesix.w2d.Utils.GPSTracker;
import com.example.shadesix.w2d.Utils.Utilities;

import java.util.ArrayList;
import java.util.List;

public class   LandingActivity extends AppCompatActivity {
    TabLayout tabLayout;
    public static ViewPager viewPager;
    TextView capacity;

    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.fragment);
        capacity = (TextView) findViewById(R.id.txt_toolbar);

        capacity.setText(getResources().getText(R.string.capacity)+" "+
                Utilities.getFromeUserDefault(LandingActivity.this, Constant.VEHICLE_CAPACITY)+" cans");

        Utilities.saveToUserDefault(LandingActivity.this,Constant.CURRENT_DELIV_STATUS,"0");

        addViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        checkLocationPermission();
    }

    private void addViewPager(ViewPager viewPager) {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new FragmentRemaining(),"Pending");
        adapter.addFragment(new FragmentRoute_Map(),"Route Map");
        adapter.addFragment(new FragmentDelivered(),"Delivered");
        adapter.addFragment(new FragmentProfile(),"Profile");

        viewPager.setAdapter(adapter);
    }

    public class SectionPagerAdapter extends FragmentPagerAdapter{

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentNameList = new ArrayList<>();

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment,String title){
            fragmentList.add(fragment);
            fragmentNameList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentNameList.get(position);
        }
    }
    //gets location permission from the user
    public void checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(LandingActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(LandingActivity.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LandingActivity.this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LandingActivity.this);
                builder.setTitle("Need Location Permission");
                builder.setMessage("W2D needs Location Permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LandingActivity.this,
                                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                100);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();

            } else if (permissionStatus.getBoolean(android.Manifest.permission.ACCESS_COARSE_LOCATION,false)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LandingActivity.this);
                builder.setTitle("Need Location Permission");
                builder.setMessage("W2Delivery needs Location Permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 101);
                        Toast.makeText(LandingActivity.this, "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(LandingActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(android.Manifest.permission.ACCESS_COARSE_LOCATION,true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
            turnGPS();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (ActivityCompat.checkSelfPermission(LandingActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                turnGPS();
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(LandingActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                turnGPS();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                turnGPS();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(LandingActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Show Information about why you need the permission

                    AlertDialog.Builder builder = new AlertDialog.Builder(LandingActivity.this);
                    builder.setTitle("Need Location Permission");
                    builder.setMessage("Legal Guru needs Location Permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(LandingActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 100);

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void turnGPS(){
        GPSTracker gps = new GPSTracker(LandingActivity.this);

        if(!gps.canGetLocation()){
            gps.showSettingsAlert();
        }
    }

    @Override
    public void onBackPressed() {

        //back pressed moves to the previous fragment
        if(viewPager.getCurrentItem() == 0)
            super.onBackPressed();
        else
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
    }
}
