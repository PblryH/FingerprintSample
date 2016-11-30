package com.example.fingerprintsample;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Toast;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import timber.log.Timber;


/**
 * Created by hzlinxuanxuan on 2016/9/12.
 */
public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {

    private FingerprintManager manager;
    private CancellationSignal mCancellationSignal;
    private SimpleAuthenticationCallback callback;
    private LocalSharedPreference mLocalSharedPreference;
    private LocalAndroidKeyStore mLocalAndroidKeyStore;

    private int purpose = KeyProperties.PURPOSE_ENCRYPT;
    private String data = "123456";

    public FingerprintHelper(Context context) {
        manager = context.getSystemService(FingerprintManager.class);
        mLocalSharedPreference = new LocalSharedPreference(context);
        mLocalAndroidKeyStore = new LocalAndroidKeyStore();
    }

    public void generateKey() {
        mLocalAndroidKeyStore.generateKey(LocalAndroidKeyStore.keyName);
        setPurpose(KeyProperties.PURPOSE_ENCRYPT);
    }

    public boolean isKeyProtectedEnforcedBySecureHardware() {
        return mLocalAndroidKeyStore.isKeyProtectedEnforcedBySecureHardware();
    }


    public int checkFingerprintAvailable(Context ctx) {
        if (!isKeyProtectedEnforcedBySecureHardware()) {
            return -1;
        } else if (!manager.isHardwareDetected()) {
            Toast.makeText(ctx, "!manager.isHardwareDetected()",Toast.LENGTH_SHORT).show();
            return -1;
        } else if (!manager.hasEnrolledFingerprints()) {
            Toast.makeText(ctx, "!manager.hasEnrolledFingerprints()",Toast.LENGTH_SHORT).show();
            return 0;
        }
        return 1;
    }

    public boolean containsToken() {
        return mLocalSharedPreference.containsKey(mLocalSharedPreference.dataKeyName);
    }

    public void setCallback(SimpleAuthenticationCallback callback) {
        this.callback = callback;
    }

    public void setPurpose(int purpose) {
        this.purpose = purpose;
    }

    public boolean authenticate() {
        try {
            FingerprintManager.CryptoObject object;
            if (purpose == KeyProperties.PURPOSE_DECRYPT) {
                String IV = mLocalSharedPreference.getData(mLocalSharedPreference.IVKeyName);
                object = mLocalAndroidKeyStore.getCryptoObject(Cipher.DECRYPT_MODE, Base64.decode(IV, Base64.URL_SAFE));
                if (object == null) {
                    return false;
                }
            } else {
                object = mLocalAndroidKeyStore.getCryptoObject(Cipher.ENCRYPT_MODE, null);
            }
            mCancellationSignal = new CancellationSignal();
            manager.authenticate(object, mCancellationSignal, 0, this, null);
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopAuthenticate() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
        callback = null;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Timber.d("onAuthenticationSucceeded()");
        if (callback == null) {
            return;
        }
        if (result.getCryptoObject() == null) {
            callback.onAuthenticationFail();
            return;
        }
        final Cipher cipher = result.getCryptoObject().getCipher();
        if (purpose == KeyProperties.PURPOSE_DECRYPT) {
            String data = mLocalSharedPreference.getData(mLocalSharedPreference.dataKeyName);
            if (TextUtils.isEmpty(data)) {
                callback.onAuthenticationFail();
                return;
            }
            try {
                byte[] decrypted = cipher.doFinal(Base64.decode(data, Base64.URL_SAFE));
                callback.onAuthenticationSucceeded(new String(decrypted));
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                callback.onAuthenticationFail();
            }
        } else {
            try {
                byte[] encrypted = cipher.doFinal(data.getBytes());
                byte[] IV = cipher.getIV();
                String se = Base64.encodeToString(encrypted, Base64.URL_SAFE);
                String siv = Base64.encodeToString(IV, Base64.URL_SAFE);
                if (mLocalSharedPreference.storeData(mLocalSharedPreference.dataKeyName, se) &&
                        mLocalSharedPreference.storeData(mLocalSharedPreference.IVKeyName, siv)) {
                    callback.onAuthenticationSucceeded(se);
                }else{
                    callback.onAuthenticationFail();
                }
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                callback.onAuthenticationFail();
            }
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        Timber.d("onAuthenticationError()");
        if (callback != null) {
            callback.onAuthenticationFail();
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        Timber.d("onAuthenticationHelp()");
    }

    @Override
    public void onAuthenticationFailed() {
        Timber.d("onAuthenticationFailed()");
    }

    public interface SimpleAuthenticationCallback {
        void onAuthenticationSucceeded(String value);

        void onAuthenticationFail();
    }
}
