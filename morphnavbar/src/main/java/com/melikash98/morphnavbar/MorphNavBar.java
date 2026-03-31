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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
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
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.ImageViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MorphNavBar extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 320;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inactiveIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint.FontMetrics labelFontMetrics = new Paint.FontMetrics();


    private final RectF barRect = new RectF();
    private final Path barPath = new Path();
    private final Path bubblePath = new Path();

    private final List<LiquidTabItem.Model> items = new ArrayList<>();
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
    private float inactiveIconY;
    private float activeIconY;

    private boolean showLabels;
    private float labelTextSizePx;
    private String labelFontFamily;

    private boolean hasAnyLabel = false;
    private float labelBaselineY = 0f;

    private static final float DEFAULT_LABEL_SIZE_SP = 14f;
    private static final float DEFAULT_LABEL_TOP_GAP_DP = 0f;
    private static final float DEFAULT_LABEL_BOTTOM_GAP_DP = 10f;


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

        barRadius = dp(26f);
        barHeight = dp(100f);
        barSideMargin = dp(0f);
        barBottomMargin = dp(0f);
        bubbleDiameter = dp(92f);
        itemIconSize = dp(34f);
        shadowBlur = dp(12f);
        shadowDy = dp(4f);
        animationDuration = DEFAULT_ANIMATION_DURATION;

        showLabels = true;
        labelTextSizePx = sp(DEFAULT_LABEL_SIZE_SP);
        labelFontFamily = "sans-serif";

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

            barRadius = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barRadius, barRadius);
            barHeight = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barHeight, barHeight);
            barSideMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barSideMargin, barSideMargin);
            barBottomMargin = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_barBottomMargin, barBottomMargin);
            bubbleDiameter = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_bubbleDiameter, bubbleDiameter);
            itemIconSize = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_itemIconSize, itemIconSize);
            shadowBlur = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowBlur, shadowBlur);
            shadowDy = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_shadowDy, shadowDy);
            animationDuration = a.getInteger(R.styleable.LiquidBottomNavigationView_lbv_animationDuration, animationDuration);

            showLabels = a.getBoolean(R.styleable.LiquidBottomNavigationView_lbv_showLabels, showLabels);
            labelTextSizePx = a.getDimension(R.styleable.LiquidBottomNavigationView_lbv_labelTextSize, labelTextSizePx);

            String family = a.getString(R.styleable.LiquidBottomNavigationView_lbv_labelFontFamily);
            if (family != null && !family.trim().isEmpty()) {
                labelFontFamily = family.trim();
            }

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

        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(labelTextSizePx);
        labelPaint.setColor(inactiveIconColor);
        applyLabelTypeface();
    }


    private void applyLabelTypeface() {
        Typeface tf = Typeface.create(labelFontFamily, Typeface.BOLD);

        if (tf == null) {
            tf = Typeface.DEFAULT_BOLD;
        }

        labelPaint.setTypeface(tf);
    }

    public void setTabs(@NonNull List<LiquidTabItem.Model> tabs) {
        items.clear();
        items.addAll(tabs);
        updateHasAnyLabel();
        resetSelection();
        rebuildCenters();
        updateContentDescription();
        requestLayout();
        invalidate();
    }

    public void setTabs(@NonNull LiquidTabItem.Model... tabs) {
        items.clear();
        Collections.addAll(items, tabs);
        updateHasAnyLabel();
        resetSelection();
        rebuildCenters();
        updateContentDescription();
        requestLayout();
        invalidate();
    }

    public void add(@NonNull LiquidTabItem.Model tab) {
        items.add(tab);
        updateHasAnyLabel();

        if (items.size() == 1) {
            selectedIndex = 0;
            fromIndex = 0;
            toIndex = 0;
            progress = 1f;
            updateContentDescription();
        }

        rebuildCenters();
        requestLayout();
        invalidate();
    }

    public void clearTabs() {
        items.clear();
        centerXs.clear();
        hasAnyLabel = false;
        selectedIndex = 0;
        fromIndex = 0;
        toIndex = 0;
        progress = 1f;
        setContentDescription(null);
        requestLayout();
        invalidate();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public int getItemCount() {
        return items.size();
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
            updateContentDescription();
            if (listener != null) listener.onTabSelected(selectedIndex, items.get(selectedIndex));
            invalidate();
            return;
        }

        fromIndex = selectedIndex;
        toIndex = index;
        progress = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.setInterpolator(positionInterpolator);

        final boolean[] cancelled = new boolean[1];

        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled[0] = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (cancelled[0]) return;

                selectedIndex = toIndex;
                fromIndex = selectedIndex;
                progress = 1f;
                updateContentDescription();

                if (listener != null && !items.isEmpty()) {
                    listener.onTabSelected(selectedIndex, items.get(selectedIndex));
                }
                invalidate();
                animator = null;
            }
        });

        animator.start();
    }

    public void setShowLabels(boolean show) {
        if (this.showLabels == show) return;
        this.showLabels = show;
        requestLayout();
        invalidate();
    }

    public void setLabelTextSizePx(float sizePx) {
        if (this.labelTextSizePx == sizePx) return;
        this.labelTextSizePx = sizePx;
        labelPaint.setTextSize(sizePx);
        requestLayout();
        invalidate();
    }

    public void setLabelTextSizeSp(float sizeSp) {
        setLabelTextSizePx(sp(sizeSp));
    }

    public void setLabelTypeface(@Nullable Typeface typeface) {
        labelPaint.setTypeface(typeface != null ? typeface : Typeface.MONOSPACE);
        requestLayout();
        invalidate();
    }

    public void setLabelFontFamily(@Nullable String fontFamily) {
        if (fontFamily == null || fontFamily.trim().isEmpty()) {
            this.labelFontFamily = "Roboto Mono";
        } else {
            this.labelFontFamily = fontFamily.trim();
        }
        applyLabelTypeface();
        requestLayout();
        invalidate();
    }


    public void setBarColor(@ColorInt int color) {
        barColor = color;
        barPaint.setColor(color);
        invalidate();
    }

    public void setSelectedColor(@ColorInt int color) {
        selectedColor = color;
        bubblePaint.setColor(color);
        invalidate();
    }

    public void setInactiveIconColor(@ColorInt int color) {
        inactiveIconColor = color;
        inactiveIconPaint.setColor(color);
        invalidate();
    }

    public void setActiveIconColor(@ColorInt int color) {
        activeIconColor = color;
        activeIconPaint.setColor(color);
        invalidate();
    }

    public void setAnimationDuration(int duration) {
        this.animationDuration = Math.max(1, duration);
    }

    private void updateHasAnyLabel() {
        hasAnyLabel = false;
        for (LiquidTabItem.Model item : items) {
            if (item.getLabel() != null && item.getLabel().length() > 0) {
                hasAnyLabel = true;
                break;
            }
        }
    }

    private void updateContentDescription() {
        if (items.isEmpty() || selectedIndex < 0 || selectedIndex >= items.size()) {
            setContentDescription(null);
            return;
        }

        LiquidTabItem.Model item = items.get(selectedIndex);
        CharSequence cd = item.getContentDescription();
        if (cd != null && cd.length() > 0) {
            setContentDescription(cd);
        } else if (item.getLabel() != null && item.getLabel().length() > 0) {
            setContentDescription(item.getLabel());
        } else {
            setContentDescription(null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + dp(360));
        int desiredHeight = (int) Math.ceil(
                getPaddingTop() + getPaddingBottom()
                        + barHeight
                        + bubbleDiameter * 0.30f
                        + barBottomMargin
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

        bubbleCenterY = top + bubbleDiameter * 0.5f;
        activeIconY = bubbleCenterY;
        inactiveIconY = bubbleCenterY;

        rebuildCenters();

        if (showLabels && hasAnyLabel) {
            labelPaint.getFontMetrics(labelFontMetrics);
            labelBaselineY = barRect.bottom - dp(DEFAULT_LABEL_BOTTOM_GAP_DP) - labelFontMetrics.bottom;
        }
    }

    private float getLabelAreaHeightPx() {
        if (!showLabels || !hasAnyLabel) return 0f;
        labelPaint.getFontMetrics(labelFontMetrics);
        return (labelFontMetrics.bottom - labelFontMetrics.top) + dp(DEFAULT_LABEL_TOP_GAP_DP);
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
        drawBubble(canvas, bubbleX, eased);
        drawActiveIcon(canvas, bubbleX, eased);

        if (showLabels && hasAnyLabel) {
            drawLabels(canvas, bubbleX);
        }
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
        float bulgeDepth = dp(9f) + dp(4.5f) * pulse;
        float bumpWidth = bubbleDiameter * 1.38f;
        float bumpLeft = Math.max(left + radius * 0.6f, bubbleX - bumpWidth / 2f);
        float bumpRight = Math.min(right - radius * 0.6f, bubbleX + bumpWidth / 2f);
        float bulgeTop = top - bulgeDepth;

        path.moveTo(left + radius, top);
        path.lineTo(bumpLeft, top);
        path.cubicTo(bumpLeft + bumpWidth * 0.25f, top, bubbleX - bumpWidth * 0.19f, bulgeTop, bubbleX, bulgeTop);
        path.cubicTo(bubbleX + bumpWidth * 0.19f, bulgeTop, bumpRight - bumpWidth * 0.25f, top, bumpRight, top);
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
        float influenceRadius = bubbleDiameter * 0.9f;
        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem.Model item = items.get(i);
            float centerX = centerXs.get(i);
            float distance = Math.abs(centerX - bubbleX);
            float t = clamp(1f - (distance / influenceRadius), 0f, 1f);
            float eased = positionInterpolator.getInterpolation(t);
            float inactiveAlpha = 1f - 0.88f * eased;
            Drawable icon = loadDrawable(item.getIconResId());
            if (icon != null) {
                drawDrawable(canvas, icon, centerX, inactiveIconY, inactiveIconColor, inactiveAlpha);
            }
        }
    }

    private void drawBubble(Canvas canvas, float bubbleX, float eased) {
        float r = bubbleDiameter / 2f;
        float pulse = (float) Math.sin(Math.PI * eased);
        float stretchFactor = 1f + 0.35f * (float) Math.sin(Math.PI * eased);
        float mainRadiusX = r * stretchFactor * (0.97f - 0.03f * pulse);
        float mainRadiusY = r * (0.97f - 0.03f * pulse);
        float mainY = bubbleCenterY + dp(1.8f);
        float crestRadius = r * (0.48f + 0.14f * pulse);
        float crestY = bubbleCenterY - r * (0.34f + 0.07f * pulse);

        Path main = new Path();
        main.addOval(bubbleX - mainRadiusX, mainY - mainRadiusY, bubbleX + mainRadiusX, mainY + mainRadiusY, Path.Direction.CW);

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

        if (eased > 0.05f && eased < 0.95f) {
            float highlightRadius = r * 0.21f * pulse;
            canvas.drawCircle(bubbleX, crestY - highlightRadius * 0.22f, highlightRadius, bubblePaint);
        }
    }

    private void drawActiveIcon(Canvas canvas, float bubbleX, float eased) {
        int iconIndex = eased < 0.5f ? fromIndex : toIndex;
        if (iconIndex < 0 || iconIndex >= items.size()) return;
        LiquidTabItem.Model item = items.get(iconIndex);
        float scale = 1f + 0.085f * (float) Math.sin(Math.PI * eased);
        Drawable icon = item.getSelectedIconResId() != 0
                ? loadDrawable(item.getSelectedIconResId())
                : loadDrawable(item.getIconResId());
        if (icon != null) {
            drawDrawable(canvas, icon, bubbleX, activeIconY, activeIconColor, 1f, scale);
        }

    }

    private void drawDrawable(Canvas canvas, @NonNull Drawable drawable, float centerX, float centerY,
                              @ColorInt int tint, float alpha) {
        drawDrawable(canvas, drawable, centerX, centerY, tint, alpha, 1f);
    }

    private void drawLabels(Canvas canvas, float bubbleX) {
        if (!showLabels || !hasAnyLabel || items.isEmpty()) return;

        float influenceRadius = bubbleDiameter * 0.9f;
        float segmentWidth = barRect.width() / Math.max(1, items.size());
        float maxTextWidth = segmentWidth - dp(10f);

        for (int i = 0; i < items.size(); i++) {
            LiquidTabItem.Model item = items.get(i);
            CharSequence label = item.getLabel();
            if (label == null || label.length() == 0) continue;

            float centerX = centerXs.get(i);
            float distance = Math.abs(centerX - bubbleX);
            float t = clamp(1f - (distance / influenceRadius), 0f, 1f);
            float eased = positionInterpolator.getInterpolation(t);

            int color = ColorUtils.blendARGB(inactiveIconColor, selectedColor, eased);
            labelPaint.setColor(color);

            CharSequence ellipsized = TextUtils.ellipsize(
                    label,
                    labelPaint,
                    maxTextWidth,
                    TextUtils.TruncateAt.END
            );

            canvas.drawText(ellipsized.toString(), centerX, labelBaselineY, labelPaint);
        }
    }

    @Nullable
    private Drawable loadDrawable(@DrawableRes int resId) {
        if (resId == 0) return null;
        return AppCompatResources.getDrawable(getContext(), resId);
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

    @Override
    public boolean performClick() {
        return super.performClick();
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

    private float sp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private void resetSelection() {
        selectedIndex = 0;
        fromIndex = 0;
        toIndex = 0;
        progress = 1f;
    }
}
