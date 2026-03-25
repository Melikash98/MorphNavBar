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
    private static final int DEFAULT_ANIMATION_DURATION = 280;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
    private int selectedColor;
    private int inactiveIconColor;
    private int activeIconColor;
    private int indicatorColor;

    private float barRadius;
    private float barHeight;
    private float barSideMargin;
    private float barBottomMargin;
    private float bubbleDiameter;
    private float itemIconSize;
    private float shadowBlur;
    private float shadowDy;
    private float indicatorWidth;
    private float indicatorHeight;
    private int animationDuration;

    private float bubbleCenterY;
    private float inactiveIconY;
    private float activeIconY;

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
        indicatorColor = Color.parseColor("#E3E3E3");

        // سایزها دقیقاً مطابق dimens.xml + بزرگ‌تر برای جلوگیری از کوچک بودن
        barRadius = dp(22f);
        barHeight = dp(80f);
        barSideMargin = dp(36f);
        barBottomMargin = dp(34f);
        bubbleDiameter = dp(72f);
        itemIconSize = dp(34f);
        shadowBlur = dp(16f);
        shadowDy = dp(6f);
        indicatorWidth = dp(120f);
        indicatorHeight = dp(2f);
        animationDuration = DEFAULT_ANIMATION_DURATION;
    }

    private void readAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) return;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LiquidBottomNavigationView, defStyleAttr, 0);
        try {
            barColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_barColor, barColor);
            shadowColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_shadowColor, shadowColor);
            selectedColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_selectedColor, selectedColor);
            inactiveIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_inactiveIconColor, inactiveIconColor);
            activeIconColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_activeIconColor, activeIconColor);
            indicatorColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_indicatorColor, indicatorColor);

            barRadius = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barRadius, barRadius);
            barHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barHeight, barHeight);
            barSideMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barSideMargin, barSideMargin);
            barBottomMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barBottomMargin, barBottomMargin);
            bubbleDiameter = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_bubbleDiameter, bubbleDiameter);
            itemIconSize = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_itemIconSize, itemIconSize);
            shadowBlur = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowBlur, shadowBlur);
            shadowDy = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowDy, shadowDy);
            indicatorWidth = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_indicatorWidth, indicatorWidth);
            indicatorHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_indicatorHeight, indicatorHeight);
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

        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(indicatorColor);

        // paintهای قدیمی آیکون حذف شدند (دیگه استفاده نمی‌شوند)
    }

    // متدهای عمومی (بدون تغییر)
    public void setTabs(@NonNull List<LiquidTabItem> tabs) { /* همان قبلی */ }
    public void setTabs(@NonNull LiquidTabItem... tabs) { /* همان قبلی */ }
    public int getSelectedIndex() { return selectedIndex; }
    public void setSelectedIndex(int index) { setSelectedIndex(index, true); }
    public void setSelectedIndex(int index, boolean animate) { /* همان قبلی */ }
    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) { this.listener = listener; }
    public void setBarColor(@ColorInt int color) { barColor = color; barPaint.setColor(color); invalidate(); }
    public void setSelectedColor(@ColorInt int color) { selectedColor = color; bubblePaint.setColor(color); invalidate(); }
    public void setInactiveIconColor(@ColorInt int color) { inactiveIconColor = color; invalidate(); }
    public void setActiveIconColor(@ColorInt int color) { activeIconColor = color; invalidate(); }
    public void setAnimationDuration(int duration) { this.animationDuration = Math.max(1, duration); }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // فضای کافی برای bulge و حباب (جلوگیری از برش)
        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + dp(360));
        int desiredHeight = (int) Math.ceil(getPaddingTop() + getPaddingBottom() +
                barHeight + bubbleDiameter * 0.65f + barBottomMargin);
        setMeasuredDimension(resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float left = getPaddingLeft() + barSideMargin;
        float right = w - getPaddingRight() - barSideMargin;
        float bottom = h - getPaddingBottom() - barBottomMargin;
        float top = bottom - barHeight;
        barRect.set(left, top, right, bottom);

        bubbleCenterY = top + bubbleDiameter * 0.38f;
        activeIconY = bubbleCenterY;
        inactiveIconY = top + barHeight * 0.5f;

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
        drawIndicator(canvas, bubbleX, eased);   // خط نازک زیر تب فعال
        drawInactiveIcons(canvas, bubbleX);
        drawBubble(canvas, bubbleX, eased);
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
        float bulgeDepth = dp(14f) + dp(7f) * pulse;          // ← قوی‌تر برای حس مایع
        float bumpWidth = bubbleDiameter * 1.55f;

        float bumpLeft = Math.max(left + radius * 0.6f, bubbleX - bumpWidth / 2f);
        float bumpRight = Math.min(right - radius * 0.6f, bubbleX + bumpWidth / 2f);
        float bulgeTop = top - bulgeDepth;

        path.moveTo(left + radius, top);
        path.lineTo(bumpLeft, top);

        path.cubicTo(bumpLeft + bumpWidth * 0.3f, top,
                bubbleX - bumpWidth * 0.25f, bulgeTop,
                bubbleX, bulgeTop);
        path.cubicTo(bubbleX + bumpWidth * 0.25f, bulgeTop,
                bumpRight - bumpWidth * 0.3f, top,
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

    private void drawIndicator(Canvas canvas, float bubbleX, float eased) {
        float y = barRect.bottom - indicatorHeight / 2f - dp(4f);
        float halfW = indicatorWidth / 2f;
        float left = bubbleX - halfW;
        float right = bubbleX + halfW;

        left = Math.max(barRect.left + barRadius, left);
        right = Math.min(barRect.right - barRadius, right);

        RectF rect = new RectF(left, y - indicatorHeight / 2f, right, y + indicatorHeight / 2f);
        canvas.drawRoundRect(rect, indicatorHeight, indicatorHeight, indicatorPaint);
    }

    private void drawInactiveIcons(Canvas canvas, float bubbleX) {
        float influenceRadius = bubbleDiameter * 0.9f;

        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem item = items.get(i);
            float centerX = centerXs.get(i);
            float distance = Math.abs(centerX - bubbleX);
            float t = clamp(1f - (distance / influenceRadius), 0f, 1f);
            float eased = positionInterpolator.getInterpolation(t);

            float inactiveAlpha = 1f - eased;   // ← حالا کامل محو می‌شود (رفع ghost)

            drawDrawable(canvas, item.getIcon(), centerX, inactiveIconY, inactiveIconColor, inactiveAlpha);
        }
    }

    private void drawBubble(Canvas canvas, float bubbleX, float eased) {
        float r = bubbleDiameter / 2f;
        float pulse = (float) Math.sin(Math.PI * eased);

        // انیمیشن مایع قوی‌تر و دقیق‌تر مثل فیلم اول
        float stretchFactor = 1f + 0.4f * (float) Math.sin(Math.PI * eased);
        float mainRadiusX = r * stretchFactor * (0.98f - 0.04f * pulse);
        float mainRadiusY = r * (0.95f - 0.05f * pulse);

        float mainY = bubbleCenterY + dp(3f);
        float crestRadius = r * (0.55f + 0.12f * pulse);
        float crestY = bubbleCenterY - r * 0.4f - dp(2f) * pulse;

        Path main = new Path();
        main.addOval(bubbleX - mainRadiusX, mainY - mainRadiusY,
                bubbleX + mainRadiusX, mainY + mainRadiusY, Path.Direction.CW);

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

        // highlight الاستیک
        if (eased > 0.05f && eased < 0.95f) {
            float highlightRadius = r * 0.22f * pulse;
            canvas.drawCircle(bubbleX, crestY - highlightRadius * 0.25f, highlightRadius, bubblePaint);
        }
    }

    private void drawActiveIcon(Canvas canvas, float bubbleX, float eased) {
        int iconIndex = eased < 0.5f ? fromIndex : toIndex;
        if (iconIndex < 0 || iconIndex >= items.size()) return;

        LiquidTabItem item = items.get(iconIndex);
        float scale = 1f + 0.085f * (float) Math.sin(Math.PI * eased);

        Drawable iconToDraw = item.getSelectedIcon() != null ? item.getSelectedIcon() : item.getIcon();
        drawDrawable(canvas, iconToDraw, bubbleX, activeIconY, activeIconColor, 1f, scale);
    }

    private void drawDrawable(Canvas canvas, @NonNull Drawable drawable,
                              float centerX, float centerY, @ColorInt int tint,
                              float alpha) {
        drawDrawable(canvas, drawable, centerX, centerY, tint, alpha, 1f);
    }

    private void drawDrawable(Canvas canvas, @NonNull Drawable drawable,
                              float centerX, float centerY, @ColorInt int tint,
                              float alpha, float scale) {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (items.isEmpty()) return super.onTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
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

    private int hitTest(float x, float y) {
        if (centerXs.isEmpty()) return -1;
        float top = barRect.top - bubbleDiameter * 0.35f;
        float bottom = barRect.bottom;
        if (y < top || y > bottom) return -1;

        float segmentWidth = barRect.width() / items.size();
        int index = (int) ((x - barRect.left) / segmentWidth);
        return (index < 0 || index >= items.size()) ? -1 : index;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
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
