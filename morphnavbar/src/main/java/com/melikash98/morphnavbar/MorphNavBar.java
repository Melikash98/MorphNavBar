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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
 * A reusable liquid bottom navigation bar for Android.
 *
 * The view is fully data-driven:
 * - icons are supplied by the consumer
 * - the active state may use a separate selected drawable
 * - the bar morphs upward under the active tab
 * - the moving bubble is drawn as a real animated blob
 */

public class MorphNavBar extends View {
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeBubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();
    private final RectF bubbleRect = new RectF();
    private final Rect iconBounds = new Rect();

    private final List<LiquidTabItem> items = new ArrayList<>();
    private Drawable[] inactiveDrawables = new Drawable[0];
    private Drawable[] activeDrawables = new Drawable[0];

    private OnTabSelectedListener listener;

    private int selectedIndex = 0;

    private int barColor = Color.WHITE;
    private int shadowColor = 0x22000000;
    private int activeIconColor = Color.WHITE;
    private int inactiveIconColor = 0xFF13CFC0;

    private float barRadius;
    private float barHeight;
    private float barSideMargin;
    private float barBottomMargin;
    private float bubbleDiameter;
    private float itemIconSize;
    private float shadowBlur;
    private float shadowDy;
    private long animationDuration;

    private float barLeft;
    private float barTop;
    private float barRight;
    private float barBottom;

    private float bubbleCenterX = Float.NaN;
    private float bubbleCenterY = Float.NaN;

    private boolean animating = false;
    private float animT = 1f;
    private float startBubbleX;
    private float endBubbleX;

    private ValueAnimator animator;

    private final int touchSlop;
    private float downX;
    private float downY;
    private int downIndex = -1;
    private boolean movedTooFar = false;

    public MorphNavBar(@NonNull Context context) {
        this(context, null);
    }

    public MorphNavBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MorphNavBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setClickable(true);
        setFocusable(true);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LiquidBottomNavigationView, defStyleAttr, 0);
            try {
                barColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_barColor, Color.WHITE);
                shadowColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_shadowColor, 0x22000000);
                activeIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_activeIconColor, Color.WHITE);
                inactiveIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_inactiveIconColor, 0xFF13CFC0);

                barRadius = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barRadius, dp(18));
                barHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barHeight, dp(76));
                barSideMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barSideMargin, dp(28));
                barBottomMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barBottomMargin, dp(20));
                bubbleDiameter = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_bubbleDiameter, dp(48));
                itemIconSize = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_itemIconSize, dp(20));
                shadowBlur = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowBlur, dp(16));
                shadowDy = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowDy, dp(6));
                animationDuration = a.getInt(R.styleable.LiquidBottomNavigationView_lbv_animationDuration, 560);
            } finally {
                a.recycle();
            }
        } else {
            barRadius = dp(18);
            barHeight = dp(76);
            barSideMargin = dp(28);
            barBottomMargin = dp(20);
            bubbleDiameter = dp(48);
            itemIconSize = dp(20);
            shadowBlur = dp(16);
            shadowDy = dp(6);
            animationDuration = 560;
        }

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(barColor);

        activeBubblePaint.setStyle(Paint.Style.FILL);
        activeBubblePaint.setColor(inactiveIconColor);

        if (shadowBlur > 0f) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            shadowPaint.setShadowLayer(shadowBlur, 0f, shadowDy, shadowColor);
        }
    }

    public void setItems(@NonNull List<LiquidTabItem> newItems) {
        items.clear();
        items.addAll(newItems);
        buildDrawableCache();

        if (!items.isEmpty()) {
            selectedIndex = clamp(selectedIndex, 0, items.size() - 1);
            bubbleCenterX = Float.NaN;
            bubbleCenterY = Float.NaN;
        }

        requestLayout();
        invalidate();
    }

    public void setItems(@NonNull LiquidTabItem... tabItems) {
        ArrayList<LiquidTabItem> list = new ArrayList<>(tabItems.length);
        Collections.addAll(list, tabItems);
        setItems(list);
    }

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }

    public void setSelectedIndex(int index, boolean animate) {
        if (items.isEmpty()) return;

        index = clamp(index, 0, items.size() - 1);
        if (index == selectedIndex && bubbleCenterX == tabCenterX(index)) {
            return;
        }

        stopAnimation();

        if (!animate || getWidth() == 0) {
            selectedIndex = index;
            bubbleCenterX = tabCenterX(index);
            bubbleCenterY = bubbleBaseCenterY();
            invalidate();
            dispatchSelected(index);
            return;
        }

        int oldIndex = selectedIndex;
        selectedIndex = index;

        startBubbleX = tabCenterX(oldIndex);
        endBubbleX = tabCenterX(index);
        bubbleCenterX = startBubbleX;
        bubbleCenterY = bubbleBaseCenterY();

        animating = true;
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.setInterpolator(new DecelerateInterpolator(1.45f));
        animator.addUpdateListener(a -> {
            animT = (float) a.getAnimatedValue();
            float pulse = (float) Math.sin(Math.PI * animT);

            bubbleCenterX = lerp(startBubbleX, endBubbleX, animT);
            bubbleCenterY = bubbleBaseCenterY() - dp(2) * pulse;
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
                bubbleCenterX = tabCenterX(selectedIndex);
                bubbleCenterY = bubbleBaseCenterY();
                animator = null;
                invalidate();
                dispatchSelected(selectedIndex);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animating = false;
                animator = null;
            }
        });
        animator.start();
    }

    private void dispatchSelected(int index) {
        if (listener != null && index >= 0 && index < items.size()) {
            listener.onTabSelected(index, items.get(index));
        }

        LiquidTabItem item = getSelectedItem();
        if (item != null && item.getContentDescription() != null) {
            announceForAccessibility(item.getContentDescription());
        }
    }

    @Nullable
    public LiquidTabItem getSelectedItem() {
        if (items.isEmpty()) return null;
        return items.get(clamp(selectedIndex, 0, items.size() - 1));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) Math.ceil(
                getPaddingTop()
                        + getPaddingBottom()
                        + barHeight
                        + barBottomMargin
                        + bubbleDiameter * 0.65f
        );
        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + dp(360));

        int measuredW = resolveSize(desiredWidth, widthMeasureSpec);
        int measuredH = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(measuredW, measuredH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        barLeft = getPaddingLeft() + barSideMargin;
        barRight = w - getPaddingRight() - barSideMargin;
        barBottom = h - getPaddingBottom() - barBottomMargin;
        barTop = barBottom - barHeight;

        if (!items.isEmpty() && Float.isNaN(bubbleCenterX)) {
            bubbleCenterX = tabCenterX(selectedIndex);
        }
        if (Float.isNaN(bubbleCenterY)) {
            bubbleCenterY = bubbleBaseCenterY();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) return;

        drawBar(canvas);
        drawInactiveIcons(canvas);
        drawActiveBubble(canvas);
        drawSelectedIcon(canvas);
    }

    private void drawBar(Canvas canvas) {
        barRect.set(barLeft, barTop, barRight, barBottom);
        canvas.drawRoundRect(barRect, barRadius, barRadius, shadowPaint);
        canvas.drawRoundRect(barRect, barRadius, barRadius, barPaint);
    }

    private void drawInactiveIcons(Canvas canvas) {
        for (int i = 0; i < items.size(); i++) {
            int alpha = 255;

            if (i == selectedIndex) {
                alpha = animating ? (int) (255f * (1f - animT)) : 0;
            }

            Drawable d = inactiveDrawables[i];
            if (d != null) {
                drawDrawableAt(canvas, d, tabCenterX(i), iconCenterY(), itemIconSize, alpha);
            }
        }
    }

    private void drawActiveBubble(Canvas canvas) {
        float cx = animating ? bubbleCenterX : tabCenterX(selectedIndex);
        float cy = animating ? bubbleCenterY : bubbleBaseCenterY();

        float baseR = bubbleDiameter * 0.5f;
        float pulse = animating ? (float) Math.sin(Math.PI * animT) : 0f;

        float rx = baseR * (1f + 0.20f * pulse);
        float ry = baseR * (1f - 0.10f * pulse);

        bubbleRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
        canvas.drawOval(bubbleRect, activeBubblePaint);
    }

    private void drawSelectedIcon(Canvas canvas) {
        if (selectedIndex < 0 || selectedIndex >= items.size()) return;

        Drawable d = activeDrawables[selectedIndex];
        if (d == null) return;

        float iconSize = itemIconSize * 0.92f;
        drawDrawableAt(canvas, d, bubbleCenterX, bubbleCenterY, iconSize, 255);
    }

    private void drawDrawableAt(Canvas canvas, Drawable drawable, float cx, float cy, float size, int alpha) {
        int half = Math.max(1, Math.round(size * 0.5f));
        iconBounds.set(
                Math.round(cx - half),
                Math.round(cy - half),
                Math.round(cx + half),
                Math.round(cy + half)
        );
        drawable.setBounds(iconBounds);
        drawable.setAlpha(clamp(alpha, 0, 255));
        drawable.draw(canvas);
    }

    private void buildDrawableCache() {
        inactiveDrawables = new Drawable[items.size()];
        activeDrawables = new Drawable[items.size()];

        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem item = items.get(i);

            inactiveDrawables[i] = tintCopy(item.getIcon(), inactiveIconColor);

            Drawable activeSource = item.getSelectedIcon() != null ? item.getSelectedIcon() : item.getIcon();
            activeDrawables[i] = tintCopy(activeSource, activeIconColor);
        }
    }

    @Nullable
    private Drawable tintCopy(@Nullable Drawable drawable, int tint) {
        if (drawable == null) return null;

        Drawable copy;
        if (drawable.getConstantState() != null) {
            copy = drawable.getConstantState().newDrawable().mutate();
        } else {
            copy = drawable.mutate();
        }

        copy.setTint(tint);
        return copy;
    }

    private float tabCenterX(int index) {
        if (items.isEmpty()) return 0f;
        float segment = (barRight - barLeft) / items.size();
        return barLeft + segment * (index + 0.5f);
    }

    private float iconCenterY() {
        return barTop + barHeight * 0.55f;
    }

    private float bubbleBaseCenterY() {
        return barTop - bubbleDiameter * 0.16f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || items.isEmpty()) {
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                downIndex = hitTest(downX, downY);
                movedTooFar = downIndex < 0;
                if (downIndex >= 0) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                return super.onTouchEvent(event);

            case MotionEvent.ACTION_MOVE:
                if (movedTooFar) return false;

                if (Math.abs(event.getX() - downX) > touchSlop || Math.abs(event.getY() - downY) > touchSlop) {
                    movedTooFar = true;
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (!movedTooFar) {
                    int upIndex = hitTest(event.getX(), event.getY());
                    if (upIndex == downIndex && upIndex >= 0) {
                        performClick();
                        if (upIndex != selectedIndex) {
                            setSelectedIndex(upIndex, true);
                        }
                    }
                }
                movedTooFar = false;
                downIndex = -1;
                return true;

            case MotionEvent.ACTION_CANCEL:
                movedTooFar = false;
                downIndex = -1;
                return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int hitTest(float x, float y) {
        if (x < barLeft || x > barRight || y < barTop - bubbleDiameter || y > barBottom) {
            return -1;
        }
        float segment = (barRight - barLeft) / items.size();
        int index = (int) ((x - barLeft) / segment);
        return clamp(index, 0, items.size() - 1);
    }

    public void setBarColor(int color) {
        barColor = color;
        barPaint.setColor(color);
        shadowPaint.setColor(color);
        invalidate();
    }

    public void setActiveIconColor(int color) {
        activeIconColor = color;
        buildDrawableCache();
        invalidate();
    }

    public void setInactiveIconColor(int color) {
        inactiveIconColor = color;
        buildDrawableCache();
        invalidate();
    }

    public void setAnimationDuration(long duration) {
        animationDuration = Math.max(0L, duration);
    }

    public void clearSelection() {
        stopAnimation();
        selectedIndex = 0;
        bubbleCenterX = Float.NaN;
        bubbleCenterY = Float.NaN;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimation();
        super.onDetachedFromWindow();
    }

    private void stopAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        animating = false;
    }

    private float dp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
