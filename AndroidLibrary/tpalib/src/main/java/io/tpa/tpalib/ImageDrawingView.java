package io.tpa.tpalib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.tpa.tpalib.android.R;

class ImageDrawingView extends ImageView {

    private static class PathColorPair {

        Path path;
        public int color;

        PathColorPair(Path path, int color) {
            this.path = path;
            this.color = color;
        }
    }

    private boolean drawingThresholdMet = false;
    private Bitmap originalBitmap;
    private Bitmap bitmap;
    private Canvas canvas;

    private Path drawPath;
    private Path circlePath;

    private Paint bitmapPaint;
    private Paint mainPaint;
    private Paint circlePaint;

    private List<PathColorPair> paths = new ArrayList<>();

    public ImageDrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ImageDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageDrawingView(Context context) {
        super(context);
        init(context);
    }

    @Override
    public void setImageBitmap(@Nullable Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        if (!bitmap.isMutable()) {
            throw new RuntimeException("Bitmap must be mutable");
        }
        super.setImageBitmap(bitmap);

        canvas = new Canvas(bitmap);
        this.bitmap = bitmap;
        originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
    }

    private void init(Context ctx) {
        drawPath = new Path();

        float circleWith = ctx.getResources().getDimension(R.dimen.ui_draw_circle_width);
        float paintWith = ctx.getResources().getDimension(R.dimen.ui_draw_paint_width);

        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLUE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(circleWith);

        mainPaint = new Paint();
        mainPaint.setAntiAlias(true);
        mainPaint.setDither(true);
        mainPaint.setColor(Color.GREEN);
        mainPaint.setStyle(Paint.Style.STROKE);
        mainPaint.setStrokeJoin(Paint.Join.ROUND);
        mainPaint.setStrokeCap(Paint.Cap.ROUND);
        mainPaint.setStrokeWidth(paintWith);

        bitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // "Clear" screen
        if (originalBitmap != null) {
            canvas.drawBitmap(originalBitmap, 0, 0, bitmapPaint);
        }

        // Save current color
        int currentColor = mainPaint.getColor();

        // Draw all "old" paths
        for (PathColorPair p : paths) {
            mainPaint.setColor(p.color);
            canvas.drawPath(p.path, mainPaint);
        }

        // Restore current color
        mainPaint.setColor(currentColor);

        canvas.drawPath(drawPath, mainPaint);
        canvas.drawPath(circlePath, circlePaint);
    }

    public Bitmap getDrawingBitmap() {
        draw(canvas);
        return bitmap;
    }

    public int getDrawColor() {
        return mainPaint.getColor();
    }

    public void setDrawColor(int color) {
        mainPaint.setColor(color);
    }

    public void clearDrawings() {
        paths.clear();
        invalidate();
    }

    public void undo() {
        if (paths.size() > 0) {
            paths.remove(paths.size() - 1); // Second to last
            invalidate();
        }
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touchStart(float x, float y) {
        drawingThresholdMet = false;
        drawPath.reset();
        drawPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            drawingThresholdMet = true;
            drawPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void touchUp() {
        drawPath.lineTo(mX, mY);
        circlePath.reset();

        // commit the path to our offscreen
        if (canvas != null) {
            canvas.drawPath(drawPath, mainPaint);
        }

        // Save this path and create a new one (if it's not empty)
        if (drawingThresholdMet) {
            paths.add(new PathColorPair(drawPath, getDrawColor()));
            drawPath = new Path();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}