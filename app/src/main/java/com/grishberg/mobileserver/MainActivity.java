package com.grishberg.mobileserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.grishberg.mobileserver.data.service.HttpService;
import com.grishberg.mobileserver.framework.BaseBinderActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends BaseBinderActivity {
    private TextView tvCaption;
    private ListView lvList;
    private int mCounter;
    ArrayList<HashMap<String, String>> mData = new ArrayList<HashMap<String, String>>();
    private SimpleAdapter mAdapter;

    @Override
    protected Intent getServiceIntent() {
        return new Intent(this, HttpService.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        tvCaption = (TextView) findViewById(R.id.tvCaption);
        lvList = (ListView) findViewById(R.id.lvList);
        mAdapter = new SimpleAdapter(this, mData, android.R.layout.simple_list_item_2,
                new String[] {"date", "event"},
                new int[] {android.R.id.text1, android.R.id.text2});
        lvList.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * start tasks
     */
    @Override
    protected void onFirstBound() {
        super.onFirstBound();
        mService.startServer();
    }

    @Override
    protected void onBound() {
        super.onBound();
        String[] resp = mService.getResponseList();
        if(resp.length > 0){
            for(String s: resp){
                HashMap<String, String> map = new HashMap<>();
                map = new HashMap<String, String>();
                map.put("date", s);
                map.put("event", "new event");
                mData.add(map);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onTaskDone(String tag, int taskId, int code) {
        super.onTaskDone(tag, taskId, code);
        Toast.makeText(getApplicationContext(), "on response", Toast.LENGTH_SHORT).show();
        mCounter ++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mService != null){
            mService.stopService();
        }
    }
}
