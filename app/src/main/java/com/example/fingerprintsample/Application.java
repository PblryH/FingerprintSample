package com.example.fingerprintsample;

import timber.log.Timber;

/**
 * Created by kamalovrail on 22.11.16.
 */

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            initTimber();
        }
    }

    private void initTimber(){
        Timber.plant(new Timber.DebugTree(){
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + "/APP";
            }
        });
    }
}
