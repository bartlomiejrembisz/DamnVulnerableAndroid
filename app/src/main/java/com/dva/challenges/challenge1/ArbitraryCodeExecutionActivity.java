package com.dva.challenges.challenge1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dva.challenges.ChallengeActivity;
import com.dva.challenges.DvaApplication;
import com.dva.challenges.R;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

public class ArbitraryCodeExecutionActivity extends AppCompatActivity implements View.OnClickListener, DvaApplication.IStatusListener
{
    private static final String TAG = ArbitraryCodeExecutionActivity.class.getSimpleName();

    private static final String REMOTE_CLASS_NAME = "RemoteClass";
    private static final String REMOTE_CLASS_METHOD_NAME = "passChallenge";

    Button mExecuteButton = null;
    TextView mExecutionStatus = null;
    DvaApplication mApplication = null;

    private ActivityResultLauncher<String> mRequestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->
            {
                if ((null == mExecutionStatus) || (null == mExecuteButton))
                    return;

                if (isGranted)
                {
                    mExecuteButton.setClickable(true);
                }
                else
                {
                    mExecutionStatus.setText("Access denied.");
                    mExecutionStatus.setTextColor(Color.RED);
                    mExecuteButton.setClickable(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //! Setup the activity and challenge environment.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arbitrary_code_execution);

        mExecutionStatus = findViewById(R.id.execution_status);
        mExecuteButton = findViewById(R.id.execute_button);
        mExecuteButton.setOnClickListener(this);
        mExecuteButton.setClickable(false);

        mApplication = (DvaApplication) getApplication();
        mApplication.registerChallengeStatusListener(1,  this);

        mRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        setStatus(mApplication.getChallengeStatus(1));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mApplication.unregisterChallengeStatusListener(1);
    }

    private Class loadRemoteClass(String remoteJarFilePath, String className) throws IOException, ClassNotFoundException
    {
        Log.i(TAG, "Loading jar file " + remoteJarFilePath);

        //! Use the DexClassLoader to load the dexified /sdcard/RemoteClass.jar java archive.
        DexClassLoader classLoader = new DexClassLoader(remoteJarFilePath,
                getCodeCacheDir().getAbsolutePath(),
                null,
                getClass().getClassLoader());

        //! Load the class.
        Log.i(TAG, "Loading class " + className);
        return classLoader.loadClass(className);
    }

    @Override
    public void onClick(View view)
    {
        Class remoteClass = null;
        try
        {
            //! Load remote class from external storage '/sdcard/'.
            remoteClass = loadRemoteClass(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + REMOTE_CLASS_NAME + ".jar",
                    REMOTE_CLASS_NAME);

            //! Create RemoteClass instance and invoke passChallenge method.
            Object classObject = remoteClass.newInstance();
            Method memberFunction = remoteClass.getMethod(REMOTE_CLASS_METHOD_NAME);
            Object memberReturnValue = memberFunction.invoke(classObject);

            Boolean booleanReturnValue = (Boolean) memberReturnValue;
            if (null == booleanReturnValue)
                throw new InvalidClassException("Wrong return type.");

            //! Pass challenge depending on output of passChallenge.
            final boolean shouldPassChallenge = booleanReturnValue;
            if (shouldPassChallenge)
                mApplication.setChallengeStatus(1, true);
            else
                mApplication.setChallengeStatus(1, false);

            Log.i(TAG, "Remote class return value=" + shouldPassChallenge);
        }
        catch (Exception e)
        {
            mApplication.setChallengeStatus(1, false);
            Log.e(TAG, "Unable to load remote class", e);
        }
    }

    @Override
    public void onStatusChange(boolean status)
    {
        setStatus(status);
    }

    private void setStatus(boolean hasPassed)
    {
        String status = hasPassed ? "PASSED" : "NOT PASSED";
        int color = hasPassed ? Color.GREEN : Color.RED;

        mExecutionStatus.setText(status);
        mExecutionStatus.setTextColor(color);
    }
}