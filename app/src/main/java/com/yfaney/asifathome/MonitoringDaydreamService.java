package com.yfaney.asifathome;

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

    private static final TimeInterpolator sInterpolator = new LinearInterpolator();

    private final AnimatorListener mAnimListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            // Start animation again
            startTextViewScrollAnimation();
        }

    };

    private final Random mRandom = new Random();
    private final Point mPointSize = new Point();

    private TextView mDreamTextView;
    private ViewPropertyAnimator mAnimator;
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
        mDreamTextView.setText(getTextFromPreferences());
        mTask = new HTSensorDataDisplayTask(mDreamTextView);
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        // TODO: Begin animations or other behaviors here.
        startTextViewScrollAnimation();
        mTask.execute(null, null, null);
    }

    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();

        // TODO: Stop anything that was started in onDreamingStarted()
        mTask.stopTask();
        mAnimator.cancel();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // TODO: Dismantle resources
        // (for example, detach from handlers and listeners).
    }

    private String getTextFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        return prefs.getString(getString(R.string.pref_dream_text_key),
//                getString(R.string.pref_dream_text_default));
        double lTemp = prefs.getFloat(getString(R.string.pref_dream_temp_key), 0f);
        double lHumi = prefs.getFloat(getString(R.string.pref_dream_humi_key), 0f);
        return String.format("%.1f\u2103/%.1f%%", lTemp, lHumi);
    }

    private void startTextViewScrollAnimation() {
        // Refresh Size of Window
        getWindowManager().getDefaultDisplay().getSize(mPointSize);

        final int windowWidth = mPointSize.x;
        final int windowHeight = mPointSize.y;

        // Move TextView so it's moved all the way to the left
        mDreamTextView.setTranslationX(-mDreamTextView.getWidth());

        // Move TextView to random y value
        final int yRange = windowHeight - mDreamTextView.getHeight();
        mDreamTextView.setTranslationY(mRandom.nextInt(yRange));

        // Create an Animator and keep a reference to it
        mAnimator = mDreamTextView.animate().translationX(windowWidth)
                .setDuration(3000)
                .setStartDelay(500)
                .setListener(mAnimListener)
                .setInterpolator(sInterpolator);

        // Start the animation
        mAnimator.start();
    }

    class HTSensorDataDisplayTask extends AsyncTask<String, Process, String> {

        TextView mTextView;
        boolean mRunning;
        HTSensorDataDisplayTask(TextView textView){
            super();
            mTextView = textView;
            mRunning = true;
        }

        @Override
        protected String doInBackground(String... params) {
            while(mRunning){
                mTextView.setText(getTextFromPreferences());
                try {
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
}
