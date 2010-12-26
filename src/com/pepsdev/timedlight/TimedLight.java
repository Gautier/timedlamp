package com.pepsdev.timedlight;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.provider.Settings;

import java.util.Timer;
import java.util.TimerTask;


public class TimedLight extends Activity {
    public static final String EXTRA_TIMEOUT = "com.pepsdev.timedlight.Timeout";
    public static final String ACTION_ILLUMINATE = "com.pepsdev.timedlight.illuminate";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

        tv = new TimedLightView(this, metrics.density);
        tv.setOnTiretteListener(new TimedLightView.OnTiretteListener() {
            @Override
            public void tiretted() {
                lightItUp();
            }
            @Override
            public void unTiretted() {
                switchOff();

                synchronized (wl) {
                    if (wl.isHeld())
                        wl.release();
                }
            }
        });
        setContentView(tv);
    }

    @Override
    public void onResume() {
        super.onResume();

        switchOff();

        wl.acquire();

        final Intent callingIntent = getIntent();
        if (callingIntent.hasExtra(EXTRA_TIMEOUT)) {
            Log.d(TAG, "has EXTRA_TIMEOUT" + callingIntent.getIntExtra(EXTRA_TIMEOUT,
                                                     DEFAULT_TIMEOUT));
            lightItUp();
            startCountDown(callingIntent.getIntExtra(EXTRA_TIMEOUT,
                                                     DEFAULT_TIMEOUT));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (tv.getCountDownStarted()) {
            // if brightness is not max_brightness it would mean that the user
            // changed the screen brightness in between
            if (getBrightness() == MAX_BRIGHTNESS) {
                setBrightness(restoreBrightness);
            }
            synchronized (this) {
                if (wl.isHeld())
                    wl.release();
            }
            tv.stopCountDown();
        }
    }

    public void startCountDown() {
        tv.startCountDown((int)tv.getTiretteDuration());
    }

    public void startCountDown(int coundDown) {
        tv.startCountDown(coundDown);
    }

    private void lightItUp() {
        tv.lightItUp();
        restoreBrightness = getBrightness();
        setBrightness(MAX_BRIGHTNESS);
    }

    private void switchOff() {
        setBrightness(DIM_BRIGHTNESS);
    }

    // 0 < brightness < 255
    private void setBrightness(int brightness) {
        Settings.System.putInt(getContentResolver(),
                               Settings.System.SCREEN_BRIGHTNESS, brightness);

        android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness / 255f; // 0.0 - 1.0
        getWindow().setAttributes(lp);
    }

    private int getBrightness() {
        // let's couper la poire en deux
        int brightness = MAX_BRIGHTNESS / 2;
        try {
            brightness = Settings.System.getInt(getContentResolver(),
                                                Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e)  {
            // whatever, brightness will be on a safe value
        }
        return brightness;
    }

    private static final int DEFAULT_TIMEOUT = 60 * 1000;
    private static final int MAX_BRIGHTNESS = 255;
    private static final int DIM_BRIGHTNESS = 30;

    private static final String TAG = "com.pepsdev.timedlight.TimedLight";

    private int restoreBrightness;
    public TimedLightView tv;

    private PowerManager.WakeLock wl;
}

