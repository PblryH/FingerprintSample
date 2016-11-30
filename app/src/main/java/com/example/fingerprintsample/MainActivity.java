package com.example.fingerprintsample;

import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,FingerprintHelper.SimpleAuthenticationCallback {

    private Button encrypt, decrypt;
    private TextView tv;
    private FingerprintHelper helper;
    private boolean mIsFingerprintAuthenticationOnGoing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        encrypt = (Button) findViewById(R.id.encrypt);
        decrypt = (Button) findViewById(R.id.decrypt);
        tv = (TextView) findViewById(R.id.tv);
        encrypt.setOnClickListener(this);
        decrypt.setOnClickListener(this);
        helper = new FingerprintHelper(this);
        helper.setCallback(this);
        helper.generateKey();
        tv.setText("Result");
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mIsFingerprintAuthenticationOnGoing) {
            helper.setCallback(this);
            helper.authenticate();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        helper.stopAuthenticate();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.encrypt:
                helper.setPurpose(KeyProperties.PURPOSE_ENCRYPT);
                tv.setText("set finger......");
                mIsFingerprintAuthenticationOnGoing = helper.authenticate();
                Timber.d("encrypt mIsFingerprintAuthenticationOnGoing = %s",mIsFingerprintAuthenticationOnGoing);
                break;
            case R.id.decrypt:
                helper.setPurpose(KeyProperties.PURPOSE_DECRYPT);
                tv.setText("set finger......");
                mIsFingerprintAuthenticationOnGoing = helper.authenticate();
                Timber.d("decrypt mIsFingerprintAuthenticationOnGoing = %s",mIsFingerprintAuthenticationOnGoing);
                break;
        }
    }

    @Override
    public void onAuthenticationSucceeded(String value) {
        tv.setText(value);
        mIsFingerprintAuthenticationOnGoing = false;
        Timber.d("onAuthenticationSucceeded mIsFingerprintAuthenticationOnGoing = %s",mIsFingerprintAuthenticationOnGoing);
    }

    @Override
    public void onAuthenticationFail() {
        tv.setText("onAuthenticationFail");
        mIsFingerprintAuthenticationOnGoing = false;
        Timber.d("onAuthenticationFail mIsFingerprintAuthenticationOnGoing = %s",mIsFingerprintAuthenticationOnGoing);
    }
}
