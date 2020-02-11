package io.tpa.tpalib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

class Screenshot {

    private static String TAG = "Screenshot";

    static final Bitmap.Config BitmapConfig = Bitmap.Config.ARGB_8888;

    @Nullable
    static String saveToDisk(@NonNull Context context, @NonNull final Bitmap bitmap) {
        try {
            // Save it to app cache - we'll delete it later
            File outputDir = context.getCacheDir();
            File outputFile = File.createTempFile("tpa_feedback", ".png", outputDir);

            FileOutputStream out = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            TpaDebugging.log.d(TAG, "File saved to: " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();

        } catch (Exception e) {
            TpaDebugging.log.e(TAG, "Couldn't save screenshot:", e);
            return null;
        }
    }

    @NonNull
    private static ArrayList<View> getAllChildren(@NonNull View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<>();

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    @Nullable
    static String captureScreenshotToFile(@NonNull Activity context) {
        Window window = context.getWindow();
        View view = window.getDecorView().getRootView();

        //run through all children and set drawing cache enabled. Necessary for e.g. custom views that draw to canvas but do not set it themselves
        ArrayList<View> allChildren = getAllChildren(view);
        View childView;
        boolean[] drawingCacheEnabledForViews = new boolean[allChildren.size()];
        for (int i = 0; i < allChildren.size(); i++) {
            childView = allChildren.get(i);
            drawingCacheEnabledForViews[i] = childView.isDrawingCacheEnabled();
            childView.setDrawingCacheEnabled(false);
            childView.setDrawingCacheEnabled(true);
        }

        int width = view.getWidth();
        int height = view.getHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, BitmapConfig);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        //restore drawingcache setting so we do not change layout behavior for the implementing app
        for (int i = 0; i < allChildren.size(); i++) {
            childView = allChildren.get(i);
            childView.setDrawingCacheEnabled(drawingCacheEnabledForViews[i]);
        }

        // Crop out status bar and navigation bar
        int statusBarHeight = ScreenHelper.getStatusBarHeight(context);
        int targetHeight = view.getHeight() - statusBarHeight;

        TpaDebugging.log.d(TAG, "Cropping statusBar = " + statusBarHeight);

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, statusBarHeight, view.getWidth(), targetHeight);

        return saveToDisk(context, croppedBitmap);
    }

    @Nullable
    static Bitmap loadBitmapFromFile(@NonNull String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = BitmapConfig;
        return BitmapFactory.decodeFile(path, options);
    }

}
