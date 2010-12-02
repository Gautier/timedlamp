package com.pepsdev.timedlight;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;

import android.provider.Settings;
import android.provider.Settings.System;

public class TimedLight extends Activity {
    private int DEFAULT_TIMEOUT = 60 * 1000;
    private int MAX_BRIGHTNESS = 255;

    private String TAG = "com.pepsdev.timedlight.TimedLight";

    private int timeout;
    Handler handler = new Handler();
    private CountDownTimer countDown;
    private boolean countDownStarted = false;
    private int restoreBrightness;
    private TimedLightView tv;
    private boolean stop = false;

    PowerManager.WakeLock wl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TimedLightView tv = new TimedLightView(this);
        setContentView(tv);

        final Intent callingIntent = getIntent();
        timeout = callingIntent.getIntExtra("com.pepsdev.timedlight.Timeout", DEFAULT_TIMEOUT);

        final int fps = 25;
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                tv.draw();
                if (!stop)
                    handler.postDelayed(this, fps);
            }
        };
        handler.postDelayed(r, fps);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        restoreBrightness = getBrightness();
        Log.d(TAG, "save brightness " + restoreBrightness);

        setBrightness(MAX_BRIGHTNESS);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

        wl.acquire();
        countDown = new CountDownTimer(timeout, timeout) {
            @Override
            public void onTick(long msUntilFinished) {
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish");
                synchronized (this) {
                    if (TimedLight.this.wl.isHeld())
                        TimedLight.this.wl.release();
                }
                setBrightness(TimedLight.this.restoreBrightness);
                TimedLight.this.finish();
            }
        };
        countDown.start();
        countDownStarted = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        stop = true;

        if (countDownStarted) {
            countDown.cancel();
            // if brightness is not max_brightness it would mean that the user
            // changed the screen brightness in between
            Log.d(TAG, "getBrightness() == " + getBrightness());
            if (getBrightness() == MAX_BRIGHTNESS) {
                Log.d(TAG, "will restore to " + restoreBrightness);
                setBrightness(restoreBrightness);
            }
            synchronized (this) {
                if (TimedLight.this.wl.isHeld())
                    TimedLight.this.wl.release();
            }
            countDownStarted = false;
        }
    }

    // 0 < brightness < 255
    private void setBrightness(int brightness) {
        System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, brightness);

        android.view.WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness / 255f; // 0.0 - 1.0
        //getWindow().setAttributes(lp);
    }

    private int getBrightness() {
        // let's couper la poire en deux
        int brightness = MAX_BRIGHTNESS / 2;
        try {
            brightness = System.getInt(getContentResolver(),
                                                    System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e)  {
            // whatever, brightness will be on a safe value
        }
        return brightness;
    }
}

