package io.tpa.tpalib;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Display;

import java.nio.Buffer;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
final class LollipopFeedbackManager extends FeedbackManager {

    @Nullable
    private ScreenShotCapture screenShotCapture;

    private boolean shouldCaptureScreenshotWhenResumed = false;

    @Nullable
    private ComponentName screenshotComponentName;

    private static final int SCREENSHOT_PERMISSION_REQUEST_CODE = 1;

    LollipopFeedbackManager(@NonNull Context context, @NonNull Config config, @NonNull Constants constants, @NonNull ProtobufFactory protobufFactory, @NonNull ProtobufReceiver protobufReceiver, @NonNull SessionUUIDProvider sessionUUIDProvider) throws FeatureInitializationException {
        super(context, config, constants, protobufFactory, protobufReceiver, sessionUUIDProvider);
    }

    @Override
    protected void startFeedbackWithoutScreenshot(@NonNull Activity activity) {
        startFeedbackFromMediaProjection(activity);
    }

    private void startFeedbackFromMediaProjection(@NonNull Activity activity) {
        //for Lollipop and newer we start activity that will call back when finished, if successful
        try {
            if (screenShotCapture != null) {
                captureScreenshot(screenShotCapture);
                screenShotCapture = null;
            } else {
                openScreenshotPermissionRequester(activity);
            }
        } catch (final RuntimeException ignored) {
            openScreenshotPermissionRequester(activity);
        }
    }

    private void openScreenshotPermissionRequester(@NonNull Activity activity) {
        screenshotComponentName = activity.getComponentName();
        ActivityResultCallback.getInstance().registerCallback(SCREENSHOT_PERMISSION_REQUEST_CODE, new ActivityResultCallback.Callback() {
            @Override
            public void onComplete(@NonNull Activity permissionActivity, int resultCode, @Nullable Intent data) {
                if (resultCode == Activity.RESULT_OK) {
                    MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) permissionActivity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    if (mediaProjectionManager != null) {
                        setScreenShotCapture(new ScreenShotCapture(mediaProjectionManager, data, resultCode));
                    }
                }
            }
        });
        activity.startActivity(new Intent(activity, ScreenshotPermissionActivity.class));
    }

    private void setScreenShotCapture(@NonNull ScreenShotCapture screenShotCapture) {
        this.screenShotCapture = screenShotCapture;
        shouldCaptureScreenshotWhenResumed = true;
    }

    private void captureScreenshot(@NonNull final ScreenShotCapture screenShotCapture) {
        final Activity currentActivity = TPAManager.getInstance().getCurrentActivity();
        if (currentActivity == null || !AppLifeCycle.isActivityValid(currentActivity)) {
            return;
        }

        final MediaProjection mediaProjection = screenShotCapture.getMediaProjection();
        if (mediaProjection == null) {
            TpaDebugging.log.e(TAG, "Could not access media projection for feedback, aborting.");
            return;
        }

        final int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
        DisplayMetrics metrics = currentActivity.getResources().getDisplayMetrics();
        int density = metrics.densityDpi;
        Display display = currentActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        final int height = size.y;
        final int width = size.x;
        final ImageReader imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        mediaProjection.createVirtualDisplay("tpascreenshot", width, height, density, flags, imageReader.getSurface(), null, null);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = null;
                Bitmap bitmap = null;
                String screenshotPath = null;

                try {
                    image = reader.acquireNextImage();
                    final Image.Plane[] planes = image.getPlanes();
                    final Buffer buffer = planes[0].getBuffer();

                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * width;

                    bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    screenshotPath = Screenshot.saveToDisk(currentActivity, bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }

                    if (image != null) {
                        image.close();
                    }

                    imageReader.close();
                    imageReader.getSurface().release();
                    mediaProjection.stop();
                }

                if (screenshotPath != null) {
                    showFeedbackActivity(currentActivity, screenshotPath);
                }
            }

        }, null);
    }

    @Override
    void onActivityResumed(@NonNull Activity activity) {
        //to avoid taking a screenshot while the activity to take picture of is paused (because of media project activity handling)
        //we check here if we are resuming after having opened the media project permission activity
        if (shouldCaptureScreenshotWhenResumed && activity.getComponentName().equals(screenshotComponentName)) {
            shouldCaptureScreenshotWhenResumed = false;

            //do capture with a delay to make sure the (potential) system diaTpaDebugging.log has disappeared before we take the screenshot
            activity.getWindow().getDecorView().getRootView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (screenShotCapture != null) {
                        captureScreenshot(screenShotCapture);
                        screenShotCapture = null;
                    }
                }
            }, 600);
        }
    }

    private static class ScreenShotCapture {

        @NonNull
        private MediaProjectionManager mediaProjectionManager;
        @Nullable
        private Intent permissionIntent;
        private int resultCode;

        ScreenShotCapture(@NonNull MediaProjectionManager mediaProjectionManager, @Nullable Intent permissionIntent, int resultCode) {
            this.mediaProjectionManager = mediaProjectionManager;
            this.permissionIntent = permissionIntent;
            this.resultCode = resultCode;
        }

        @Nullable
        MediaProjection getMediaProjection() {
            if (permissionIntent == null) {
                return null;
            }
            return mediaProjectionManager.getMediaProjection(resultCode, permissionIntent);
        }
    }
}
