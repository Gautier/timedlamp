package com.pepsdev.timedlight;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.media.MediaPlayer;

import alt.android.os.CountDownTimer;

import android.graphics.Rect;


public class TimedLightView extends SurfaceView
    implements SurfaceHolder.Callback {

    private static final String TAG = "com.pepsdev.timedlight";
    private static final int SPT = 250; // seconds per tick

    public interface OnTiretteListener {
        public void tiretted();
        public void unTiretted();
    }

    public void setOnTiretteListener(OnTiretteListener l) {
        tiretteListener = l;
    }

    private SurfaceHolder mSurfaceHolder;
    private GestureDetector gestureDetector;
    private OnTiretteListener tiretteListener;

    private CountDownTimer mCountDown;
    public boolean mCoundDownStarted = false;

    private int width;
    private int height;
    private float mDensity;

    private boolean canDraw  = false;

    private Bitmap currentLamp;
    private Bitmap lamp;
    private Bitmap lamp_hl;
    private Bitmap handle;
	private int handleHeight;

    private static final int HANDLE_DURATION = 1000 * 120; // 2 mins

    /* Following constants are density dependants
     * This is why they are not final
     */
    //private int HANDLE_POS_STOP = 346;
    private int HANDLE_POS_DEFAULT;
    private int HANDLE_POS_X;
    private int HANDLE_POS_MAX;
    private Rect touchBox;

    private int handlePos; // bottom of the handle
    private boolean listeningToScroll = false;

    private MediaPlayer mpClick = null;

    public void draw() {
        if (!canDraw)
            return;
        mSurfaceHolder.setKeepScreenOn(true);
        Canvas c = null;
        try {
            c = mSurfaceHolder.lockCanvas();
            doDraw(c);
        } finally {
            if (c != null) {
                mSurfaceHolder.unlockCanvasAndPost(c);
            }
        }
    }

    public void tick(long ms) {
        setTiretteDuration(ms);

        if (handlePos <= HANDLE_POS_DEFAULT) {
            if (tiretteListener != null) {
                tiretteListener.unTiretted();
            }
        }
    }

    public void doDraw(Canvas c) {
        c.drawARGB(255, 255, 255, 255);
        c.drawBitmap(handle, HANDLE_POS_X, handlePos - handleHeight, null);
        c.drawBitmap(currentLamp, 0, 0, null);
    }

    private void playClick() {
        if (mpClick == null) {
            mpClick = MediaPlayer.create(getContext(), R.raw.click);
            mpClick.start();
        } else {
            mpClick.stop();
            try {
                mpClick.prepare();
                mpClick.start();
            } catch(java.io.IOException e) {
                Log.w("TimedLightView", "Warning player did not work");
                Log.w("TimedLightView", e);
            }
        }
    }

    public long getTiretteDuration() {
        long msPerPx = HANDLE_DURATION / (HANDLE_POS_MAX - HANDLE_POS_DEFAULT);

        return msPerPx * (handlePos - HANDLE_POS_DEFAULT);
    }

    public void setTiretteDuration(long ms) {
        double pxPerMs = (double)(HANDLE_POS_MAX - HANDLE_POS_DEFAULT) /
                         (double) HANDLE_DURATION;

        handlePos = HANDLE_POS_DEFAULT + (int)(ms * pxPerMs);
        draw();
    }

    public void lightItUp() {
        currentLamp = lamp_hl;
    }

    public void switchOff() {
        currentLamp = lamp;
        setTiretteDuration(0);
        draw();
    }

    public void toggle() {
        if (currentLamp == lamp_hl) {
            switchOff();
        } else {
            lightItUp();
        }
    }

    public TimedLightView(Context context, final float density) {
        super(context);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);


        lamp = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.lamp);
        lamp_hl = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.lamp_hl);
        currentLamp = lamp;
        handle = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.handle);

        handleHeight = handle.getHeight();

        mDensity = density;

        HANDLE_POS_DEFAULT = (int)(205 * mDensity);
        handlePos = HANDLE_POS_DEFAULT;
        HANDLE_POS_X = (int)(120 * mDensity);
        HANDLE_POS_MAX = (int)(205 + (85 * 2) * mDensity);

        touchBox = new Rect((int)(128 * mDensity), (int)(HANDLE_POS_DEFAULT - 90 * mDensity),
                            (int)(290 * mDensity), (int)(lamp.getHeight()));

        gestureDetector = new GestureDetector(
                new GestureDetector.SimpleOnGestureListener () {
                    public boolean onScroll(MotionEvent e1, MotionEvent e2,
                        float distanceX, float distanceY) {
                        if (!listeningToScroll)  return false;
                        if (distanceY < 0 &&
                            handlePos < HANDLE_POS_MAX) {
                            handlePos = Math.min(handlePos - (int)distanceY,
                                                    HANDLE_POS_MAX);
                        } else if (distanceY > 0 &&
                                   handlePos > HANDLE_POS_DEFAULT) {
                            handlePos = Math.max(handlePos - (int)distanceY,
                                                 HANDLE_POS_DEFAULT);
                        }

                        draw();
                        return false;
                    }
        });

        setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);

                    final float x = event.getX();
                    final float y = event.getY();
                    if (touchBox.contains((int)x, (int)y)) {
                        listeningToScroll = true;

                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("TimedLightView", "ACTIONNNNNNNN UP");
                            playClick();
                            if (tiretteListener != null) {
                                if (handlePos > HANDLE_POS_DEFAULT) {
                                    tiretted();
                                } else {
                                    unTiretted();
                                }
                            }
                            listeningToScroll = false;
                        }
                    } else {
                        listeningToScroll = false;
                    }
                    return true;
                }
        });
    }

    public void tiretted() {
        stopCountDown();
        tiretteListener.tiretted();
        startCountDown((int)getTiretteDuration());
    }

    public void unTiretted() {
        stopCountDown();
        tiretteListener.unTiretted();
        switchOff();
    }

    public void startCountDown(long timeout) {
        Log.d(TAG, "start with timeout " + timeout);
        final long stopAt = System.currentTimeMillis() + timeout;
        mCoundDownStarted = true;

        mCountDown = new CountDownTimer(timeout, SPT) {
            @Override
            public void onTick(long ms) {
                tick(ms);
            }

            @Override
            public void onFinish() {
                //switchOff();
                //if (tiretteListener != null) {
                //    tiretteListener.unTiretted();
                //}
            }
        }.start();
    }

    public void stopCountDown() {
        mCoundDownStarted = false;
        if (mCountDown != null) {
            mCountDown.cancel();
        }
    }

	public boolean getCountDownStarted(){
		return mCoundDownStarted;
	}

	public void setCountDownStarted(boolean mCoundDownStarted) {
		mCoundDownStarted = mCoundDownStarted;
	}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        width = width;
        height = height;

        touchBox.bottom = height;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        canDraw = true;
        width = getWidth();
        height = getHeight();

        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        canDraw = false;
    }
}
