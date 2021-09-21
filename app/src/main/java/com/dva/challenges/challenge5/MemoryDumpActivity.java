package com.dva.challenges.challenge5;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dva.challenges.DvaApplication;
import com.dva.challenges.R;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MemoryDumpActivity extends AppCompatActivity implements DvaApplication.IStatusListener, View.OnClickListener
{
    private static final String TAG = MemoryDumpActivity.class.getSimpleName();
    private static final String PRIVATE_KEY_HEADER = "-----BEGIN RSA PRIVATE KEY-----\n";
    private static final String PRIVATE_KEY_FOOTER = "\n-----END RSA PRIVATE KEY-----\n";

    private RSAPublicKey publicKey = null;
    private RSAPrivateKey privateKey = null;

    private Cipher cipher = null;
    private DvaApplication mApplication = null;

    private String privateKeyString = new String();

    private TextView mStatus = null;
    private EditText mMessageInput = null;
    private Button mPostButton = null;

    byte[] mData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_dump);

        try
        {
            //! Generate RSA key pair.
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(515);
            KeyPair keyPair = generator.generateKeyPair();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();

            //! Encode the private key with PKCS8.
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey.getEncoded());

            //! Copy the base64 encoded PKCS8 encoded private key to a string.
            privateKeyString += PRIVATE_KEY_HEADER;
            privateKeyString += Base64.getEncoder().encodeToString(spec.getEncoded());
            privateKeyString += PRIVATE_KEY_FOOTER;

            //! Generate random 8 character message.
            String message = generateRandomString(8);

            //! Encrypt the random message with the 515 bit RSA public key and cache it.
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            mData = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            Log.i(TAG, Base64.getEncoder().encodeToString(mData));

            mApplication = (DvaApplication) getApplication();
            mApplication.registerChallengeStatusListener(5,  this);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e)
        {
            Log.e(TAG, "Can't encrypt message, ", e);
            setResult(RESULT_CANCELED);
            finish();
        }

        mStatus = (TextView) findViewById(R.id.memory_dump_status);
        mMessageInput = (EditText) findViewById(R.id.message_input);
        mPostButton = (Button) findViewById(R.id.post_button);
        mPostButton.setOnClickListener(this);

        mStatus.setVisibility(View.VISIBLE);
        mMessageInput.setVisibility(View.VISIBLE);
        mPostButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        setStatus(mApplication.getChallengeStatus(5));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mApplication.unregisterChallengeStatusListener(5);
    }

    @Override
    public void onClick(View view)
    {
        try
        {
            //! On 'Post' button click, extract the message box string.
            String messageString = mMessageInput.getText().toString();

            //! Decrypt the encrypted cached random message with the RSA private key.
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] data = cipher.doFinal(mData);
            String decryptedString = new String(data, StandardCharsets.UTF_8);

            //! Pass the challenge if the message box string is the same as the decrypted cached
            //! random message.
            mApplication.setChallengeStatus(5, messageString.equals(decryptedString));
        }
        catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e)
        {
            Log.e(TAG, "Unable to compare messages, ", e);
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

        mStatus.setText(status);
        mStatus.setTextColor(color);
    }

    private static String generateRandomString(final int stringSize)
    {
        //! Generate random string.

        final int leftLimit = 97;
        final int rightLimit = 122;
        Random random = new Random();

        String randomString = random.ints(leftLimit, rightLimit + 1)
                .limit(stringSize)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return randomString;
    }
}