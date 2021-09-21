package com.dva.challenges;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;

public class DvaApplication extends Application
{
    private static final String TAG = DvaApplication.class.getSimpleName();

    public interface IStatusListener
    {
        void onStatusChange(boolean status);
    };

    private HashMap<Integer, IStatusListener> mStatusCallback = null;
    private SharedPreferences mSharedPreferences = null;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mStatusCallback = new HashMap<>();

        mSharedPreferences = getSharedPreferences(getString(R.string.app_data_preference_file_name), Context.MODE_PRIVATE);
    }

    public void setChallengeStatus(int challengeId, boolean status)
    {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(String.valueOf(challengeId), status);
        editor.commit();

        IStatusListener listener = mStatusCallback.get(challengeId);
        if (null == listener)
            return;

        Log.i(TAG, "Challenge " + challengeId + " updated");

        listener.onStatusChange(getChallengeStatus(challengeId));
    }

    public boolean getChallengeStatus(int challengeId)
    {
        return mSharedPreferences.getBoolean(String.valueOf(challengeId), false);
    }

    public void resetChallenges()
    {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    public void registerChallengeStatusListener(int challengeId, IStatusListener listener)
    {
        mStatusCallback.put(challengeId, listener);
    }

    public void unregisterChallengeStatusListener(int challengeId)
    {
        mStatusCallback.remove(challengeId);
    }
}
