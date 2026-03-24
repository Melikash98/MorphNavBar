package com.melikash98.morphnavbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class BackgroundView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public BackgroundView(Context context) {
        super(context);
    }

    public BackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        paint.setShader(new LinearGradient(
                0f, 0f, w, h,
                new int[] {0xFF7DF2E6, 0xFF90F5EF, 0xFF5AE2D0, 0xFF2CCABB},
                new float[] {0f, 0.45f, 0.80f, 1f},
                Shader.TileMode.CLAMP
        ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        circlePaint.setShader(new RadialGradient(
                getWidth() * 0.12f, getHeight() * 0.08f, getWidth() * 0.28f,
                new int[] {0x44FFFFFF, 0x10FFFFFF, 0x00FFFFFF},
                new float[] {0f, 0.6f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(getWidth() * 0.12f, getHeight() * 0.08f, getWidth() * 0.26f, circlePaint);

        circlePaint.setShader(new RadialGradient(
                getWidth() * 0.72f, getHeight() * 0.18f, getWidth() * 0.40f,
                new int[] {0x33FFFFFF, 0x10FFFFFF, 0x00FFFFFF},
                new float[] {0f, 0.7f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(getWidth() * 0.72f, getHeight() * 0.18f, getWidth() * 0.38f, circlePaint);

        circlePaint.setShader(new RadialGradient(
                getWidth() * 0.14f, getHeight() * 0.92f, getWidth() * 0.45f,
                new int[] {0x1D000000, 0x05000000, 0x00000000},
                new float[] {0f, 0.7f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(getWidth() * 0.14f, getHeight() * 0.92f, getWidth() * 0.42f, circlePaint);

        circlePaint.setShader(new RadialGradient(
                getWidth() * 0.95f, getHeight() * 0.95f, getWidth() * 0.35f,
                new int[] {0x22000000, 0x08000000, 0x00000000},
                new float[] {0f, 0.7f, 1f},
                Shader.TileMode.CLAMP
        ));
        canvas.drawCircle(getWidth() * 0.95f, getHeight() * 0.95f, getWidth() * 0.30f, circlePaint);
    }

}
