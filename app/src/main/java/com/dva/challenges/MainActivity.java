package com.dva.challenges;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dva.challenges.challenge1.ArbitraryCodeExecutionActivity;
import com.dva.challenges.challenge3.VulnerableServiceActivity;
import com.dva.challenges.challenge4.VulnerableBroadcastReceiverActivity;
import com.dva.challenges.challenge5.MemoryDumpActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String MODIFY_PERMISSION = "com.dva.challenges.permission.MODIFY";

    private static final String TAG = MainActivity.class.getSimpleName();

    private DvaApplication mDvaApplication = null;

    private ActivityResultLauncher<Intent> mChallengeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                }
            });

    Button mChallenge1Button = null;
    Button mChallenge2Button = null;
    Button mChallenge3Button = null;
    Button mChallenge4Button = null;
    Button mChallenge5Button = null;

    Button mResetChallengesButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //! Setup MainActivity environment.

        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mDvaApplication = (DvaApplication) getApplication();

        mChallenge1Button = findViewById(R.id.challenge_1_button);
        mChallenge1Button.setOnClickListener(this);

        mChallenge2Button = findViewById(R.id.challenge_2_button);
//        mChallenge3Button.setClickable(false);
        mChallenge2Button.setActivated(false);

        mChallenge3Button = findViewById(R.id.challenge_3_button);
        mChallenge3Button.setOnClickListener(this);

        mChallenge4Button = findViewById(R.id.challenge_4_button);
        mChallenge4Button.setOnClickListener(this);

        mChallenge5Button = findViewById(R.id.challenge_5_button);
        mChallenge5Button.setOnClickListener(this);

        mResetChallengesButton = findViewById(R.id.reset_challenges_button);
        mResetChallengesButton.setOnClickListener(this);
    }

    private void updateChallengeStatus()
    {
        //! Update challenge status for every challenge.
        for (int i = 1; i <= 5; i++)
        {
            boolean challengeStatus = mDvaApplication.getChallengeStatus(i);
            Button challengeButton = null;

            switch (i)
            {
                case 1:
                    challengeButton = mChallenge1Button;
                    break;

                case 2:
                    challengeButton = mChallenge2Button;
                    break;

                case 3:
                    challengeButton = mChallenge3Button;
                    break;

                case 4:
                    challengeButton = mChallenge4Button;
                    break;

                case 5:
                    challengeButton = mChallenge5Button;
                    break;

                default:
                    continue;
            }

            //! Brighten the button corresponding to the challenge if challenge has been passed.
            if (challengeStatus)
                challengeButton.setAlpha(1.0f);
            else
                challengeButton.setAlpha(0.5f);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //! Update challenge status everytime the activity is resumed.
        updateChallengeStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //! parse result
        if (null == data)
            return;

        Log.d(TAG, "Activity=" + data.getComponent().getClassName());

        if (Activity.RESULT_OK == resultCode)
        {
            Log.d(TAG, "RESULT_OK");
        }
    }

    @Override
    public void onClick(View view)
    {
        Intent intent = new Intent();

        //! Check which button was clicked and start the appropriate activity.
        switch (view.getId())
        {
            case R.id.challenge_1_button:
                intent.setClass(getApplicationContext(), ArbitraryCodeExecutionActivity.class);
                break;

            case R.id.challenge_2_button:
                return;

            case R.id.challenge_3_button:
                intent.setClass(getApplicationContext(), VulnerableServiceActivity.class);
                break;

            case R.id.challenge_4_button:
                intent.setClass(getApplicationContext(), VulnerableBroadcastReceiverActivity.class);
                break;

            case R.id.challenge_5_button:
                intent.setClass(getApplicationContext(), MemoryDumpActivity.class);
                break;

            case R.id.reset_challenges_button:
                mDvaApplication.resetChallenges();
                updateChallengeStatus();
                return;

            default:
                Log.e(TAG, "Invalid view");
                return;
        }
        intent.putExtra("CHALLENGE_ID", view.getId());

        //! Launch activity
        mChallengeLauncher.launch(intent);
    }
}