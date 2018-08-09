package com.eeseetech.nagrand.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.eeseetech.nagrand.R;


/**
 * Created by XL on 2017/4/27.
 */

public class FadingSeekBar extends View {

    private Context context;
    private Paint paint;
    private TextPaint textPaint;

    private int width;
    private int height;
    private int defaultWidth;
    private int defaultHeight;
    private int progress;

    public FadingSeekBar(Context context) {
        super(context);
    }

    public FadingSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initPaint();
        measureDefaultWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setShader(new LinearGradient(0, -50, 0, height, Color.WHITE, Color.parseColor("#ff3f6cb0"), Shader.TileMode.REPEAT));
        RectF bar = new RectF(0, 0, progress * width / 100, height);
        canvas.drawRoundRect(bar, height / 2, height / 2, paint);
        if (progress > 10) {
            String progressStr = progress + "%";
            canvas.drawText(progressStr, progress * width / 100 - textPaint.measureText(progressStr) - height / 2, height * 3 / 4, textPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else {
            width = defaultWidth;
        }
        height = defaultHeight;
        setMeasuredDimension(width, height);
    }

    @Override
    public void onCancelPendingInputEvents() {
        super.onCancelPendingInputEvents();
    }

    private void initPaint() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(23);
    }

    private void measureDefaultWidth() {
        Bitmap shadow = BitmapFactory.decodeResource(context.getResources(), R.drawable.bar_shadow);
        defaultWidth = shadow.getWidth() - 7;
        defaultHeight = shadow.getHeight() - 7;
        shadow.recycle();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

}
