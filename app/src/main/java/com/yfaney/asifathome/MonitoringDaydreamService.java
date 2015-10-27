package com.yfaney.asifathome;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.view.ViewPropertyAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;


/**
 * This class is a sample implementation of a DreamService. When activated, a
 * TextView will repeatedly, move from the left to the right of screen, at a
 * random y-value.
 * <p/>
 * Daydreams are only available on devices running API v17+.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class MonitoringDaydreamService extends DreamService {
    final static int EVENT_UPDATE_TEMP_HUMI = 1;
    boolean toggleCelciusFahrenheit = false;
    double mTemperature = 0f;
    double mHumidity = 0f;

    private static final TimeInterpolator sInterpolator = new LinearInterpolator();
    private static final TimeInterpolator sInterpolator2 = new LinearInterpolator();

    private final AnimatorListener mAnimListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            // Start temp/humi text animation again
            mDreamTextView.setText(getTempHumi());
            startTextViewScrollAnimation();
        }

    };
    private final AnimatorListener mAnimListener2 = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            // Start animation again
            startTextViewScrollAnimation2();
        }

    };

    private final Random mRandom = new Random();
    private final Point mPointSize = new Point();
    public EventHandler mHandler;

    private TextView mDreamTextView;
    private TextView mDreamTimeView;
    private ViewPropertyAnimator mAnimator;
    private ViewPropertyAnimator mTimeAni;
    private HTSensorDataDisplayTask mTask;
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Exit dream upon user touch?
        setInteractive(true);

        // Hide system UI?
        setFullscreen(true);

        // Keep screen at full brightness?
        setScreenBright(false);

        // Set the content view, just like you would with an Activity.
        setContentView(R.layout.monitoring_daydream);

        mDreamTextView = (TextView) findViewById(R.id.dream_text);
        getDataFromPreferences();
        mDreamTextView.setText(getTempHumi());
        mDreamTimeView = (TextView) findViewById(R.id.dream_time);
        mDreamTimeView.setText(getCurrentTime());
        mHandler = new EventHandler(this);
        mTask = new HTSensorDataDisplayTask(mHandler);

//        mDreamTimeView.setTranslationX(-mDreamTimeView.getWidth());
//        mDreamTextView.setTranslationX(-mDreamTextView.getWidth());

    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        startTextViewScrollAnimation();
        startTextViewScrollAnimation2();
        mTask.execute(null, null, null);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();

        mTask.stopTask();
        mAnimator.cancel();
        mTimeAni.cancel();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // (for example, detach from handlers and listeners).
    }

    private void handleMessage(Message message) {
        switch(message.what){
            case EVENT_UPDATE_TEMP_HUMI:
            {
                getDataFromPreferences();
                mDreamTextView.setText(getTempHumi());
                mDreamTimeView.setText(getCurrentTime());
                break;
            }
            default:
            {

            }
        }
    }

    private void getDataFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        return prefs.getString(getString(R.string.pref_dream_text_key),
//                getString(R.string.pref_dream_text_default));
        mTemperature = prefs.getFloat(getString(R.string.pref_dream_temp_key), 0f);
        mHumidity = prefs.getFloat(getString(R.string.pref_dream_humi_key), 0f);
    }
    private String getTempHumi(){
        if(toggleCelciusFahrenheit){
            double lTempFahr = mTemperature * 1.8 + 32;
            toggleCelciusFahrenheit = !toggleCelciusFahrenheit;
            return String.format("%.1f\u2109 / %.1f%%\n", lTempFahr, mHumidity);
        }else{
            toggleCelciusFahrenheit = !toggleCelciusFahrenheit;
            return String.format("%.1f\u2103 / %.1f%%\n", mTemperature, mHumidity);
        }

    }

    private String getCurrentTime(){
        DateFormat df2 = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return df2.format(new Date());
    }

    private void startTextViewScrollAnimation() {
        // Refresh Size of Window
        getWindowManager().getDefaultDisplay().getSize(mPointSize);

        final int windowWidth = mPointSize.x;
        final int windowHeight = mPointSize.y;

        // Move TextView so it's moved all the way to the left
        mDreamTextView.setTranslationX(windowWidth);

        // Move TextView to random y value
        final int yRange = windowHeight - mDreamTextView.getHeight();

        mDreamTextView.setTranslationY(mRandom.nextInt(yRange));

        // Create an Animator and keep a reference to it
        mAnimator = mDreamTextView.animate().translationX(-mDreamTextView.getWidth())
                .setDuration(6000)
                .setStartDelay(100)
                .setListener(mAnimListener)
                .setInterpolator(sInterpolator);

        // Start the animation
        mAnimator.start();
    }

    private void startTextViewScrollAnimation2() {
        // Refresh Size of Window
        getWindowManager().getDefaultDisplay().getSize(mPointSize);

        final int windowWidth = mPointSize.x;
        final int windowHeight = mPointSize.y;

        // Move TextView to random x value
        final int xRange = windowWidth - mDreamTimeView.getWidth();
        // Move TextView to random y value
        final int yRange = windowHeight - mDreamTimeView.getHeight();

        mDreamTimeView.setTranslationX(mRandom.nextInt(xRange));
        mDreamTimeView.setTranslationY(mRandom.nextInt(yRange));
        mDreamTimeView.setAlpha(1);

        // Create an Animator and keep a reference to it
        mTimeAni = mDreamTimeView.animate().alpha(0)
                .setDuration(2000)
                .setStartDelay(8000)
                .setListener(mAnimListener2)
                .setInterpolator(sInterpolator2);

        // Start the animation
        mTimeAni.start();
    }


    class HTSensorDataDisplayTask extends AsyncTask<String, Process, String> {

        EventHandler mHander;
        boolean mRunning;
        HTSensorDataDisplayTask(EventHandler handler){
            super();
            mHandler = handler;
            mRunning = true;
        }

        @Override
        protected String doInBackground(String... params) {
            while(mRunning){
                try {
                    mHandler.sendEmptyMessage(EVENT_UPDATE_TEMP_HUMI);
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        public void stopTask(){
            mRunning = false;
        }
    }
    static class EventHandler extends Handler {
        private WeakReference<MonitoringDaydreamService> mService = null;
        EventHandler(MonitoringDaydreamService service){
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message message){
            MonitoringDaydreamService lService = mService.get();
            lService.handleMessage(message);
        }
    }
}
