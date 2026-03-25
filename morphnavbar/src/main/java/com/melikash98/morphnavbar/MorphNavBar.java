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
    private static final int DEFAULT_ANIMATION_DURATION = 300;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inactiveIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();
    private final Path barPath = new Path();
    private final Path bubblePath = new Path();

    private final List<LiquidTabItem> items = new ArrayList<>();
    private final List<Float> centerXs = new ArrayList<>();

    private final FastOutSlowInInterpolator positionInterpolator = new FastOutSlowInInterpolator();

    private ValueAnimator animator;
    private OnTabSelectedListener listener;

    private int selectedIndex = 0;
    private int fromIndex = 0;
    private int toIndex = 0;
    private float progress = 1f;

    private int barColor;
    private int shadowColor;
    private int indicatorColor;      // فعلاً استفاده نمی‌شود (مطابق ویدیو اول)
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
        barColor = getResources().getColor(R.color.lbv_bar_color);
        shadowColor = getResources().getColor(R.color.lbv_shadow_color);
        indicatorColor = getResources().getColor(R.color.lbv_indicator_color);
        selectedColor = getResources().getColor(R.color.lbv_selected_color);
        unselectedColor = getResources().getColor(R.color.lbv_unselected_color);
        activeIconColor = getResources().getColor(R.color.lbv_active_icon_color);
        inactiveIconColor = getResources().getColor(R.color.lbv_inactive_icon_color);

        barRadius = getResources().getDimension(R.dimen.lbv_bar_radius);
        barHeight = getResources().getDimension(R.dimen.lbv_bar_height);
        barSideMargin = getResources().getDimension(R.dimen.lbv_bar_side_margin);
        barBottomMargin = getResources().getDimension(R.dimen.lbv_bar_bottom_margin);
        bubbleDiameter = getResources().getDimension(R.dimen.lbv_bubble_diameter);
        itemIconSize = getResources().getDimension(R.dimen.lbv_item_icon_size);
        indicatorWidth = getResources().getDimension(R.dimen.lbv_indicator_width);
        indicatorHeight = getResources().getDimension(R.dimen.lbv_indicator_height);
        shadowBlur = getResources().getDimension(R.dimen.lbv_shadow_blur);
        shadowDy = getResources().getDimension(R.dimen.lbv_shadow_dy);
        animationDuration = DEFAULT_ANIMATION_DURATION;
    }

    private void readAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) return;
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

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }

    public void setSelectedIndex(int index, boolean animate) {
        if (items.isEmpty() || index < 0 || index >= items.size() || index == selectedIndex) return;

        if (animator != null) {
            animator.cancel();
            animator = null;
        }

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
        animator.setInterpolator(positionInterpolator);
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

    // متدهای setter برای تم (اختیاری)
    public void setBarColor(@ColorInt int color) { barColor = color; barPaint.setColor(color); invalidate(); }
    public void setSelectedColor(@ColorInt int color) { selectedColor = color; bubblePaint.setColor(color); invalidate(); }
    public void setInactiveIconColor(@ColorInt int color) { inactiveIconColor = color; inactiveIconPaint.setColor(color); invalidate(); }
    public void setActiveIconColor(@ColorInt int color) { activeIconColor = color; activeIconPaint.setColor(color); invalidate(); }
    public void setAnimationDuration(int duration) { this.animationDuration = Math.max(1, duration); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + dp(360));
        int desiredHeight = (int) Math.ceil(getPaddingTop() + getPaddingBottom() + barHeight + bubbleDiameter * 0.22f + barBottomMargin);
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
        bubbleCenterY = top + bubbleDiameter * 0.42f;
        rebuildCenters();
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
        float eased = positionInterpolator.getInterpolation(progress);
        float bubbleX = lerp(startX, endX, eased);

        drawShadow(canvas, bubbleX, eased);
        drawBar(canvas, bubbleX, eased);
        drawInactiveIcons(canvas, bubbleX);
        drawBubble(canvas, bubbleX, eased);   // blob مایع دقیقاً مثل ویدیو اول
        drawActiveIcon(canvas, bubbleX, eased);
    }

    private void drawShadow(Canvas canvas, float bubbleX, float eased) {
        shadowPaint.setShadowLayer(shadowBlur, 0f, shadowDy, shadowColor);
        canvas.drawPath(buildBarPath(bubbleX, eased), shadowPaint);
        shadowPaint.clearShadowLayer();
    }

    private void drawBar(Canvas canvas, float bubbleX, float eased) {
        canvas.drawPath(buildBarPath(bubbleX, eased), barPaint);
    }

    private Path buildBarPath(float bubbleX, float eased) {
        Path path = new Path();
        float left = barRect.left, top = barRect.top, right = barRect.right, bottom = barRect.bottom;
        float radius = barRadius;

        float pulse = (float) Math.sin(Math.PI * eased);
        float bulgeDepth = dp(12f) + dp(6f) * pulse;           // تنظیم شده برای ویدیو اول (بلندتر)
        float bumpWidth = bubbleDiameter * 1.45f;              // تنظیم شده برای bulge نرم‌تر

        float bumpLeft = clamp(bubbleX - bumpWidth / 2f, left + radius * 1.1f, right - radius * 1.1f - bumpWidth);
        float bumpRight = bumpLeft + bumpWidth;
        float bulgeTop = top - bulgeDepth;

        path.moveTo(left + radius, top);
        path.lineTo(bumpLeft, top);

        // cubic دقیق‌تر برای bulge مایع (مطابق ویدیو اول)
        path.cubicTo(bumpLeft + bumpWidth * 0.22f, top,
                bubbleX - bumpWidth * 0.20f, bulgeTop,
                bubbleX, bulgeTop);
        path.cubicTo(bubbleX + bumpWidth * 0.20f, bulgeTop,
                bumpRight - bumpWidth * 0.22f, top,
                bumpRight, top);

        path.lineTo(right - radius, top);
        path.quadTo(right, top, right, top + radius);
        path.lineTo(right, bottom - radius);
        path.quadTo(right, bottom, right - radius, bottom);
        path.lineTo(left + radius, bottom);
        path.quadTo(left, bottom, left, bottom - radius);
        path.lineTo(left, top + radius);
        path.quadTo(left, top, left + radius, top);
        path.close();
        return path;
    }

    private void drawInactiveIcons(Canvas canvas, float bubbleX) {
        float influenceRadius = bubbleDiameter * 0.85f;
        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem item = items.get(i);
            float centerX = centerXs.get(i);
            float distance = Math.abs(centerX - bubbleX);
            float t = clamp(1f - (distance / influenceRadius), 0f, 1f);
            float eased = positionInterpolator.getInterpolation(t);

            float inactiveAlpha = 1f - 0.85f * eased;
            float activeAlpha = 0.95f * eased;

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

    private void drawBubble(Canvas canvas, float bubbleX, float eased) {
        float r = bubbleDiameter / 2f;
        float pulse = (float) Math.sin(Math.PI * eased);

        // blob مایع دقیق‌تر (مطابق فریم ۵ ویدیو اول)
        float mainRadius = r * (0.98f - 0.04f * pulse);
        float crestRadius = r * (0.42f + 0.12f * pulse);   // crest بزرگ‌تر برای حس مایع

        float mainY = bubbleCenterY + dp(2f);
        float crestY = bubbleCenterY - r * (0.32f + 0.06f * pulse);

        Path main = new Path();
        main.addCircle(bubbleX, mainY, mainRadius, Path.Direction.CW);

        Path crest = new Path();
        crest.addCircle(bubbleX, crestY, crestRadius, Path.Direction.CW);

        bubblePath.reset();
        bubblePath.set(main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bubblePath.op(crest, Path.Op.UNION);
        } else {
            bubblePath.addPath(crest);
        }

        canvas.drawPath(bubblePath, bubblePaint);

        // highlight الاستیک کوچک (حس زنده)
        if (eased > 0f && eased < 1f) {
            float highlightRadius = r * 0.22f * pulse;
            if (highlightRadius > 0f) {
                canvas.drawCircle(bubbleX, crestY - highlightRadius * 0.25f, highlightRadius, bubblePaint);
            }
        }
    }

    private void drawActiveIcon(Canvas canvas, float bubbleX, float eased) {
        int iconIndex = eased < 0.5f ? fromIndex : toIndex;
        if (iconIndex < 0 || iconIndex >= items.size()) return;

        LiquidTabItem item = items.get(iconIndex);
        float scale = 1f + 0.09f * (float) Math.sin(Math.PI * eased);

        Drawable selected = item.getSelectedIcon();
        if (selected != null) {
            drawDrawable(canvas, selected, bubbleX, bubbleCenterY, activeIconColor, 1f, scale);
        } else {
            drawDrawable(canvas, item.getIcon(), bubbleX, bubbleCenterY, activeIconColor, 1f, scale);
        }
    }

    private void drawDrawable(Canvas canvas, @NonNull Drawable drawable, float centerX, float centerY,
                              @ColorInt int tint, float alpha) {
        drawDrawable(canvas, drawable, centerX, centerY, tint, alpha, 1f);
    }

    private void drawDrawable(Canvas canvas, @NonNull Drawable drawable, float centerX, float centerY,
                              @ColorInt int tint, float alpha, float scale) {
        Drawable d = drawable.mutate();
        d.setTint(tint);

        int size = Math.round(itemIconSize * scale);
        int half = size / 2;

        d.setBounds(Math.round(centerX) - half, Math.round(centerY) - half,
                Math.round(centerX) + half, Math.round(centerY) + half);

        int oldAlpha = d.getAlpha();
        d.setAlpha((int) (255f * clamp(alpha, 0f, 1f)));
        d.draw(canvas);
        d.setAlpha(oldAlpha);
    }

    private float getCenterX(int index) {
        if (centerXs.isEmpty()) return barRect.centerX();
        index = Math.max(0, Math.min(index, centerXs.size() - 1));
        return centerXs.get(index);
    }

    // ====================== TOUCH (فقط تپ - Drag کاملاً حذف شد) ======================
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (items.isEmpty()) return super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return true;

            case MotionEvent.ACTION_MOVE:
                return true;   // ← مهم: Drag را کاملاً consume می‌کند (مشکل ویدیو دوم حل شد)

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
        if (centerXs.isEmpty()) return -1;
        float top = barRect.top - bubbleDiameter * 0.3f;
        float bottom = barRect.bottom;
        if (y < top || y > bottom) return -1;

        float segmentWidth = barRect.width() / items.size();
        int index = (int) ((x - barRect.left) / segmentWidth);
        return (index < 0 || index >= items.size()) ? -1 : index;
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
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
