package com.melikash98.morphnavbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Floating liquid bottom navigation inspired by the supplied video.
 * <p>
 * The component is intentionally self-contained: it draws the bar, the active bubble,
 * the center indicator line, and the built-in outline icons directly on canvas.
 */
public class MorphNavBar extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 360;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();
    private final RectF tempRect = new RectF();
    private final Path bubblePath = new Path();

    private final List<LiquidTabItem> items = new ArrayList<>();
    private final List<Float> centerXs = new ArrayList<>();

    private final FastOutSlowInInterpolator motionInterpolator = new FastOutSlowInInterpolator();
    private final DecelerateInterpolator iconInterpolator = new DecelerateInterpolator();

    private int selectedIndex = 0;
    private int fromIndex = 0;
    private int toIndex = 0;
    private float progress = 1f;
    private ValueAnimator animator;

    private OnTabSelectedListener listener;

    private int barColor;
    private int shadowColor;
    private int indicatorColor;
    private int selectedColor;
    private int unselectedColor;
    private int activeIconColor;
    private int inactiveIconColor;

    private float barRadius;
    private float barHeight;
    private float barSideMargin;
    private float barBottomMargin;
    private float bubbleDiameter;
    private float itemIconSize;
    private float indicatorWidth;
    private float indicatorHeight;
    private float shadowBlur;
    private float shadowDy;
    private int animationDuration;

    private float bubbleCenterY;

    public  MorphNavBar(@NonNull Context context) {
        this(context, null);
    }

    public  MorphNavBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public  MorphNavBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults();
        readAttributes(context, attrs, defStyleAttr);
        initPaints();
        setClickable(true);
        setFocusable(true);
        setWillNotDraw(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void initDefaults() {
        barColor = Color.WHITE;
        shadowColor = Color.parseColor("#22000000");
        indicatorColor = Color.parseColor("#E3E3E3");
        selectedColor = Color.parseColor("#00CFC0");
        unselectedColor = Color.parseColor("#00CFC0");
        activeIconColor = Color.WHITE;
        inactiveIconColor = Color.parseColor("#00CFC0");

        // Smaller, closer to the reference video.
        barRadius = dp(22);
        barHeight = dp(76);
        barSideMargin = dp(28);
        barBottomMargin = dp(20);
        bubbleDiameter = dp(48);
        itemIconSize = dp(21);
        indicatorWidth = dp(92);
        indicatorHeight = dp(2);
        shadowBlur = dp(14);
        shadowDy = dp(5);
        animationDuration = DEFAULT_ANIMATION_DURATION;
    }

    private void readAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LiquidBottomNavigationView, defStyleAttr, 0);
        try {
            barColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_barColor, barColor);
            shadowColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_shadowColor, shadowColor);
            indicatorColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_indicatorColor, indicatorColor);
            selectedColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_selectedColor, selectedColor);
            unselectedColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_unselectedColor, unselectedColor);
            activeIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_activeIconColor, activeIconColor);
            inactiveIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_inactiveIconColor, inactiveIconColor);

            barRadius = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barRadius, barRadius);
            barHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barHeight, barHeight);
            barSideMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barSideMargin, barSideMargin);
            barBottomMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barBottomMargin, barBottomMargin);
            bubbleDiameter = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_bubbleDiameter, bubbleDiameter);
            itemIconSize = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_itemIconSize, itemIconSize);
            indicatorWidth = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_indicatorWidth, indicatorWidth);
            indicatorHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_indicatorHeight, indicatorHeight);
            shadowBlur = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowBlur, shadowBlur);
            shadowDy = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowDy, shadowDy);
            animationDuration = a.getInteger(R.styleable.LiquidBottomNavigationView_lbv_animationDuration, animationDuration);
        } finally {
            a.recycle();
        }
    }

    private void initPaints() {
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(shadowColor);

        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(indicatorColor);

        activeFillPaint.setStyle(Paint.Style.FILL);
        activeFillPaint.setColor(selectedColor);

        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);
        iconPaint.setStrokeWidth(dp(1.9f));
        iconPaint.setColor(inactiveIconColor);

        activeIconPaint.setStyle(Paint.Style.STROKE);
        activeIconPaint.setStrokeCap(Paint.Cap.ROUND);
        activeIconPaint.setStrokeJoin(Paint.Join.ROUND);
        activeIconPaint.setStrokeWidth(dp(1.9f));
        activeIconPaint.setColor(activeIconColor);
    }

    public void setTabs(@NonNull List<LiquidTabItem> tabs) {
        items.clear();
        items.addAll(tabs);

        if (selectedIndex >= items.size()) {
            selectedIndex = 0;
        }

        fromIndex = selectedIndex;
        toIndex = selectedIndex;
        progress = 1f;

        rebuildCenters();
        requestLayout();
        invalidate();
    }

    public void setTabs(@NonNull LiquidTabItem... tabs) {
        items.clear();
        Collections.addAll(items, tabs);

        if (selectedIndex >= items.size()) {
            selectedIndex = 0;
        }

        fromIndex = selectedIndex;
        toIndex = selectedIndex;
        progress = 1f;

        rebuildCenters();
        requestLayout();
        invalidate();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }

    public void setSelectedIndex(int index, boolean animate) {
        if (items.isEmpty() || index < 0 || index >= items.size() || index == selectedIndex) {
            return;
        }

        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        if (!animate || centerXs.isEmpty()) {
            selectedIndex = index;
            fromIndex = index;
            toIndex = index;
            progress = 1f;
            if (listener != null) {
                listener.onTabSelected(index, items.get(index));
            }
            invalidate();
            return;
        }

        fromIndex = selectedIndex;
        toIndex = index;
        progress = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.setInterpolator(motionInterpolator);
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                selectedIndex = toIndex;
                fromIndex = selectedIndex;
                progress = 1f;

                if (listener != null && selectedIndex >= 0 && selectedIndex < items.size()) {
                    listener.onTabSelected(selectedIndex, items.get(selectedIndex));
                }
                invalidate();
            }
        });
        animator.start();
    }

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void setBarColor(@ColorInt int color) {
        barColor = color;
        barPaint.setColor(color);
        invalidate();
    }

    public void setSelectedColor(@ColorInt int color) {
        selectedColor = color;
        activeFillPaint.setColor(color);
        invalidate();
    }

    public void setUnselectedColor(@ColorInt int color) {
        unselectedColor = color;
        invalidate();
    }

    public void setActiveIconColor(@ColorInt int color) {
        activeIconColor = color;
        activeIconPaint.setColor(color);
        invalidate();
    }

    public void setInactiveIconColor(@ColorInt int color) {
        inactiveIconColor = color;
        iconPaint.setColor(color);
        invalidate();
    }

    public void setIndicatorColor(@ColorInt int color) {
        indicatorColor = color;
        indicatorPaint.setColor(color);
        invalidate();
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = Math.max(1, animationDuration);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + dp(360));
        int desiredHeight = (int) Math.ceil(
                getPaddingTop() + getPaddingBottom() + barHeight + bubbleDiameter * 0.28f + barBottomMargin
        );

        int width = resolveSize(desiredWidth, widthMeasureSpec);
        int height = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float left = getPaddingLeft() + barSideMargin;
        float right = w - getPaddingRight() - barSideMargin;
        float bottom = h - getPaddingBottom() - barBottomMargin;
        float top = bottom - barHeight;
        barRect.set(left, top, right, bottom);

        // Bubble sits slightly above the bar center like the reference.
        bubbleCenterY = top + bubbleDiameter * 0.42f;

        rebuildCenters();
    }

    private void rebuildCenters() {
        centerXs.clear();
        if (items.isEmpty()) {
            return;
        }

        float width = barRect.width();
        float seg = width / items.size();
        for (int i = 0; i < items.size(); i++) {
            centerXs.add(barRect.left + seg * (i + 0.5f));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) {
            return;
        }

        drawShadow(canvas);
        drawBar(canvas);
        drawIndicator(canvas);
        drawInactiveIcons(canvas);
        drawActiveBlob(canvas);
        drawActiveIcon(canvas);
    }

    private void drawShadow(Canvas canvas) {
        shadowPaint.setShadowLayer(shadowBlur, 0f, shadowDy, shadowColor);
        canvas.drawRoundRect(barRect, barRadius, barRadius, shadowPaint);
        shadowPaint.clearShadowLayer();
    }

    private void drawBar(Canvas canvas) {
        canvas.drawRoundRect(barRect, barRadius, barRadius, barPaint);
    }

    private void drawIndicator(Canvas canvas) {
        float cx = barRect.centerX();
        float cy = barRect.bottom - dp(18);

        tempRect.set(
                cx - indicatorWidth / 2f,
                cy - indicatorHeight / 2f,
                cx + indicatorWidth / 2f,
                cy + indicatorHeight / 2f
        );

        canvas.drawRoundRect(tempRect, indicatorHeight, indicatorHeight, indicatorPaint);
    }

    private void drawInactiveIcons(Canvas canvas) {
        float bubbleX = getBubbleCenterX();
        float influenceRadius = bubbleDiameter * 0.60f;

        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem item = items.get(i);
            float centerX = centerXs.get(i);
            float distance = Math.abs(centerX - bubbleX);
            float t = clamp(1f - (distance / influenceRadius), 0f, 1f);
            float eased = iconInterpolator.getInterpolation(t);

            float inactiveAlpha = 1f - 0.75f * eased;
            float activeAlpha = 0.92f * eased;

            Drawable base = item.getIcon();
            Drawable selected = item.getSelectedIcon();

            if (selected == null) {
                drawDrawable(canvas, base, centerX, bubbleCenterY, inactiveIconColor, inactiveAlpha);
            } else {
                drawDrawable(canvas, base, centerX, bubbleCenterY, inactiveIconColor, inactiveAlpha);
                drawDrawable(canvas, selected, centerX, bubbleCenterY, activeIconColor, activeAlpha);
            }
        }
    }

    private void drawActiveBlob(Canvas canvas) {
        float startX = getCenterX(fromIndex);
        float endX = getCenterX(toIndex);
        float t = motionInterpolator.getInterpolation(progress);
        float blobX = lerp(startX, endX, t);

        float r = bubbleDiameter / 2f;
        float startR = r * (0.98f - 0.18f * t);
        float endR = r * (0.80f + 0.12f * t);
        float bridgeR = r * (0.18f + 0.07f * (float) Math.sin(Math.PI * t));

        Path p1 = circlePath(startX, bubbleCenterY, startR);
        Path p2 = circlePath(endX, bubbleCenterY, endR);
        Path p3 = circlePath(
                blobX,
                bubbleCenterY - r * 0.12f * (1f - Math.abs(0.5f - t) * 2f),
                bridgeR
        );

        Path blob = new Path();
        blob.set(p1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Path merged = new Path(blob);
            merged.op(p2, Path.Op.UNION);
            merged.op(p3, Path.Op.UNION);
            blob = merged;
        } else {
            blob.addPath(p2);
            blob.addPath(p3);
        }

        bubblePath.reset();
        bubblePath.addPath(blob);
        canvas.drawPath(bubblePath, activeFillPaint);
    }

    private void drawActiveIcon(Canvas canvas) {
        if (items.isEmpty()) {
            return;
        }

        float startX = getCenterX(fromIndex);
        float endX = getCenterX(toIndex);
        float t = motionInterpolator.getInterpolation(progress);
        float bubbleX = lerp(startX, endX, t);

        int iconIndex = t < 0.52f ? fromIndex : toIndex;
        LiquidTabItem item = items.get(iconIndex);

        float scale = 1f + 0.08f * (float) Math.sin(Math.PI * t);
        Drawable selected = item.getSelectedIcon();

        if (selected != null) {
            drawDrawable(canvas, selected, bubbleX, bubbleCenterY, activeIconColor, 1f, scale);
        } else {
            drawDrawable(canvas, item.getIcon(), bubbleX, bubbleCenterY, activeIconColor, 1f, scale);
        }
    }

    private void drawDrawable(Canvas canvas,
                              @NonNull Drawable drawable,
                              float centerX,
                              float centerY,
                              @ColorInt int tint,
                              float alpha) {
        drawDrawable(canvas, drawable, centerX, centerY, tint, alpha, 1f);
    }

    private void drawDrawable(Canvas canvas,
                              @NonNull Drawable drawable,
                              float centerX,
                              float centerY,
                              @ColorInt int tint,
                              float alpha,
                              float scale) {
        Drawable d = drawable.mutate();
        d.setTint(tint);

        int size = Math.round(itemIconSize * scale);
        int half = size / 2;

        d.setBounds(
                Math.round(centerX) - half,
                Math.round(centerY) - half,
                Math.round(centerX) + half,
                Math.round(centerY) + half
        );

        int oldAlpha = d.getAlpha();
        d.setAlpha((int) (255f * clamp(alpha, 0f, 1f)));
        d.draw(canvas);
        d.setAlpha(oldAlpha);
    }

    private float getBubbleCenterX() {
        if (items.isEmpty()) {
            return barRect.centerX();
        }

        float startX = getCenterX(fromIndex);
        float endX = getCenterX(toIndex);
        float t = motionInterpolator.getInterpolation(progress);
        return lerp(startX, endX, t);
    }

    private float getCenterX(int index) {
        if (centerXs.isEmpty()) {
            return barRect.centerX();
        }
        index = Math.max(0, Math.min(index, centerXs.size() - 1));
        return centerXs.get(index);
    }

    private Path circlePath(float cx, float cy, float radius) {
        Path p = new Path();
        p.addCircle(cx, cy, radius, Path.Direction.CW);
        return p;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (items.isEmpty()) {
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                int index = hitTest(event.getX(), event.getY());
                if (index != -1) {
                    setSelectedIndex(index, true);
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int hitTest(float x, float y) {
        if (centerXs.isEmpty()) {
            return -1;
        }

        float top = barRect.top - bubbleDiameter * 0.18f;
        float bottom = barRect.bottom;
        if (y < top || y > bottom) {
            return -1;
        }

        float segmentWidth = barRect.width() / items.size();
        int index = (int) ((x - barRect.left) / segmentWidth);
        if (index < 0 || index >= items.size()) {
            return -1;
        }
        return index;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    private float dp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
