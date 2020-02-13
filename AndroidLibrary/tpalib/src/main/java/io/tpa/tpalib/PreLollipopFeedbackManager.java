package io.tpa.tpalib;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

final class PreLollipopFeedbackManager extends FeedbackManager {

    PreLollipopFeedbackManager(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
    }

    @Override
    protected void startFeedbackWithoutScreenshot(@NonNull Activity activity) {
        String screenshotPath = Screenshot.captureScreenshotToFile(activity);
        if (screenshotPath != null) {
            showFeedbackActivity(activity, screenshotPath);
        }
    }
}
