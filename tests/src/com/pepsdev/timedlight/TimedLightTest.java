package com.pepsdev.timedlight;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.pepsdev.timedlight.TimedLightTest \
 * com.pepsdev.timedlight.tests/android.test.InstrumentationTestRunner
 */
public class TimedLightTest extends ActivityInstrumentationTestCase2<TimedLight> {

    private TimedLight timedLightActivity;

    public TimedLightTest() {
        super("com.pepsdev.timedlight", TimedLight.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        timedLightActivity = this.getActivity();
    }

    public void testCountDown() {
        assertTrue(!timedLightActivity.tv.getCountDownStarted());

        timedLightActivity.startCountDown(1000);

        assertTrue(timedLightActivity.tv.getCountDownStarted());
    }
}
