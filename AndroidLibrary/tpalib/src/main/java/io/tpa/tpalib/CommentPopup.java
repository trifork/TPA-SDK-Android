package io.tpa.tpalib;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import io.tpa.tpalib.android.R;

class CommentPopup {

    @NonNull
    private Activity mActivity;
    @NonNull
    private PopupWindow mWindow;
    @NonNull
    private View mView;
    private int top;

    public interface CommentChangedListener {

        void commentChanged(String comment);
    }

    public CommentPopup(@NonNull Activity activity, @Nullable String existingComment, int top, final @Nullable CommentChangedListener listener) {
        mActivity = activity;
        this.top = top;
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        mView = layoutInflater.inflate(R.layout.comment_popup, null);

        mWindow = new PopupWindow(activity);
        mWindow.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.popup_background));
        mWindow.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_FROM_FOCUSABLE);
        mWindow.setWidth(1);
        mWindow.setHeight(1);
        mWindow.setFocusable(true);
        mWindow.setAnimationStyle(R.style.PopupAnimations);
        mWindow.setContentView(mView);

        // Listeners

        final EditText comment = mView.findViewById(R.id.feedback_comment_text);
        if (existingComment != null) {
            comment.setText(existingComment);
            comment.setSelection(existingComment.length());
        }

        mView.findViewById(R.id.feedback_button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment.setText("");
            }
        });

        mView.findViewById(R.id.feedback_button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.commentChanged(comment.getText().toString());
                }
                mWindow.dismiss();
            }
        });

        mView.findViewById(R.id.feedback_button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindow.dismiss();
            }
        });
    }

    public void show(final @NonNull View anchor) {
        if (!mActivity.isFinishing()) {
            mWindow.showAtLocation(anchor, Gravity.TOP | Gravity.CENTER, 0, top);
        }
    }
}
