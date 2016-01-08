package com.grishberg.mobileserver.framework;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.grishberg.mobileserver.Const;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by g on 07.01.16.
 */
public class BaseBinderService extends Service {
    private static final String TAG = BaseBinderService.class.getSimpleName();
    private boolean mIsShutdowning;
    private boolean mIsBroadcasRegistered;
    private IntentFilter mLocalBroadcast;
    private int mBindersCount;
    private int mActivitiesCount;
    private ApiServiceBinder mBinder;
    private List<String> mResponseList;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");

    public BaseBinderService() {
        mBindersCount = 0;
        mBinder = new ApiServiceBinder();
        mResponseList = new ArrayList<>(5);
        Log.d(TAG, "BaseBinderService: "+this);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {
            int code = intent.getIntExtra(Const.EXTRA_TASK_CODE, -1);
            Log.d(TAG, "onReceive: code="+code);
            switch (code){
                case Const.EXTRA_CREATE_ACTIVITY_CODE:
                    mActivitiesCount++;
                    break;
                case Const.EXTRA_DESTROY_ACTIVITY_CODE:
                    mActivitiesCount--;
                    checkDestroyService();
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + this);
        unregisterBroadcast();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: " + this);
        mLocalBroadcast = new IntentFilter(Const.ACTIVITY_ACTION);
        registerBroadcast();
        mActivitiesCount = 1;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBindersCount--;
        if (mBindersCount == 0) {
            checkDestroyService();
        }
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        mBindersCount++;
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mBindersCount++;
        return mBinder;
    }

    private void registerBroadcast() {
        if (!mIsBroadcasRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mMessageReceiver, mLocalBroadcast);
            mIsBroadcasRegistered = true;
        }
    }

    private void unregisterBroadcast() {
        if (mIsBroadcasRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(
                    mMessageReceiver);
            mIsBroadcasRegistered = false;
        }
    }


    private void checkDestroyService(){
        if(mBindersCount == 0 && mActivitiesCount == 0){
            stopSelf();
        }
    }

    public String[] getResponseList() {
        String[] res = mResponseList.toArray(new String[mResponseList.size()]);
        mResponseList.clear();
        return res;
    }

    // service container for Activity
    public class ApiServiceBinder extends Binder {
        public BaseBinderService getService() {
            return BaseBinderService.this;
        }
    }


    /**
     * send message to activities
     */
    public void sendMessage(int code, int id) {
        sendMessage(Const.SERVICE_ACTION_TASK_DONE, code, null, id);
    }

    /**
     * send message to activities
     */
    public void sendMessage(String action, int code, int id) {
        sendMessage(action, code, null, id);
    }

    public void sendMessage(String action, int code, Serializable data, int id) {
        if(mBindersCount == 0){
            String resp = String.format("%s %d"
                    ,sdf.format(Calendar.getInstance().getTime())
                    ,code);
            mResponseList.add(resp);
            return;
        }

        Intent intent = new Intent(action);
        intent.putExtra(Const.EXTRA_TASK_SERIALIZABLE, data);
        intent.putExtra(Const.EXTRA_TASK_CODE, code);
        intent.putExtra(Const.EXTRA_TASK_ID, id);
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(intent);
    }

    public void sendMessageParcel(String action, int code, Parcelable data, int id) {
        Intent intent = new Intent(action);
        intent.putExtra(Const.EXTRA_TASK_PARCELABLE, data);
        intent.putExtra(Const.EXTRA_TASK_CODE, code);
        intent.putExtra(Const.EXTRA_TASK_ID, id);
        LocalBroadcastManager.getInstance(this).
                sendBroadcast(intent);
    }

}
