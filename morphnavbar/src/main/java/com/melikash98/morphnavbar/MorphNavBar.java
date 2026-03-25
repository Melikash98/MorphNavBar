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
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path barPath = new Path();

    private final RectF tmpRect = new RectF();
    private final RectF topLeft = new RectF();
    private final RectF topRight = new RectF();
    private final RectF bottomRight = new RectF();
    private final RectF bottomLeft = new RectF();
    private final RectF notchRect = new RectF();
    private final Rect iconBounds = new Rect();

    private final List<LiquidTabItem> items = new ArrayList<>();
    private Drawable[] inactiveIcons = new Drawable[0];
    private Drawable[] activeIcons = new Drawable[0];

    private OnTabSelectedListener listener;

    private int selectedIndex = 0;
    private int fromIndex = 0;
    private int toIndex = 0;
    private boolean animating = false;

    private float progress = 1f;
    private float bubbleCenterX = Float.NaN;
    private float bubbleScaleX = 1f;
    private float bubbleScaleY = 1f;
    private float bubbleLift = 0f;
    private float startCenterX = Float.NaN;
    private float endCenterX = Float.NaN;

    private ValueAnimator animator;

    private int barColor = Color.WHITE;
    private int shadowColor = 0x22000000;
    private int indicatorColor = 0x14000000;
    private int activeIconColor = Color.WHITE;
    private int inactiveIconColor = 0xFF13CFC0;

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
    private long animationDuration;

    private float barLeft;
    private float barTop;
    private float barRight;
    private float barBottom;

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
                indicatorColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_indicatorColor, 0x14000000);
                activeIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_activeIconColor, Color.WHITE);
                inactiveIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_inactiveIconColor, 0xFF13CFC0);

                barRadius = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barRadius, dp(22));
                barHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barHeight, dp(160));
                barSideMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barSideMargin, dp(36));
                barBottomMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barBottomMargin, dp(34));
                bubbleDiameter = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_bubbleDiameter, dp(92));
                itemIconSize = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_itemIconSize, dp(24));
                indicatorWidth = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_indicatorWidth, dp(120));
                indicatorHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_indicatorHeight, dp(2));
                shadowBlur = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowBlur, dp(16));
                shadowDy = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowDy, dp(6));
                animationDuration = a.getInt(R.styleable.LiquidBottomNavigationView_lbv_animationDuration, 420);
            } finally {
                a.recycle();
            }
        } else {
            barRadius = dp(22);
            barHeight = dp(160);
            barSideMargin = dp(36);
            barBottomMargin = dp(34);
            bubbleDiameter = dp(92);
            itemIconSize = dp(24);
            indicatorWidth = dp(120);
            indicatorHeight = dp(2);
            shadowBlur = dp(16);
            shadowDy = dp(6);
            animationDuration = 420;
        }

        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setColor(barColor);

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(barColor);
        if (shadowBlur > 0f) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            shadowPaint.setShadowLayer(shadowBlur, 0f, shadowDy, shadowColor);
        }

        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(indicatorColor);
    }

    public void setItems(@NonNull List<LiquidTabItem> newItems) {
        items.clear();
        items.addAll(newItems);
        buildDrawableCache();

        if (items.isEmpty()) {
            selectedIndex = 0;
            fromIndex = 0;
            toIndex = 0;
            bubbleCenterX = Float.NaN;
        } else {
            selectedIndex = clamp(selectedIndex, 0, items.size() - 1);
            fromIndex = selectedIndex;
            toIndex = selectedIndex;
            bubbleCenterX = Float.NaN;
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

    @Nullable
    public LiquidTabItem getSelectedItem() {
        if (items.isEmpty()) return null;
        return items.get(clamp(selectedIndex, 0, items.size() - 1));
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, false);
    }

    public void setSelectedIndex(int index, boolean animate) {
        if (items.isEmpty()) return;

        index = clamp(index, 0, items.size() - 1);

        if (!animate || getWidth() == 0 || selectedIndex == index) {
            stopAnimation();
            selectedIndex = index;
            fromIndex = index;
            toIndex = index;
            bubbleCenterX = tabCenterX(index);
            progress = 1f;
            bubbleScaleX = 1f;
            bubbleScaleY = 1f;
            bubbleLift = 0f;
            invalidate();
            dispatchSelected(index);
            return;
        }

        stopAnimation();
        animating = true;
        fromIndex = selectedIndex;
        toIndex = index;
        selectedIndex = index;

        startCenterX = tabCenterX(fromIndex);
        endCenterX = tabCenterX(toIndex);
        bubbleCenterX = startCenterX;
        progress = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.setInterpolator(new DecelerateInterpolator(1.35f));
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            bubbleCenterX = lerp(startCenterX, endCenterX, progress);
            float pulse = (float) Math.sin(Math.PI * progress);
            bubbleScaleX = 1f + 0.16f * pulse;
            bubbleScaleY = 1f - 0.08f * pulse;
            bubbleLift = -dp(3) * pulse;
            invalidate();
        });
        int finalIndex = index;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
                bubbleCenterX = endCenterX;
                progress = 1f;
                bubbleScaleX = 1f;
                bubbleScaleY = 1f;
                bubbleLift = 0f;
                animator = null;
                invalidate();
                dispatchSelected(finalIndex);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animator = null;
                animating = false;
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

    private void buildDrawableCache() {
        inactiveIcons = new Drawable[items.size()];
        activeIcons = new Drawable[items.size()];
        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem item = items.get(i);
            inactiveIcons[i] = tintCopy(item.getIcon(), inactiveIconColor);
            Drawable activeSource = item.getSelectedIcon() != null ? item.getSelectedIcon() : item.getIcon();
            activeIcons[i] = tintCopy(activeSource, activeIconColor);
        }
    }

    @Nullable
    private Drawable tintCopy(@Nullable Drawable drawable, @ColorInt int tint) {
        if (drawable == null) return null;
        Drawable copy = drawable.getConstantState() != null
                ? drawable.getConstantState().newDrawable().mutate()
                : drawable.mutate();
        copy.setTint(tint);
        return copy;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) (getPaddingLeft() + getPaddingRight() + dp(360));
        int desiredHeight = (int) Math.ceil(getPaddingTop() + getPaddingBottom() + barHeight + barBottomMargin);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) return;

        if (Float.isNaN(bubbleCenterX)) {
            bubbleCenterX = tabCenterX(selectedIndex);
        }

        drawBar(canvas);
        drawBottomIndicator(canvas);
        drawInactiveTabs(canvas);
        drawBubbleAndSelectedIcon(canvas);
    }

    private void drawBar(Canvas canvas) {
        float corner = barRadius;
        float nr = notchRadius() * (1f + 0.10f * (float) Math.sin(Math.PI * progress));
        float cx = clamp(bubbleCenterX, barLeft + corner + nr, barRight - corner - nr);

        topLeft.set(barLeft, barTop, barLeft + 2f * corner, barTop + 2f * corner);
        topRight.set(barRight - 2f * corner, barTop, barRight, barTop + 2f * corner);
        bottomRight.set(barRight - 2f * corner, barBottom - 2f * corner, barRight, barBottom);
        bottomLeft.set(barLeft, barBottom - 2f * corner, barLeft + 2f * corner, barBottom);
        notchRect.set(cx - nr, barTop - nr, cx + nr, barTop + nr);

        barPath.reset();
        barPath.moveTo(barLeft + corner, barTop);
        barPath.lineTo(cx - nr, barTop);
        barPath.addArc(notchRect, 180f, 180f);
        barPath.lineTo(barRight - corner, barTop);
        barPath.addArc(topRight, 270f, 90f);
        barPath.lineTo(barRight, barBottom - corner);
        barPath.addArc(bottomRight, 0f, 90f);
        barPath.lineTo(barLeft + corner, barBottom);
        barPath.addArc(bottomLeft, 90f, 90f);
        barPath.lineTo(barLeft, barTop + corner);
        barPath.addArc(topLeft, 180f, 90f);
        barPath.close();

        canvas.drawPath(barPath, shadowPaint);
        canvas.drawPath(barPath, barPaint);
    }

    private void drawBottomIndicator(Canvas canvas) {
        float cx = (barLeft + barRight) * 0.5f;
        float cy = barBottom - dp(22);
        tmpRect.set(
                cx - indicatorWidth * 0.5f,
                cy - indicatorHeight * 0.5f,
                cx + indicatorWidth * 0.5f,
                cy + indicatorHeight * 0.5f
        );
        canvas.drawRoundRect(tmpRect, indicatorHeight, indicatorHeight, indicatorPaint);
    }

    private void drawInactiveTabs(Canvas canvas) {
        for (int i = 0; i < items.size(); i++) {
            int alpha = 255;
            if (animating && i == fromIndex) {
                alpha = (int) (255f * (1f - progress));
            }
            drawIcon(canvas, inactiveIcons[i], i, alpha);
        }
    }

    private void drawBubbleAndSelectedIcon(Canvas canvas) {
        if (selectedIndex < 0 || selectedIndex >= items.size()) return;

        float cx = animating ? bubbleCenterX : tabCenterX(selectedIndex);
        float cy = iconCenterY() + bubbleLift;
        float r = bubbleDiameter * 0.5f;
        float rx = r * bubbleScaleX;
        float ry = r * bubbleScaleY;

        tmpRect.set(cx - rx, cy - ry, cx + rx, cy + ry);
        canvas.drawOval(tmpRect, barPaint);

        drawIcon(canvas, activeIcons[selectedIndex], selectedIndex, 255);
    }

    private void drawIcon(Canvas canvas, @Nullable Drawable drawable, int index, int alpha) {
        if (drawable == null) return;

        float cx = tabCenterX(index);
        float cy = iconCenterY();

        float size = itemIconSize;
        if (index == selectedIndex && !animating) {
            size *= 1.02f;
        }
        if (animating && index == toIndex) {
            size *= 1f + 0.04f * (float) Math.sin(Math.PI * progress);
        }

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

    private float tabCenterX(int index) {
        if (items.isEmpty()) return 0f;
        float segment = (barRight - barLeft) / items.size();
        return barLeft + segment * (index + 0.5f);
    }

    private float iconCenterY() {
        return barTop + barHeight * 0.36f;
    }

    private float notchRadius() {
        return bubbleDiameter * 0.22f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || items.isEmpty()) return super.onTouchEvent(event);

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
        if (x < barLeft || x > barRight || y < barTop - bubbleDiameter * 0.6f || y > barBottom) {
            return -1;
        }
        float segment = (barRight - barLeft) / items.size();
        int index = (int) ((x - barLeft) / segment);
        return clamp(index, 0, items.size() - 1);
    }

    public void setBarColor(@ColorInt int color) {
        barColor = color;
        barPaint.setColor(color);
        shadowPaint.setColor(color);
        invalidate();
    }

    public void setActiveIconColor(@ColorInt int color) {
        activeIconColor = color;
        buildDrawableCache();
        invalidate();
    }

    public void setInactiveIconColor(@ColorInt int color) {
        inactiveIconColor = color;
        buildDrawableCache();
        invalidate();
    }

    public void setShadowColor(@ColorInt int color) {
        shadowColor = color;
        shadowPaint.setShadowLayer(shadowBlur, 0f, shadowDy, color);
        invalidate();
    }

    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = Math.max(0L, animationDuration);
    }

    public void clearSelection() {
        stopAnimation();
        selectedIndex = 0;
        fromIndex = 0;
        toIndex = 0;
        bubbleCenterX = Float.NaN;
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

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
