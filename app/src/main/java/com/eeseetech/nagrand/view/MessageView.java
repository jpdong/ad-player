package com.eeseetech.nagrand.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by dong on 2017/4/28.
 */

public class MessageView extends android.support.v7.widget.AppCompatTextView implements Runnable {

    private static final String TAG = "nagrand";

    private int currentScrollPos = -getWidth();
    private int nextDelay = 30;
    private int textWidth = 0;
    private boolean flag = false;
    private boolean isMeasured = false;
    private StatusCallback mStatusCallback;

    public MessageView(Context context) {
        super(context);
        initSetup();
    }

    public MessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initSetup();
    }

    private void initSetup() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public interface StatusCallback {
        void finish();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isMeasured) {
            getTextWidth();
            isMeasured = true;
        }
    }

    @Override
    public void run() {
        this.currentScrollPos += 1;
        scrollTo(this.currentScrollPos, 0);
        if (currentScrollPos >= textWidth) {
            currentScrollPos = -this.getWidth();
            flag = true;
            this.setVisibility(GONE);
            if (mStatusCallback != null) {
                mStatusCallback.finish();
            }
        }
        if (!flag) {
            postDelayed(this, nextDelay);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    private void getTextWidth() {
        Paint paint = this.getPaint();
        String s = this.getText().toString();
        if (s == null) {
            textWidth = 0;
        }
        textWidth = (int) paint.measureText(s);
    }

    public void showMessage() {
        flag = false;
        isMeasured = false;
        this.removeCallbacks(this);
        this.currentScrollPos = -this.getWidth();
        if (this.getVisibility() != VISIBLE) {
            this.setVisibility(VISIBLE);
        }
        post(this);
    }

    public void setStatusCallback(StatusCallback callback) {
        mStatusCallback = callback;
    }
}
