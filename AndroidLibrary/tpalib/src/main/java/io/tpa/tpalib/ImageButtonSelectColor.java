package io.tpa.tpalib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageButton;

import io.tpa.tpalib.android.R;

public class ImageButtonSelectColor extends ImageButton {

    private Bitmap bitmap;
    private Bitmap mask;
    private Bitmap filteredBitmap;
    private Paint colorFilterPaint;

    public ImageButtonSelectColor(Context context) {
        this(context, null);
    }

    public ImageButtonSelectColor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageButtonSelectColor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_border_color_white_24dp);
        mask = BitmapFactory.decodeResource(getResources(), R.drawable.ic_border_mask);

        filteredBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        colorFilterPaint = new Paint();
    }

    public void changeColor(int color) {
        float[] src = {
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 255,
                1, 1, 1, -1, 0,
        };
        ColorMatrix cm = new ColorMatrix(src);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);

        Paint maskPaint = new Paint();
        maskPaint.setColorFilter(filter);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Canvas c = new Canvas(filteredBitmap);
        c.drawBitmap(bitmap, 0, 0, null);
        c.drawBitmap(mask, 0, 0, maskPaint);

        colorFilterPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        int cx = (getWidth() - bitmap.getWidth()) >> 1;
        int cy = (getHeight() - bitmap.getHeight()) >> 1;

        canvas.save();

        canvas.drawBitmap(bitmap, cx, cy, null);
        canvas.drawBitmap(filteredBitmap, cx, cy, colorFilterPaint);
        canvas.restore();
    }
}
