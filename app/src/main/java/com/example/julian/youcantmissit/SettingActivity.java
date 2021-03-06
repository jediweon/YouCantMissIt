/**
 * Initially Created by Julian on 2015-11-22.
 * Lastly modified by Julian on 2015-11-25.
 */

package com.example.julian.youcantmissit;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity {
    DBManager dbManager;
    ArrayList<LocationData> locationList;
    ListView listView;
    ItemAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbManager = DBManager.getInstance(getApplicationContext());
        locationList = dbManager.getAllLocation();

        listView = (ListView) findViewById(R.id.listView);
        adapter = new ItemAdapter(this, this.locationList);
        listView.setAdapter(adapter);
        listView.setLongClickable(true);

        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent childIntent = new Intent(SettingActivity.this, SearchLocationActivity.class);
                startActivityForResult(childIntent, 0);
                adapter.notifyDataSetChanged();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), LocationDetailActivity.class);
                intent.putExtra("lat", locationList.get(position).getLat());
                intent.putExtra("lng", locationList.get(position).getLng());
                intent.putExtra("name", locationList.get(position).getName());
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int pos = position;
                final int key = locationList.get(pos).getKey();
                AlertDialog.Builder adBuilder = new AlertDialog.Builder(getApplicationContext());
                adBuilder.setTitle("Removing Location");
                adBuilder
                        .setMessage("Are you sure?")
                        .setCancelable(false)
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbManager.delete(locationList.get(key));
                                locationList.remove(pos);
                                adapter.notifyDataSetChanged();
                            }
                        });

                return false;
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0&&resultCode==0) {
            int key;
            try {
                key = data.getExtras().getString("name").hashCode();
                Log.e("TAG", "Incomedata " + data.getExtras().getString("name") + " || " + data.getExtras().getFloat("lat") + " || " + data.getExtras().getFloat("lng"));
                LocationData newLocation = new LocationData(key, data.getExtras().getString("name"),data.getExtras().getFloat("lat"),data.getExtras().getFloat("lng"),1);
                this.dbManager.insert(newLocation);
            } catch(NullPointerException e) {
                //
            }
            adapter.notifyDataSetChanged();
            LocationService.updateTargetLocation();
        }
    }

    protected void onPause() {
        dbManager.updateActiv();
        super.onPause();
    }

    protected void onStop() {
        setResult(0);
        super.onStop();
    }
}
