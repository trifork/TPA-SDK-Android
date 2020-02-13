package io.tpa.tpalib;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.tpa.tpalib.android.R;
import io.tpa.tpalib.feedback.FeedbackInvocation;
import io.tpa.tpalib.feedback.ShakeFeedbackHandler;

abstract class FeedbackManager extends TpaFeature {

    protected final static String TAG = "FeedbackManager";

    @Nullable
    private ShakeDetector detector;
    @Nullable
    private SensorManager sensor;
    @Nullable
    private ShakeFeedbackHandler shakeFeedbackHandler;

    FeedbackManager(@NonNull final Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);

        shakeFeedbackHandler = config.getFeedbackHandler();

        if (config.getFeedbackInvocation() == FeedbackInvocation.EVENT_SHAKE) {
            //setup the shake detector in background as this can take time (seen up to 200 ms at init of TPA)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    setupShakeDetector(context);
                }
            }).start();
        }
    }

    private void setupShakeDetector(@NonNull Context context) {
        sensor = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensor == null) {
            TpaDebugging.log.e(TAG, "Couldn't get SensorManager");
            return;
        }
        TpaDebugging.log.i(TAG, "Setting up shake detector");

        detector = new ShakeDetector(new ShakeDetector.Listener() {
            @Override
            public void hearShake() {
                TpaDebugging.log.d(TAG, "Detected shake!");
                stopDetection();

                // Are we using the screenshot listener?
                if (shakeFeedbackHandler != null) {
                    TpaDebugging.log.d(TAG, "Custom shake handler registered, sending event to it");
                    shakeFeedbackHandler.handleShakeFeedback();
                    return;
                }

                // If not we use start the feedback normally
                Activity activity = TPAManager.getInstance().getCurrentActivity();
                if (activity != null && AppLifeCycle.isActivityValid(activity)) {
                    TpaDebugging.log.d(TAG, "Starting feedback!");
                    startFeedback(activity, null);
                }
            }
        });
        startDetection();
    }

    void startFeedback() {
        Activity activity = TPAManager.getInstance().getCurrentActivity();
        if (activity != null && AppLifeCycle.isActivityValid(activity)) {
            startFeedback(activity, null);
        }
    }

    void startFeedback(@Nullable String screenshotFileLocation) {
        Activity activity = TPAManager.getInstance().getCurrentActivity();
        if (activity != null && AppLifeCycle.isActivityValid(activity)) {
            startFeedback(activity, screenshotFileLocation);
        }
    }

    private void startFeedback(@NonNull Activity activity, @Nullable String screenshotFileLocation) {
        if (screenshotFileLocation != null) {
            showFeedbackActivity(activity, screenshotFileLocation);
        } else {
            startFeedbackWithoutScreenshot(activity);
        }
    }

    abstract void startFeedbackWithoutScreenshot(@NonNull Activity activity);

    void showFeedbackActivity(@NonNull Activity activity, @NonNull String screenshotPath) {
        Intent feedbackIntent = new Intent(activity, FeedbackActivity.class);

        TpaDebugging.log.d(TAG, "We got a screenshot!");
        feedbackIntent.putExtra("screenshotPath", screenshotPath);

        activity.startActivity(feedbackIntent);
        activity.overridePendingTransition(R.anim.feedback_activity_animation, 0);
    }

    void startDetection() {
        if (detector != null) {
            boolean success = detector.start(sensor);
            TpaDebugging.log.d(TAG, success ? "Shake sensor started" : "Shake sensor failed to start");
        }
    }

    private void stopDetection() {
        if (detector != null) {
            detector.stop();
            TpaDebugging.log.d(TAG, "Stopped shake detection");
        }
    }

    @Override
    void onAppStarted() {
        // If detector and sensor are already in place, start the listener again
        if (detector != null && sensor != null) {
            startDetection();
        }
    }

    @Override
    void onAppEnding() {
        stopDetection();
    }

    @Override
    void onActivityCreated(@NonNull Activity activity, @Nullable Bundle arg1) {
        if (activity instanceof FeedbackActivity) {
            ((FeedbackActivity) activity).setFeedbackManager(this);
        }
    }
}
