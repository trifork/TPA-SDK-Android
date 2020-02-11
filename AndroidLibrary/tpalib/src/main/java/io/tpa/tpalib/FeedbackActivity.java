package io.tpa.tpalib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import io.tpa.tpalib.android.R;

public class FeedbackActivity extends Activity {

    private static final int animationLength = 300;

    private View buttonBarContainer;
    private View topBarContainer;
    private int bottomBarHeight;
    private int topBarHeight;

    private boolean isFeedbackUiShowing = false;

    private Timer showUiTimer = new Timer();
    private TimerTask showUiTimerTask;

    private ImageDrawingView screenshotView;
    private ImageButtonSelectColor colorButton;

    private String comment = "";
    private String screenshotPath;

    private Handler delayMainHandler;

    @Nullable
    private FeedbackManager feedbackManager;

    void setFeedbackManager(@Nullable FeedbackManager feedbackManager) {
        this.feedbackManager = feedbackManager;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenHelper.lockOrientation(this);

        delayMainHandler = new Handler(Looper.getMainLooper());

        setTitle(R.string.feedback_title);
        setContentView(R.layout.feedback_activity);

        screenshotView = findViewById(R.id.screenshot_view);
        screenshotView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideFeedbackUI(0);
                resetUiTimer();
                return false;
            }
        });

        // Get the ui elements
        buttonBarContainer = findViewById(R.id.feedback_button_bar);
        topBarContainer = findViewById(R.id.feedback_top_bar);

        bottomBarHeight = getResources().getDimensionPixelSize(R.dimen.ui_bottom_bar_height);
        topBarHeight = getResources().getDimensionPixelSize(R.dimen.ui_top_bar_height);

        setupFeedbackUI();

        // Load screenshot
        screenshotPath = getIntent().getStringExtra("screenshotPath");
        Bitmap screenshotBitmap = Screenshot.loadBitmapFromFile(screenshotPath);

        if (screenshotBitmap != null) {
            // Create mutable bitmap
            Bitmap mutableBitmap = screenshotBitmap.copy(Screenshot.BitmapConfig, true);
            screenshotView.setImageBitmap(mutableBitmap);
        }
    }

    @Override
    public void finish() {
        if (feedbackManager != null) {
            feedbackManager.startDetection();
            feedbackManager = null;
        }
        ScreenHelper.unlockOrientation(this);
        super.finish();
    }

    private void changeColor(int color) {
        if (screenshotView == null) {
            return;
        }

        screenshotView.setDrawColor(color);

        colorButton.changeColor(color);
    }

    /* **************** *
     * Button listeners *
     * **************** */

    private void setupFeedbackUI() {
        // Find UI
        findViewById(R.id.feedback_button_back).setOnClickListener(generalOnClickListener);
        findViewById(R.id.feedback_button_send).setOnClickListener(sendClickListener);

        findViewById(R.id.feedback_button_comment).setOnClickListener(commentClickListener);
        findViewById(R.id.feedback_button_undo).setOnClickListener(generalOnClickListener);
        findViewById(R.id.feedback_button_delete).setOnClickListener(generalOnClickListener);

        colorButton = findViewById(R.id.feedback_button_color);
        colorButton.setOnClickListener(colorClickListener);

        // Initially change color to color 1
        changeColor(getResources().getColor(R.color.ui_draw_color_1));
    }

    private View.OnClickListener generalOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.feedback_button_back) {
                finish();
                return;
            }

            if (v.getId() == R.id.feedback_button_undo) {
                screenshotView.undo();
                return;
            }

            if (v.getId() == R.id.feedback_button_delete) {
                screenshotView.clearDrawings();
            }
        }
    };

    private View.OnClickListener sendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bitmap bitmap = screenshotView.getDrawingBitmap();
            if (bitmap != null) {
                savePendingFeedback(bitmap);
            }
        }
    };

    private View.OnClickListener colorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ColorPickerPopup popup = new ColorPickerPopup(FeedbackActivity.this, new ColorPickerPopup.ColorChangedListener() {
                @Override
                public void colorChanged(int color) {
                    changeColor(color);
                }
            });

            popup.show(v);
        }
    };

    private View.OnClickListener commentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int statusBarHeight = ScreenHelper.getStatusBarHeight(FeedbackActivity.this);
            int top = getResources().getDimensionPixelSize(R.dimen.ui_top_bar_height) + statusBarHeight + 10;

            CommentPopup popup = new CommentPopup(FeedbackActivity.this, comment, top, new CommentPopup.CommentChangedListener() {
                @Override
                public void commentChanged(String comment) {
                    // Update comment
                    FeedbackActivity.this.comment = comment;
                }
            });
            popup.show(v);
        }
    };

    private void savePendingFeedback(final @NonNull Bitmap bitmap) {
        if (screenshotPath != null) {
            // Remove temporary screenshot file
            new File(screenshotPath).delete();
        }

        final FeedbackManager feedbackManager = this.feedbackManager; // Make sure we capture feedback manager before finishing

        Thread t = new Thread() {
            @Override
            public void run() {
                if (feedbackManager != null) {
                    ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOs);

                    byte[] mediaBytes = byteOs.toByteArray();

                    feedbackManager.sendFeedback(comment, mediaBytes);
                }
            }
        };
        t.start();

        Toast.makeText(FeedbackActivity.this, R.string.feedback_sent_toast, Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(0, R.anim.feedback_activity_send_animation);
    }


    /* ***************** *
     * Animation stuff *
     * ***************** */

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        prepareAnimations();
        showFeedbackUI(800);
    }

    private void prepareAnimations() {
        // Hide below content
        setVerticalMargin(topBarContainer, -topBarHeight, 0);
        setVerticalMargin(buttonBarContainer, 0, -bottomBarHeight);
    }

    private void showFeedbackUI(final int delay) {
        if (!isFeedbackUiShowing) {
            isFeedbackUiShowing = true;

            delayMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator animTopBar = ObjectAnimator.ofFloat(topBarContainer, "translationY", topBarHeight);
                    animTopBar.setDuration(animationLength);
                    animTopBar.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            topBarContainer.setVisibility(View.VISIBLE);
                        }
                    });
                    ObjectAnimator animButtonBar = ObjectAnimator.ofFloat(buttonBarContainer, "translationY", -bottomBarHeight);
                    animButtonBar.setDuration(animationLength);
                    animButtonBar.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            buttonBarContainer.setVisibility(View.VISIBLE);
                        }
                    });

                    animTopBar.start();
                    animButtonBar.start();
                }
            }, delay);
        }
    }

    private void hideFeedbackUI(final int delay) {
        if (isFeedbackUiShowing) {
            isFeedbackUiShowing = false;

            delayMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator animTopBar = ObjectAnimator.ofFloat(topBarContainer, "translationY", -topBarHeight);
                    animTopBar.setDuration(animationLength);

                    ObjectAnimator animButtonBar = ObjectAnimator.ofFloat(buttonBarContainer, "translationY", bottomBarHeight);
                    animButtonBar.setDuration(animationLength);

                    animTopBar.start();
                    animButtonBar.start();
                }
            }, delay);
        }
    }

    /**
     * Only use this method to manipulate margins of views inside of the rootView (RelativeLayout).
     */
    private void setVerticalMargin(View v, int top, int bottom) {
        ViewGroup.LayoutParams params = v.getLayoutParams();
        if (params instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
            relativeLayoutParams.setMargins(0, top, 0, bottom);
            v.setLayoutParams(relativeLayoutParams);
        }
    }


    /* ************************* *
     * Stuff related to UI timer *
     * ************************* */

    private void resetUiTimer() {
        if (showUiTimerTask != null) {
            showUiTimerTask.cancel();
        }
        showUiTimerTask = new ShowUiTimerTask();
        showUiTimer.schedule(showUiTimerTask, 1100L);
    }

    private class ShowUiTimerTask extends TimerTask {

        @Override
        public void run() {
            showFeedbackUI(0);
        }
    }
}
