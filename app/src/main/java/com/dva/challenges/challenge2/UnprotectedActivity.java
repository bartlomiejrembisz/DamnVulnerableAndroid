package com.dva.challenges.challenge2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dva.challenges.DvaApplication;
import com.dva.challenges.MainActivity;
import com.dva.challenges.R;

public class UnprotectedActivity extends AppCompatActivity
{
    static final String TAG = UnprotectedActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unprotected);

        Log.d(TAG, "Challenge 2 passed.");
        ((DvaApplication) getApplication()).setChallengeStatus(2, true);
        Intent intent = new Intent(UnprotectedActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
        finish();
    }
}