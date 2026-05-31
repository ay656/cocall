package io.github.ay656.call;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

class GlassDrawable extends Drawable {
    private final float radius;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);

    GlassDrawable(float radius) {
        this.radius = radius;
        fill.setColor(Color.argb(118, 255, 255, 255));
        stroke.setColor(Color.argb(180, 255, 255, 255));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(2f);
    }

    @Override
    public void draw(Canvas canvas) {
        RectF rect = new RectF(getBounds());
        rect.inset(1f, 1f);
        canvas.drawRoundRect(rect, radius, radius, fill);
        canvas.drawRoundRect(rect, radius, radius, stroke);
    }

    @Override
    public void setAlpha(int alpha) {
        fill.setAlpha(alpha);
        stroke.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        fill.setColorFilter(colorFilter);
        stroke.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
