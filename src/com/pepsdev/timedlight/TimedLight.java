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

import alt.android.os.CountDownTimer;

import java.util.Timer;
import java.util.TimerTask;


public class TimedLight extends Activity {
    public static final String EXTRA_TIMEOUT = "com.pepsdev.timedlight.Timeout";
    public static final String ACTION_ILLUMINATE = "com.pepsdev.timedlight.illuminate";
    public boolean countDownStarted = false;

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
                stopCountDown();
                lightItUp();
                startCountDown();
            }
            @Override
            public void unTiretted() {
                switchOff();

                stopCountDown();

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

        if (countDownStarted) {

            // if brightness is not max_brightness it would mean that the user
            // changed the screen brightness in between
            if (getBrightness() == MAX_BRIGHTNESS) {
                setBrightness(restoreBrightness);
            }
            synchronized (this) {
                if (wl.isHeld())
                    wl.release();
            }
            countDownStarted = false;
            countDown.cancel();
        }
    }

    public void startCountDown() {
        startCountDown((int)tv.getTiretteDuration());
    }

    public void startCountDown(long timeout) {
        Log.d(TAG, "start with timeout " + timeout);

        final long stopAt = System.currentTimeMillis() + timeout;

        countDown = new CountDownTimer(timeout, SPT) {
            @Override
            public void onTick(long ms) {
                tv.tick(ms);
            }

            @Override
            public void onFinish() {
                synchronized (wl) {
                    if (wl.isHeld())
                        wl.release();
                }
                tv.switchOff();
                setBrightness(TimedLight.this.restoreBrightness);
                restoreBrightness = getBrightness();
            }
        }.start();

        countDownStarted = true;
    }

    public void stopCountDown() {
        countDownStarted = false;
        if (countDown != null) {
            countDown.cancel();
        }
    }

    private void lightItUp() {
        tv.lightItUp();
        restoreBrightness = getBrightness();
        setBrightness(MAX_BRIGHTNESS);
    }

    private void switchOff() {
        tv.switchOff();
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

    private static final int SPT = 250; // seconds per tick

    private static final String TAG = "com.pepsdev.timedlight.TimedLight";

    private CountDownTimer countDown;

    private int restoreBrightness;
    private TimedLightView tv;

    private PowerManager.WakeLock wl;
}

