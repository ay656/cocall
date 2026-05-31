package io.github.ay656.call;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

class GlassDrawable extends Drawable {
    private final float radius;
    private boolean pressed;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    GlassDrawable(float radius) {
        this.radius = radius;
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.6f);
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(2.8f);
    }

    void setPressed(boolean pressed) {
        if (this.pressed != pressed) {
            this.pressed = pressed;
            invalidateSelf();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        RectF bounds = new RectF(getBounds());
        float inset = 1.8f;

        // Shadow layer
        RectF shadowRect = new RectF(bounds);
        shadowRect.inset(2f, 2f);
        shadowRect.offset(0, 2.5f);
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setShadowLayer(10f, 0, 3f, Color.argb(40, 0, 0, 0));
        shadowPaint.setColor(Color.argb(pressed ? 18 : 35, 0, 0, 0));
        canvas.drawRoundRect(shadowRect, radius, radius, shadowPaint);

        // Base fill
        RectF fillRect = new RectF(bounds);
        fillRect.inset(inset, inset);

        fillPaint.setColor(pressed
                ? Color.argb(155, 245, 245, 248)
                : Color.argb(105, 252, 252, 255));
        canvas.drawRoundRect(fillRect, radius, radius, fillPaint);

        // Diagonal light sweep for depth
        int[] gradColors = pressed
                ? new int[]{
                    Color.argb(18, 255, 255, 255),
                    Color.argb(40, 210, 225, 240),
                    Color.argb(10, 255, 255, 255)}
                : new int[]{
                    Color.argb(38, 255, 255, 255),
                    Color.argb(10, 215, 230, 242),
                    Color.argb(28, 255, 255, 255)};

        gradientPaint.setShader(new LinearGradient(
                bounds.left, bounds.top,
                bounds.right, bounds.bottom,
                gradColors,
                new float[]{0f, 0.48f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(fillRect, radius, radius, gradientPaint);
        gradientPaint.setShader(null);

        // Top rim light — inner glow
        rimPaint.setShader(new LinearGradient(
                bounds.left, bounds.top,
                bounds.left, bounds.top + bounds.height() * 0.28f,
                new int[]{
                    Color.argb(85, 255, 255, 255),
                    Color.argb(0, 255, 255, 255)},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRoundRect(fillRect, radius, radius, rimPaint);
        rimPaint.setShader(null);

        // Border with subtle top-bottom opacity gradient
        borderPaint.setShader(new LinearGradient(
                bounds.left, bounds.top,
                bounds.left, bounds.bottom,
                new int[]{
                    Color.argb(175, 255, 255, 255),
                    Color.argb(pressed ? 130 : 100, 255, 255, 255)},
                new float[]{0f, 1f},
                Shader.TileMode.CLAMP));
        RectF borderRect = new RectF(fillRect);
        borderRect.inset(0.8f, 0.8f);
        canvas.drawRoundRect(borderRect, radius, radius, borderPaint);
        borderPaint.setShader(null);
    }

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        gradientPaint.setAlpha(alpha);
        rimPaint.setAlpha(alpha);
        borderPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        gradientPaint.setColorFilter(colorFilter);
        rimPaint.setColorFilter(colorFilter);
        borderPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
