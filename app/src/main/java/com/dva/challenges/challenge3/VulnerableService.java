package com.dva.challenges.challenge3;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dva.challenges.DvaApplication;
import com.dva.challenges.MainActivity;

public class VulnerableService extends Service
{
    private static final String TAG = VulnerableService.class.getSimpleName();
    DvaApplication mApplication = null;

    //! Binder service object.
    private IBinder binder = new IVulnerableService.Stub()
    {
        @Override
        public void pass() throws RemoteException
        {
            //! Pass the challenge.
            mApplication.setChallengeStatus(3, true);
        }

        @Override
        public boolean hasPassed() throws RemoteException
        {
            //! Check whether the challenge has passed.
            return mApplication.getChallengeStatus(3);
        }

        @Override
        public void reset() throws RemoteException
        {
            //! Reset the challenge status. Only available if local MODIFY_PERMISSION permission is granted.
            if (PackageManager.PERMISSION_GRANTED != checkCallingOrSelfPermission(MainActivity.MODIFY_PERMISSION))
                throw new SecurityException("Permission " + MainActivity.MODIFY_PERMISSION + " not granted.");

            mApplication.setChallengeStatus(3, false);
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();

        mApplication = (DvaApplication) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        ComponentName componentName = intent.getComponent();
        if (null != componentName)
            Log.i(TAG, "Service started, component=" + componentName.getShortClassName());
        else
            Log.i(TAG, "Service started.");

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        Log.i(TAG, "Service stopped");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }
}
