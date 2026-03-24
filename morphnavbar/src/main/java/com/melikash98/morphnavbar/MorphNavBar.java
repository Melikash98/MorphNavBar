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
 * A reusable liquid bottom navigation bar for Android.
 * <p>
 * The view is fully data-driven:
 * - icons are supplied by the consumer
 * - the active state may use a separate selected drawable
 * - the bar morphs upward under the active tab
 * - the moving bubble is drawn as a real animated blob
 */

public class MorphNavBar extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 300;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inactiveIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();
    private final RectF tempRect = new RectF();
    private final Path barPath = new Path();

    private final List<LiquidTabItem> items = new ArrayList<>();
    private final List<Float> centerXs = new ArrayList<>();

    private final FastOutSlowInInterpolator moveInterpolator = new FastOutSlowInInterpolator();
    private final DecelerateInterpolator iconInterpolator = new DecelerateInterpolator();

    private ValueAnimator animator;
    private OnTabSelectedListener listener;

    private int selectedIndex = 0;
    private int fromIndex = 0;
    private int toIndex = 0;
    private float progress = 1f;

    private boolean draggable = true;
    private boolean dragging = false;
    private float dragX;
    private float dragOffsetX;
    private int dragOriginIndex = 0;
    private int dragTargetIndex = 0;

    private int barColor;
    private int shadowColor;
    private int selectedColor;
    private int inactiveIconColor;
    private int activeIconColor;

    private float barRadius;
    private float barHeight;
    private float barSideMargin;
    private float barBottomMargin;
    private float bubbleDiameter;
    private float itemIconSize;
    private float shadowBlur;
    private float shadowDy;
    private int animationDuration;

    private float bubbleCenterY;

    public MorphNavBar(@NonNull Context context) {
        this(context, null);
    }

    public MorphNavBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MorphNavBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults();
        readAttributes(context, attrs, defStyleAttr);
        initPaints();
        setWillNotDraw(false);
        setClickable(true);
        setFocusable(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void initDefaults() {
        barColor = Color.WHITE;
        shadowColor = Color.parseColor("#22000000");
        selectedColor = Color.parseColor("#00CFC0");
        inactiveIconColor = Color.parseColor("#00CFC0");
        activeIconColor = Color.WHITE;

        barRadius = dp(22);
        barHeight = dp(76);
        barSideMargin = dp(18);
        barBottomMargin = dp(18);
        bubbleDiameter = dp(48);
        itemIconSize = dp(20);
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
            selectedColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_selectedColor, selectedColor);
            inactiveIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_inactiveIconColor, inactiveIconColor);
            activeIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_activeIconColor, activeIconColor);

            barRadius = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barRadius, barRadius);
            barHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barHeight, barHeight);
            barSideMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barSideMargin, barSideMargin);
            barBottomMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barBottomMargin, barBottomMargin);
            bubbleDiameter = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_bubbleDiameter, bubbleDiameter);
            itemIconSize = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_itemIconSize, itemIconSize);
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

        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setColor(selectedColor);

        inactiveIconPaint.setStyle(Paint.Style.STROKE);
        inactiveIconPaint.setStrokeCap(Paint.Cap.ROUND);
        inactiveIconPaint.setStrokeJoin(Paint.Join.ROUND);
        inactiveIconPaint.setStrokeWidth(dp(1.9f));
        inactiveIconPaint.setColor(inactiveIconColor);

        activeIconPaint.setStyle(Paint.Style.STROKE);
        activeIconPaint.setStrokeCap(Paint.Cap.ROUND);
        activeIconPaint.setStrokeJoin(Paint.Join.ROUND);
        activeIconPaint.setStrokeWidth(dp(1.9f));
        activeIconPaint.setColor(activeIconColor);
    }

    public void setDraggable(boolean draggable) {
        this.draggable = draggable;
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

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.listener = listener;
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
        animator.setInterpolator(moveInterpolator);
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                selectedIndex = toIndex;
                fromIndex = selectedIndex;
                progress = 1f;
                invalidate();
                if (listener != null && selectedIndex >= 0 && selectedIndex < items.size()) {
                    listener.onTabSelected(selectedIndex, items.get(selectedIndex));
                }
            }
        });
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + dp(360));
        int desiredHeight = (int) Math.ceil(
                getPaddingTop() + getPaddingBottom() + barHeight + bubbleDiameter * 0.35f + barBottomMargin
        );

        setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec)
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float left = getPaddingLeft() + barSideMargin;
        float right = w - getPaddingRight() - barSideMargin;
        float bottom = h - getPaddingBottom() - barBottomMargin;
        float top = bottom - barHeight;
        barRect.set(left, top, right, bottom);

        bubbleCenterY = top + bubbleDiameter * 0.42f;

        rebuildCenters();
    }

    private void rebuildCenters() {
        centerXs.clear();
        if (items.isEmpty()) {
            return;
        }
        float segment = barRect.width() / items.size();
        for (int i = 0; i < items.size(); i++) {
            centerXs.add(barRect.left + segment * (i + 0.5f));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (items.isEmpty()) {
            return;
        }

        float bubbleX = getCurrentBubbleX();
        float t = getMotionT();

        drawShadow(canvas, bubbleX, t);
        drawBar(canvas, bubbleX, t);
        drawIcons(canvas, bubbleX);
        drawBubble(canvas, bubbleX, t);
        drawActiveIcon(canvas, bubbleX, t);
    }

    private void drawShadow(Canvas canvas, float bubbleX, float t) {
        shadowPaint.setShadowLayer(shadowBlur, 0f, shadowDy, shadowColor);
        canvas.drawPath(buildBarPath(bubbleX, t), shadowPaint);
        shadowPaint.clearShadowLayer();
    }

    private void drawBar(Canvas canvas, float bubbleX, float t) {
        canvas.drawPath(buildBarPath(bubbleX, t), barPaint);
    }

    /**
     * Soft rounded top bulge that stays smooth at the first and last items.
     */
    private Path buildBarPath(float bubbleX, float t) {
        barPath.reset();

        float left = barRect.left;
        float top = barRect.top;
        float right = barRect.right;
        float bottom = barRect.bottom;
        float r = barRadius;

        float pulse = (float) Math.sin(Math.PI * t);

        float baseWidth = bubbleDiameter * (1.10f + 0.08f * pulse);
        float sideRoom = Math.min(bubbleX - left, right - bubbleX);
        float edgeFit = clamp(sideRoom / (bubbleDiameter * 0.75f), 0.58f, 1f);
        float bumpWidth = baseWidth * edgeFit;

        float safeLeft = left + r + bumpWidth * 0.50f;
        float safeRight = right - r - bumpWidth * 0.50f;
        float bumpCenterX = clamp(bubbleX, safeLeft, safeRight);

        float bumpDepth = (dp(7f) + dp(4f) * pulse) * (0.78f + 0.22f * edgeFit);
        float bumpTop = top - bumpDepth;

        float bumpLeft = bumpCenterX - bumpWidth / 2f;
        float bumpRight = bumpCenterX + bumpWidth / 2f;

        float shoulder = bumpWidth * 0.20f;
        float crown = bumpWidth * 0.08f;

        barPath.moveTo(left + r, top);

        barPath.lineTo(bumpLeft, top);
        barPath.cubicTo(
                bumpLeft + shoulder * 0.35f, top,
                bumpCenterX - crown * 1.2f, bumpTop,
                bumpCenterX - crown * 0.20f, bumpTop
        );
        barPath.cubicTo(
                bumpCenterX - crown * 0.05f, bumpTop,
                bumpCenterX + crown * 0.05f, bumpTop,
                bumpCenterX + crown * 0.20f, bumpTop
        );
        barPath.cubicTo(
                bumpCenterX + crown * 1.2f, bumpTop,
                bumpRight - shoulder * 0.35f, top,
                bumpRight, top
        );

        barPath.lineTo(right - r, top);
        barPath.quadTo(right, top, right, top + r);

        barPath.lineTo(right, bottom - r);
        barPath.quadTo(right, bottom, right - r, bottom);

        barPath.lineTo(left + r, bottom);
        barPath.quadTo(left, bottom, left, bottom - r);

        barPath.lineTo(left, top + r);
        barPath.quadTo(left, top, left + r, top);

        barPath.close();
        return barPath;
    }

    private void drawIcons(Canvas canvas, float bubbleX) {
        float influenceRadius = bubbleDiameter * 0.80f;

        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem item = items.get(i);
            float x = centerXs.get(i);
            float distance = Math.abs(x - bubbleX);
            float blend = clamp(1f - distance / influenceRadius, 0f, 1f);
            float eased = iconInterpolator.getInterpolation(blend);

            float inactiveAlpha = 1f - 0.78f * eased;
            float selectedAlpha = 0.92f * eased;

            Drawable base = item.getIcon();
            Drawable selected = item.getSelectedIcon();

            if (selected == null) {
                drawDrawable(canvas, base, x, bubbleCenterY, inactiveIconColor, inactiveAlpha, 1f);
            } else {
                drawDrawable(canvas, base, x, bubbleCenterY, inactiveIconColor, inactiveAlpha, 1f);
                drawDrawable(canvas, selected, x, bubbleCenterY, activeIconColor, selectedAlpha, 1f);
            }
        }
    }

    /**
     * Draws a single bubble that shrinks while traveling and merges into the target.
     */
    private void drawBubble(Canvas canvas, float bubbleX, float t) {
        float r = bubbleDiameter / 2f;
        float pulse = (float) Math.sin(Math.PI * t);

        boolean moving = dragging
                ? Math.abs(dragX - getCenterX(dragOriginIndex)) > dp(0.5f)
                : fromIndex != toIndex;

        if (!moving) {
            canvas.drawCircle(bubbleX, bubbleCenterY, r, bubblePaint);
            return;
        }

        float startX = dragging ? getCenterX(dragOriginIndex) : getCenterX(fromIndex);
        float endX = dragging ? getCenterX(dragTargetIndex) : getCenterX(toIndex);

        float leadT = 0.18f + 0.62f * t;
        float trailT = 0.12f + 0.72f * t;

        float leftX = lerp(startX, bubbleX, leadT);
        float rightX = lerp(bubbleX, endX, trailT);

        float leftRadius = r * (1.00f - 0.20f * pulse);
        float rightRadius = r * (0.74f + 0.18f * pulse);

        float bridgeHalfHeight = r * (0.34f + 0.06f * pulse);
        tempRect.set(
                Math.min(leftX, rightX),
                bubbleCenterY - bridgeHalfHeight,
                Math.max(leftX, rightX),
                bubbleCenterY + bridgeHalfHeight
        );
        canvas.drawRoundRect(tempRect, bridgeHalfHeight, bridgeHalfHeight, bubblePaint);

        canvas.drawCircle(leftX, bubbleCenterY + r * 0.06f, leftRadius, bubblePaint);
        canvas.drawCircle(rightX, bubbleCenterY - r * 0.03f, rightRadius, bubblePaint);

        float crestRadius = r * (0.23f + 0.07f * pulse);
        canvas.drawCircle(
                bubbleX,
                bubbleCenterY - r * (0.28f + 0.05f * pulse),
                crestRadius,
                bubblePaint
        );
    }

    private void drawActiveIcon(Canvas canvas, float bubbleX, float t) {
        int index = t < 0.52f ? fromIndex : toIndex;
        if (index < 0 || index >= items.size()) {
            return;
        }

        LiquidTabItem item = items.get(index);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (items.isEmpty()) {
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!draggable) {
                    return true;
                }
                dragOriginIndex = selectedIndex;
                dragTargetIndex = selectedIndex;
                dragX = getCenterX(selectedIndex);
                dragOffsetX = event.getX() - dragX;
                dragging = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!dragging) {
                    return super.onTouchEvent(event);
                }
                dragX = clamp(
                        event.getX() - dragOffsetX,
                        barRect.left + bubbleDiameter / 2f,
                        barRect.right - bubbleDiameter / 2f
                );
                dragTargetIndex = nearestIndex(dragX);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    dragging = false;
                    int target = nearestIndex(dragX);
                    if (target != selectedIndex) {
                        setSelectedIndex(target, true);
                    } else {
                        setSelectedIndex(selectedIndex, true);
                    }
                    performClick();
                    return true;
                }

                int tapped = hitTest(event.getX(), event.getY());
                if (tapped != -1 && tapped != selectedIndex) {
                    setSelectedIndex(tapped, true);
                    performClick();
                }
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

        float top = barRect.top - bubbleDiameter * 0.24f;
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

    private int nearestIndex(float x) {
        if (centerXs.isEmpty()) {
            return 0;
        }

        int best = 0;
        float bestDistance = Float.MAX_VALUE;
        for (int i = 0; i < centerXs.size(); i++) {
            float d = Math.abs(centerXs.get(i) - x);
            if (d < bestDistance) {
                bestDistance = d;
                best = i;
            }
        }
        return best;
    }

    private float getCurrentBubbleX() {
        if (dragging) {
            return dragX;
        }
        return lerp(getCenterX(fromIndex), getCenterX(toIndex), getMotionT());
    }

    private float getMotionT() {
        if (dragging) {
            float start = getCenterX(dragOriginIndex);
            float end = getCenterX(dragTargetIndex);
            float distance = Math.max(1f, Math.abs(end - start));
            return clamp(Math.abs(dragX - start) / distance, 0f, 1f);
        }
        return moveInterpolator.getInterpolation(progress);
    }

    private float getCenterX(int index) {
        if (centerXs.isEmpty()) {
            return barRect.centerX();
        }
        index = Math.max(0, Math.min(index, centerXs.size() - 1));
        return centerXs.get(index);
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
