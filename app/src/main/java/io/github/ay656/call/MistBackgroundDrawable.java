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
        int width = getBounds().width();
        int height = getBounds().height();

        paint.setShader(new LinearGradient(
                0, 0, width, height,
                new int[]{Color.rgb(218, 226, 216), Color.rgb(224, 204, 198), Color.rgb(187, 210, 214)},
                new float[]{0f, 0.55f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(getBounds(), paint);

        paint.setShader(new RadialGradient(width * 0.22f, height * 0.18f, width * 0.55f,
                Color.argb(115, 236, 202, 196),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(getBounds(), paint);

        paint.setShader(new RadialGradient(width * 0.82f, height * 0.42f, width * 0.60f,
                Color.argb(100, 176, 203, 210),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP));
        canvas.drawRect(getBounds(), paint);

        paint.setShader(null);
        paint.setColor(Color.argb(34, 255, 255, 255));
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
