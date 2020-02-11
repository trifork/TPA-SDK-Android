package io.tpa.tpalib;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import io.tpa.tpalib.android.R;

public class ColorPickerPopup {

    public interface ColorChangedListener {

        void colorChanged(int color);
    }

    @NonNull
    private Activity mActivity;
    @NonNull
    private PopupWindow mWindow;
    @NonNull
    private LinearLayout mView;

    private ColorChangedListener listener;

    public ColorPickerPopup(@NonNull Activity activity, @Nullable ColorChangedListener listener) {
        mActivity = activity;
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        mView = (LinearLayout) layoutInflater.inflate(R.layout.color_picker, null);

        this.listener = listener;

        addClickListeners();

        mWindow = new PopupWindow(activity);
        mWindow.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.popup_background_left));
        mWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setWidth(1);
        mWindow.setHeight(1);
        mWindow.setTouchable(true);
        mWindow.setFocusable(true);
        mWindow.setOutsideTouchable(true);
        mWindow.setAnimationStyle(R.style.PopupAnimations);
        mWindow.setContentView(mView);
    }

    public void show(final @NonNull View anchor) {
        if (!mActivity.isFinishing()) {
            mWindow.showAtLocation(anchor, Gravity.START | Gravity.BOTTOM, anchor.getRight() / 2 - 25, anchor.getBottom());
        }
    }

    private void addClickListeners() {
        for (int i = 0; i < mView.getChildCount(); i++) {
            View child = mView.getChildAt(i);
            if (child instanceof ViewGroup) {
                continue;
            }
            child.setOnClickListener(buttonOnClickListener);
        }
    }

    private View.OnClickListener buttonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v instanceof Button) {

                if (v.getBackground() instanceof ColorDrawable) {
                    ColorDrawable drawableBackground = (ColorDrawable) v.getBackground();

                    if (listener != null) {
                        listener.colorChanged(drawableBackground.getColor());
                    }
                }

                mWindow.dismiss();
            }
        }
    };
}
