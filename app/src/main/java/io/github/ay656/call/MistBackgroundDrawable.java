package io.github.ay656.call;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

class MistBackgroundDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    public void draw(Canvas canvas) {
        int w = getBounds().width();
        int h = getBounds().height();
        if (w <= 0 || h <= 0) return;

        // Layer 1 — soft diagonal base gradient
        paint.setShader(new LinearGradient(
                0, 0, w, h,
                new int[]{
                        Color.rgb(224, 218, 210),
                        Color.rgb(218, 224, 220),
                        Color.rgb(194, 214, 218)},
                new float[]{0f, 0.45f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(getBounds(), paint);

        // Layer 2 — warm peach glow top-left
        paint.setShader(new RadialGradient(
                w * 0.18f, h * 0.12f, w * 0.52f,
                Color.argb(98, 238, 196, 188),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(getBounds(), paint);

        // Layer 3 — cool teal glow bottom-right
        paint.setShader(new RadialGradient(
                w * 0.84f, h * 0.72f, w * 0.58f,
                Color.argb(82, 168, 200, 208),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(getBounds(), paint);

        // Layer 4 — subtle lavender accent mid-right
        paint.setShader(new RadialGradient(
                w * 0.65f, h * 0.38f, w * 0.44f,
                Color.argb(52, 208, 198, 212),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(getBounds(), paint);

        // Layer 5 — overall frost veil
        paint.setShader(null);
        paint.setColor(Color.argb(22, 255, 255, 255));
        canvas.drawRect(getBounds(), paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.OPAQUE;
    }
}
