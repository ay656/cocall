package io.github.ay656.call;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

class InitialDrawable extends Drawable {
    private final String text;
    private final int fillColor;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);

    InitialDrawable(String name) {
        this(name, 0);
    }

    InitialDrawable(String name, int color) {
        text = name == null || name.isEmpty() ? "\u5bb6" : name.substring(0, 1);
        fillColor = color != 0 ? color : Color.argb(235, 238, 243, 238);
        fill.setColor(fillColor);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(3f);
        stroke.setColor(Color.argb(210, 255, 255, 255));
        label.setColor(Color.argb(180, 48, 55, 55));
        label.setTextAlign(Paint.Align.CENTER);
        label.setFakeBoldText(true);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float radius = Math.min(bounds.width(), bounds.height()) / 2f - 3f;
        float cx = bounds.centerX();
        float cy = bounds.centerY();
        canvas.drawCircle(cx, cy, radius, fill);
        canvas.drawCircle(cx, cy, radius, stroke);

        label.setTextSize(radius * 0.95f);
        Paint.FontMetrics metrics = label.getFontMetrics();
        float y = cy - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(text, cx, y, label);
    }

    @Override
    public void setAlpha(int alpha) {
        fill.setAlpha(alpha);
        stroke.setAlpha(alpha);
        label.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        fill.setColorFilter(colorFilter);
        stroke.setColorFilter(colorFilter);
        label.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
