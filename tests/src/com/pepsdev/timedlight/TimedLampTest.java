package com.pepsdev.timedlamp;

import android.app.Instrumentation;
import android.provider.Settings;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.UiThreadTest;
import android.util.Log;

public class TimedLampTest extends ActivityInstrumentationTestCase2<TimedLamp> {

    private TimedLamp timedLampActivity;
    private Instrumentation mInstrumentation;

    public TimedLampTest() {
        super("com.pepsdev.timedlamp", TimedLamp.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        timedLampActivity = this.getActivity();
        mInstrumentation = this.getInstrumentation();
    }

    public void testPreConditions() {
        assertTrue(!timedLampActivity.tv.isCountDownStarted());
    }

    @UiThreadTest
    public void testDontTouchBrightnessOnPause() {
        int beforeBrightness = getBrightness();

        timedLampActivity.startCountDown(1000);
        timedLampActivity.lightItUp();
        assertTrue(timedLampActivity.tv.isCountDownStarted());

        mInstrumentation.callActivityOnPause(timedLampActivity);

        int afterBrightness = getBrightness();

        Log.d("Test me", "beforeBrightness is " + beforeBrightness + " and after is " + afterBrightness);
        assertEquals(beforeBrightness, afterBrightness);
    }

    public void testCountDown() {
        timedLampActivity.startCountDown(1000);
        assertTrue(timedLampActivity.tv.isCountDownStarted());
    }

    public void testDragTurnOn() {
        // miss me
        TouchUtils.drag(this, timedLampActivity.tv.touchBox.left - 2,
                              timedLampActivity.tv.touchBox.left - 2,
                              timedLampActivity.tv.touchBox.top + 2,
                              timedLampActivity.tv.touchBox.bottom - 2,
                              10
                       );
        assertTrue(!timedLampActivity.tv.isCountDownStarted());

        // turn me on
        TouchUtils.drag(this, timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.top + 2,
                              timedLampActivity.tv.touchBox.top + 80,
                              10
                       );
        assertTrue(timedLampActivity.tv.isCountDownStarted());

        // short me down
        TouchUtils.drag(this, timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.top + 30,
                              timedLampActivity.tv.touchBox.top + 20,
                              10
                       );
        assertTrue(timedLampActivity.tv.isCountDownStarted());

        int brightness1 = getBrightness();

        // turn me off
        TouchUtils.drag(this, timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.top + 80,
                              timedLampActivity.tv.touchBox.top + 2,
                              10
                       );
        TouchUtils.drag(this, timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.left + 2,
                              timedLampActivity.tv.touchBox.top + 80,
                              timedLampActivity.tv.touchBox.top + 2,
                              10
                       );
        assertTrue(!timedLampActivity.tv.isCountDownStarted());

        int brightness2 = getBrightness();

        assertTrue(brightness1 > brightness2);
    }

    private int getBrightness() {
        int brightness;
        try {
            brightness = Settings.System.getInt(timedLampActivity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e)  {
            brightness = -1;
        }
        return brightness;
    }
}
