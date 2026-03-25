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


public class MorphNavBar extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 320; // کمی نرم‌تر و نزدیک به اصلی

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inactiveIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // فقط برای سازگاری
    private final Paint activeIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();
    private final Path barPath = new Path();
    private final Path bubblePath = new Path();

    private final List<LiquidTabItem> items = new ArrayList<>();
    private final List<Float> centerXs = new ArrayList<>();

    private ValueAnimator animator;
    private OnTabSelectedListener listener;

    private int selectedIndex = 0;
    private int fromIndex = 0;
    private int toIndex = 0;
    private float progress = 1f;

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

    private float iconY;           // ← همه آیکون‌ها در یک ردیف (جدید)
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
        selectedColor = Color.parseColor("#00CFC0");
        inactiveIconColor = Color.parseColor("#00CFC0");
        activeIconColor = Color.WHITE;

        barRadius = dp(36f);
        barHeight = dp(72f);
        barSideMargin = dp(24f);      // کمی فاصله از کناره‌ها (زیباتر)
        barBottomMargin = dp(28f);
        bubbleDiameter = dp(98f);     // ← بزرگ‌تر شد (درخواست شما)
        itemIconSize = dp(36f);       // ← آیکون‌ها بزرگ‌تر
        shadowBlur = dp(14f);
        shadowDy = dp(6f);
        animationDuration = DEFAULT_ANIMATION_DURATION;
    }

    private void readAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) return;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LiquidBottomNavigationView, defStyleAttr, 0);
        try {
            barColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_barColor, barColor);
            // ... (بقیه attributes بدون تغییر)
            bubbleDiameter = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_bubbleDiameter, bubbleDiameter);
            itemIconSize = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_itemIconSize, itemIconSize);
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

        // آیکون‌های غیرفعال (stroke)
        inactiveIconPaint.setStyle(Paint.Style.STROKE);
        inactiveIconPaint.setStrokeWidth(dp(2.1f));
        inactiveIconPaint.setColor(inactiveIconColor);
        inactiveIconPaint.setStrokeCap(Paint.Cap.ROUND);
        inactiveIconPaint.setStrokeJoin(Paint.Join.ROUND);

        activeIconPaint.setStyle(Paint.Style.STROKE);
        activeIconPaint.setStrokeWidth(dp(2.1f));
        activeIconPaint.setColor(activeIconColor);
        activeIconPaint.setStrokeCap(Paint.Cap.ROUND);
        activeIconPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setTabs(@NonNull List<LiquidTabItem> tabs) {
        items.clear();
        items.addAll(tabs);
        if (selectedIndex >= items.size()) selectedIndex = 0;
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
        if (selectedIndex >= items.size()) selectedIndex = 0;
        fromIndex = selectedIndex;
        toIndex = selectedIndex;
        progress = 1f;
        rebuildCenters();
        requestLayout();
        invalidate();
    }

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }

    public void setSelectedIndex(int index, boolean animate) {
        if (items.isEmpty() || index < 0 || index >= items.size() || index == selectedIndex) return;

        if (animator != null) animator.cancel();

        if (!animate || centerXs.isEmpty()) {
            selectedIndex = index;
            fromIndex = index;
            toIndex = index;
            progress = 1f;
            if (listener != null) listener.onTabSelected(index, items.get(index));
            invalidate();
            return;
        }

        fromIndex = selectedIndex;
        toIndex = index;
        progress = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.setInterpolator(new FastOutSlowInInterpolator()); // نرم و premium
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
                if (listener != null) listener.onTabSelected(selectedIndex, items.get(selectedIndex));
                invalidate();
            }
        });
        animator.start();
    }

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + dp(360));
        int desiredHeight = (int) Math.ceil(getPaddingTop() + getPaddingBottom() + barHeight + barBottomMargin + bubbleDiameter * 0.15f);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float left = getPaddingLeft() + barSideMargin;
        float right = w - getPaddingRight() - barSideMargin;
        float bottom = h - getPaddingBottom() - barBottomMargin;
        float top = bottom - barHeight;

        barRect.set(left, top, right, bottom);

        // همه آیکون‌ها دقیقاً در یک ردیف
        iconY = barRect.top + barHeight * 0.5f;
        bubbleCenterY = iconY;
    }

    private void rebuildCenters() {
        centerXs.clear();
        if (items.isEmpty()) return;
        float seg = barRect.width() / items.size();
        for (int i = 0; i < items.size(); i++) {
            centerXs.add(barRect.left + seg * (i + 0.5f));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (items.isEmpty()) return;

        float startX = getCenterX(fromIndex);
        float endX = getCenterX(toIndex);
        float eased = new FastOutSlowInInterpolator().getInterpolation(progress);
        float bubbleX = lerp(startX, endX, eased);

        drawShadow(canvas);
        drawBar(canvas);
        drawInactiveIcons(canvas, bubbleX);
        drawBubble(canvas, bubbleX, eased);      // blob افقی جدید
        drawActiveIcon(canvas, bubbleX, eased);
    }

    private void drawShadow(Canvas canvas) {
        shadowPaint.setShadowLayer(shadowBlur, 0f, shadowDy, shadowColor);
        canvas.drawPath(buildBarPath(), shadowPaint);
        shadowPaint.clearShadowLayer();
    }

    private void drawBar(Canvas canvas) {
        canvas.drawPath(buildBarPath(), barPaint);
    }

    private Path buildBarPath() {
        Path path = new Path();
        path.addRoundRect(barRect, barRadius, barRadius, Path.Direction.CW);
        return path;
    }

    private void drawInactiveIcons(Canvas canvas, float bubbleX) {
        float influence = bubbleDiameter * 1.05f; // محدوده fade

        for (int i = 0; i < items.size(); i++) {
            float cx = centerXs.get(i);
            float distance = Math.abs(cx - bubbleX);
            float alpha = Math.max(0f, 1f - (distance / influence));

            drawDrawable(canvas, items.get(i).getIcon(), cx, iconY, inactiveIconColor, alpha, 1f);
        }
    }

    private void drawBubble(Canvas canvas, float bubbleX, float eased) {
        float r = bubbleDiameter / 2f;

        // === انیمیشن Blob افقی (مثل قطره اشک مایع) ===
        float stretchX = 1f + 0.72f * (float) Math.sin(Math.PI * eased);   // کشیدگی افقی قوی در وسط مسیر
        float stretchY = 0.94f - 0.08f * (float) Math.sin(Math.PI * eased); // کمی فشرده‌تر در محور عمودی

        float w = r * 2f * stretchX;
        float h = r * 2f * stretchY;

        RectF oval = new RectF(
                bubbleX - w / 2f,
                bubbleCenterY - h / 2f,
                bubbleX + w / 2f,
                bubbleCenterY + h / 2f
        );

        bubblePath.reset();
        bubblePath.addOval(oval, Path.Direction.CW);

        canvas.drawPath(bubblePath, bubblePaint);
    }

    private void drawActiveIcon(Canvas canvas, float bubbleX, float eased) {
        int iconIndex = eased < 0.5f ? fromIndex : toIndex;
        if (iconIndex < 0 || iconIndex >= items.size()) return;

        LiquidTabItem item = items.get(iconIndex);
        Drawable iconToDraw = item.getSelectedIcon() != null ? item.getSelectedIcon() : item.getIcon();

        // آیکون فعال بزرگ‌تر + pulse نرم
        float scale = 1.25f + 0.12f * (float) Math.sin(Math.PI * eased);

        drawDrawable(canvas, iconToDraw, bubbleX, iconY, activeIconColor, 1f, scale);
    }

    private void drawDrawable(Canvas canvas, @NonNull Drawable drawable,
                              float centerX, float centerY, @ColorInt int tint,
                              float alpha, float scale) {

        Drawable d = drawable.mutate();
        d.setTint(tint);

        int size = Math.round(itemIconSize * scale);
        int half = size / 2;

        d.setBounds(Math.round(centerX) - half,
                Math.round(centerY) - half,
                Math.round(centerX) + half,
                Math.round(centerY) + half);

        int oldAlpha = d.getAlpha();
        d.setAlpha((int) (255f * Math.max(0f, Math.min(1f, alpha))));
        d.draw(canvas);
        d.setAlpha(oldAlpha);
    }

    private float getCenterX(int index) {
        if (centerXs.isEmpty()) return barRect.centerX();
        index = Math.max(0, Math.min(index, centerXs.size() - 1));
        return centerXs.get(index);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // ... (بدون تغییر - همان کد قبلی)
        // فقط برای کامل بودن نگه داشتم
        if (items.isEmpty()) return super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                int index = hitTest(event.getX(), event.getY());
                if (index != -1) {
                    setSelectedIndex(index, true);
                    performClick();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private int hitTest(float x, float y) {
        if (centerXs.isEmpty()) return -1;
        float segmentWidth = barRect.width() / items.size();
        int index = (int) ((x - barRect.left) / segmentWidth);
        return (index < 0 || index >= items.size()) ? -1 : index;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }
}
