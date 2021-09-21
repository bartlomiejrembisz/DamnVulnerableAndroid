package com.dva.challenges.challenge4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dva.challenges.DvaApplication;
import com.dva.challenges.R;
import com.dva.challenges.challenge1.ArbitraryCodeExecutionActivity;

public class VulnerableBroadcastReceiverActivity extends AppCompatActivity implements DvaApplication.IStatusListener
{
    private static final String TAG = ArbitraryCodeExecutionActivity.class.getSimpleName();

    private TextView mStatus = null;
    DvaApplication mApplication = null;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            try
            {
                Log.d(TAG, "Broadcast receiver from application=" + context.getApplicationInfo().name);
                Log.d(TAG, "Intent action=" + intent.getAction());
                if (intent.getAction().equalsIgnoreCase(getString(R.string.vulnerable_broadcast_receiver_ACTION_PASS)))
                {
                    Log.d(TAG, "Challenge 5 passed.");
                    mApplication.setChallengeStatus(4, true);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vulnerable_broadcast_receiver);

        mStatus = (TextView) findViewById(R.id.vulnerable_receiver_status);

        mApplication = (DvaApplication) getApplication();
        mApplication.registerChallengeStatusListener(4,  this);
        setStatus(mApplication.getChallengeStatus(4));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.vulnerable_broadcast_receiver_ACTION_PASS));
        intentFilter.addAction(getString(R.string.vulnerable_broadcast_receiver_ACTION_RESET));
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        setStatus(mApplication.getChallengeStatus(4));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mApplication.unregisterChallengeStatusListener(4);

        unregisterReceiver(mBroadcastReceiver);
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