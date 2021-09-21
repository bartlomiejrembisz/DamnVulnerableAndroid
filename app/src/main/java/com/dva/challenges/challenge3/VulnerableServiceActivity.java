package com.dva.challenges.challenge3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dva.challenges.DvaApplication;
import com.dva.challenges.R;

public class VulnerableServiceActivity extends AppCompatActivity implements DvaApplication.IStatusListener {
    private static final String TAG = VulnerableServiceActivity.class.getSimpleName();
    private Intent passwordServiceIntent;
    private TextView mStatus = null;

    private ComponentName serviceComponentName = null;

    IVulnerableService mVulnerableService = null;

    DvaApplication mApplication = null;

    ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            mVulnerableService = IVulnerableService.Stub.asInterface(iBinder);
            Log.d(TAG, "Vulnerable service connected");

            try
            {
                setStatus(mVulnerableService.hasPassed());
            }
            catch (RemoteException e)
            {
                setStatus(false);
                Log.e(TAG, "HasPassed failed. " + e);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mVulnerableService = null;
            Log.d(TAG, "Vulnerable service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vulnerable_service);

        Intent intent = new Intent(getApplicationContext(), VulnerableService.class);

        boolean ret = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Service bind result=" + ret);

        mApplication = (DvaApplication) getApplication();
        mApplication.registerChallengeStatusListener(3,  this);

        mStatus = (TextView) findViewById(R.id.vulnerable_service_status);
        setStatus(mApplication.getChallengeStatus(3));
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        try
        {
            if (null != mVulnerableService)
                setStatus(mVulnerableService.hasPassed());
            else
                setStatus(mApplication.getChallengeStatus(3));
        }
        catch (RemoteException e)
        {
            Log.e(TAG, "Set status failed, ", e);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unbindService(mConnection);

        mApplication.unregisterChallengeStatusListener(3);
    }

    private void setStatus(boolean hasPassed)
    {
        String status = hasPassed ? "PASSED" : "NOT PASSED";
        int color = hasPassed ? Color.GREEN : Color.RED;

        mStatus.setText(status);
        mStatus.setTextColor(color);
    }

    @Override
    public void onStatusChange(boolean status)
    {
        setStatus(status);
    }
}