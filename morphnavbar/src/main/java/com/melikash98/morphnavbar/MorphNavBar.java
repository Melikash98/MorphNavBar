package com.melikash98.morphnavbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.ColorUtils;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.melikash98.morphnavbar.Interface.ClickListener;
import com.melikash98.morphnavbar.Interface.OnTabSelectedListener;
import com.melikash98.morphnavbar.Interface.ReselectListener;
import com.melikash98.morphnavbar.Interface.ShowListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MorphNavBar extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 1400;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint inactiveIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint activeIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint labelPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint.FontMetrics labelFontMetrics = new Paint.FontMetrics();
    private final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint badgeTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);


    private final RectF barRect = new RectF();
    private final Path bubblePath = new Path();

    private final List<MorphNavTabItem.Model> items = new ArrayList<>();
    private final List<Float> centerXs = new ArrayList<>();
    private final Map<Integer, String> badgeCounts = new HashMap<>();
    private final Map<Integer, Float> shakeOffsets = new HashMap<>();
    private final Map<Integer, ValueAnimator> shakeAnimators = new HashMap<>();

    private final FastOutSlowInInterpolator positionInterpolator = new FastOutSlowInInterpolator();

    private ValueAnimator animator;
    private OnTabSelectedListener listener;
    private ClickListener clickListener;
    private ShowListener showListener;
    private ReselectListener reselectListener;

    private boolean showLabelOnlyOnSelected = false;


    private int badgeBackgroundColor = Color.RED;
    private int badgeTextColor = Color.WHITE;
    private float badgeTextSizePx = sp(11.5f);
    ;
    private int selectedIndex = 0;
    private int fromIndex = 0;
    private int toIndex = 0;
    private float progress = 1f;

    private int barColor;
    private int shadowColor;
    private int selectedColor;
    private int inactiveIconColor;
    private int activeIconColor;
    private int unselectedColor;

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
    private static final float DEFAULT_LABEL_TOP_GAP_DP = 4.5f;
    private float horizontalContentPadding = dp(14f);
    private static final float LABEL_BOTTOM_PADDING_DP = 26f;
    private static final float SHAKE_AMPLITUDE_DP = 5.2f;
    private static final int SHAKE_DURATION_MS = 760;
    private static final float SHAKE_FREQUENCY = 7.8f;
    private static final float MAX_STRETCH_FACTOR = 2.40f;
    private static final float STRETCH_DURATION_FACTOR = 0.40f;


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
        unselectedColor = Color.parseColor("#00CFC0");

        barRadius = dp(26f);
        barHeight = dp(100f);
        barSideMargin = dp(0f);
        barBottomMargin = dp(0f);
        bubbleDiameter = dp(60f);
        itemIconSize = dp(34f);
        shadowBlur = dp(12f);
        shadowDy = dp(4f);
        animationDuration = DEFAULT_ANIMATION_DURATION;

        showLabels = true;
        showLabelOnlyOnSelected = false;
        labelTextSizePx = sp(DEFAULT_LABEL_SIZE_SP);
        labelFontFamily = "sans-serif";

    }

    private void readAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) return;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MorphNavBarView, defStyleAttr, 0);
        try {
            barColor = a.getColor(R.styleable.MorphNavBarView_lbv_barColor, barColor);
            shadowColor = a.getColor(R.styleable.MorphNavBarView_lbv_shadowColor, shadowColor);
            selectedColor = a.getColor(R.styleable.MorphNavBarView_lbv_selectedColor, selectedColor);
            inactiveIconColor = a.getColor(R.styleable.MorphNavBarView_lbv_inactiveIconColor, inactiveIconColor);
            activeIconColor = a.getColor(R.styleable.MorphNavBarView_lbv_activeIconColor, activeIconColor);

            barRadius = a.getDimension(R.styleable.MorphNavBarView_lbv_barRadius, barRadius);
            barHeight = a.getDimension(R.styleable.MorphNavBarView_lbv_barHeight, barHeight);
            barSideMargin = a.getDimension(R.styleable.MorphNavBarView_lbv_barSideMargin, barSideMargin);
            barBottomMargin = a.getDimension(R.styleable.MorphNavBarView_lbv_barBottomMargin, barBottomMargin);

            itemIconSize = a.getDimension(R.styleable.MorphNavBarView_lbv_itemIconSize, itemIconSize);
            shadowBlur = a.getDimension(R.styleable.MorphNavBarView_lbv_shadowBlur, shadowBlur);
            shadowDy = a.getDimension(R.styleable.MorphNavBarView_lbv_shadowDy, shadowDy);
            animationDuration = a.getInteger(R.styleable.MorphNavBarView_lbv_animationDuration, animationDuration);

            showLabels = a.getBoolean(R.styleable.MorphNavBarView_lbv_showLabels, showLabels);
            labelTextSizePx = a.getDimension(R.styleable.MorphNavBarView_lbv_labelTextSize, labelTextSizePx);

            showLabelOnlyOnSelected = a.getBoolean(R.styleable.MorphNavBarView_lbv_showLabelOnlyOnSelected, false);

            String family = a.getString(R.styleable.MorphNavBarView_lbv_labelFontFamily);
            if (family != null && !family.trim().isEmpty()) {
                labelFontFamily = family.trim();
            }

            badgeBackgroundColor = a.getColor(R.styleable.MorphNavBarView_lbv_badgeBackgroundColor, badgeBackgroundColor);
            badgeTextColor = a.getColor(R.styleable.MorphNavBarView_lbv_badgeTextColor, badgeTextColor);
            badgeTextSizePx = a.getDimension(R.styleable.MorphNavBarView_lbv_badgeTextSize, badgeTextSizePx);
            unselectedColor = a.getColor(R.styleable.MorphNavBarView_lbv_unselectedColor, unselectedColor);

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

        badgePaint.setStyle(Paint.Style.FILL);
        badgePaint.setColor(badgeBackgroundColor);

        badgeTextPaint.setTextAlign(Paint.Align.CENTER);
        badgeTextPaint.setColor(badgeTextColor);
        badgeTextPaint.setTextSize(badgeTextSizePx);
        badgeTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setBadgeBackgroundColor(@ColorInt int color) {
        this.badgeBackgroundColor = color;
        badgePaint.setColor(color);
        invalidate();
    }

    public void setBadgeTextColor(@ColorInt int color) {
        this.badgeTextColor = color;
        badgeTextPaint.setColor(color);
        invalidate();
    }

    public void setBadgeTextSizeSp(float sizeSp) {
        this.badgeTextSizePx = sp(sizeSp);
        badgeTextPaint.setTextSize(badgeTextSizePx);
        invalidate();
    }

    private void applyLabelTypeface() {
        Typeface tf = Typeface.create(labelFontFamily, Typeface.BOLD);
        if (tf == null) {
            tf = Typeface.DEFAULT_BOLD;
        }
        labelPaint.setTypeface(tf);
    }

    public void setTabs(@NonNull List<MorphNavTabItem.Model> tabs) {
        items.clear();
        items.addAll(tabs);
        updateHasAnyLabel();
        resetSelection();
        rebuildCenters();
        updateContentDescription();
        requestLayout();
        invalidate();

        if (!items.isEmpty() && showListener != null) {
            showListener.onShowItem(items.get(selectedIndex));
        }
    }

    public void setTabs(@NonNull MorphNavTabItem.Model... tabs) {
        items.clear();
        Collections.addAll(items, tabs);
        updateHasAnyLabel();
        resetSelection();
        rebuildCenters();
        updateContentDescription();
        requestLayout();
        invalidate();

        if (!items.isEmpty() && showListener != null) {
            showListener.onShowItem(items.get(selectedIndex));
        }
    }

    public void add(@NonNull MorphNavTabItem.Model tab) {
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

        if (!items.isEmpty() && showListener != null) {
            showListener.onShowItem(items.get(selectedIndex));
        }
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

    public void setOnClickMenuListener(@Nullable ClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnShowListener(@Nullable ShowListener listener) {
        this.showListener = listener;
    }

    public void setOnReselectListener(@Nullable ReselectListener listener) {
        this.reselectListener = listener;
    }

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void setCount(int tabIndex, String count) {
        if (tabIndex < 0 || tabIndex >= items.size()) return;
        if (count == null || count.trim().isEmpty()) {
            badgeCounts.remove(tabIndex);
        } else {
            badgeCounts.put(tabIndex, count.trim());
        }
        invalidate();
    }

    public void setCount(int tabIndex, int count) {
        setCount(tabIndex, String.valueOf(count));
    }

    public void clearCount(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= items.size()) return;
        badgeCounts.remove(tabIndex);
        invalidate();
    }

    public void clearAllCounts() {
        badgeCounts.clear();
        invalidate();
    }

    public void show(int tabIndex) {
        setSelectedIndex(tabIndex);
    }

    public void setShowLabelOnlyOnSelected(boolean enabled) {
        if (this.showLabelOnlyOnSelected == enabled) return;
        this.showLabelOnlyOnSelected = enabled;
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
        final int oldIndex = selectedIndex;
        if (!animate || centerXs.isEmpty()) {
            selectedIndex = index;
            fromIndex = index;
            toIndex = index;
            progress = 1f;

            if (oldIndex != index) {
                startShake(oldIndex);
            } else if (reselectListener != null) {
                startShake(index);
            }

            updateContentDescription();

            if (listener != null) listener.onTabSelected(selectedIndex, items.get(selectedIndex));
            if (showListener != null && !items.isEmpty()) {
                showListener.onShowItem(items.get(selectedIndex));
            }
            invalidate();
            return;
        }

        fromIndex = selectedIndex;
        toIndex = index;
        progress = 0f;

        startShake(fromIndex);

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
                if (showListener != null && !items.isEmpty()) {
                    showListener.onShowItem(items.get(selectedIndex));
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
        for (MorphNavTabItem.Model item : items) {
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
        MorphNavTabItem.Model item = items.get(selectedIndex);
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
        float labelArea = getLabelAreaHeightPx();
        int desiredHeight = (int) Math.ceil(
                getPaddingTop() + getPaddingBottom()
                        + barHeight
                        + bubbleDiameter * 0.42f
                        + barBottomMargin
                        + labelArea
        );
        setMeasuredDimension(
                resolveSize(desiredWidth, widthMeasureSpec),
                resolveSize(desiredHeight, heightMeasureSpec)
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float labelArea = getLabelAreaHeightPx();

        float left = getPaddingLeft() + barSideMargin;
        float right = w - getPaddingRight() - barSideMargin;
        float bottom = h - getPaddingBottom() - barBottomMargin;
        float top = bottom - (barHeight + labelArea);

        barRect.set(left, top, right, bottom);

        bubbleCenterY = top + bubbleDiameter * 0.5f;
        activeIconY = bubbleCenterY;
        inactiveIconY = bubbleCenterY;

        rebuildCenters();

        if ((showLabels || showLabelOnlyOnSelected) && hasAnyLabel) {
            labelPaint.getFontMetrics(labelFontMetrics);
            labelBaselineY = barRect.bottom - dp(LABEL_BOTTOM_PADDING_DP) - labelFontMetrics.bottom;
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
        float effectiveWidth = barRect.width() - 2 * horizontalContentPadding;
        float seg = effectiveWidth / items.size();
        for (int i = 0; i < items.size(); i++) {
            centerXs.add(barRect.left + horizontalContentPadding + seg * (i + 0.5f));
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

        if ((showLabels || showLabelOnlyOnSelected) && hasAnyLabel) {
            drawLabels(canvas, bubbleX, eased);
        }
        drawBadges(canvas);
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
        float bulgeDepth = dp(11f) + dp(16f) * pulse;
        float bumpWidth = bubbleDiameter * 1.65f;
        float bumpLeft = Math.max(left + radius * 0.6f, bubbleX - bumpWidth / 2f);
        float bumpRight = Math.min(right - radius * 0.6f, bubbleX + bumpWidth / 2f);
        float bulgeTop = top - bulgeDepth;

        path.moveTo(left + radius, top);
        path.lineTo(bumpLeft, top);
        path.cubicTo(bumpLeft + bumpWidth * 0.25f, top, bubbleX - bumpWidth * 0.19f, bulgeTop, bubbleX, bulgeTop);
        path.cubicTo(bubbleX + bumpWidth * 0.19f, bulgeTop, bumpRight - bumpWidth * 0.25f, top, bumpRight, top);
        path.lineTo(right - radius, top);
        path.quadTo(right, top, right, top + radius);
        path.lineTo(right, bottom);
        path.lineTo(left, bottom);
        path.lineTo(left, top + radius);
        path.quadTo(left, top, left + radius, top);
        path.close();
        return path;
    }

    private void drawInactiveIcons(Canvas canvas, float bubbleX) {
        float influenceRadius = bubbleDiameter * 0.9f;
        for (int i = 0; i < items.size(); i++) {
            MorphNavTabItem.Model item = items.get(i);
            float centerX = centerXs.get(i);
            float distance = Math.abs(centerX - bubbleX);
            float t = clamp(1f - (distance / influenceRadius), 0f, 1f);
            float eased = positionInterpolator.getInterpolation(t);
            float inactiveAlpha = 1f - 0.88f * eased;
            Drawable icon = loadDrawable(item.getIconResId());
            if (icon != null) {
                float shakeX = getShakeOffset(i);
                drawDrawable(canvas, icon, centerX + shakeX, inactiveIconY, inactiveIconColor, inactiveAlpha);
            }
        }
    }

    private void drawBubble(Canvas canvas, float bubbleX, float eased) {
        float r = bubbleDiameter / 2f;
        float stretchProgress = (float) Math.pow(Math.abs(eased - 0.5f) * 2.0f, STRETCH_DURATION_FACTOR);
        float stretchAmount = (float) Math.pow(Math.sin(Math.PI * stretchProgress), 0.55f);
        float stretchFactor = 1f + (MAX_STRETCH_FACTOR - 1f) * stretchAmount;
        float verticalCompress = 0.92f - 0.13f * (stretchFactor - 1f);
        float mainRadiusX = r * stretchFactor;
        float mainRadiusY = r * verticalCompress;
        float mainY = bubbleCenterY + dp(2f);
        float scalePulse = 1f + 0.068f * (float) Math.sin(Math.PI * eased * 1.65f);
        float finalRadiusY = r * verticalCompress * scalePulse;
        Path mainPath = new Path();
        mainPath.addOval(
                bubbleX - mainRadiusX,
                mainY - mainRadiusY,
                bubbleX + mainRadiusX,
                mainY + mainRadiusY,
                Path.Direction.CW
        );

        bubblePath.reset();
        bubblePath.set(mainPath);

        canvas.drawPath(bubblePath, bubblePaint);
        if (eased > 0.08f && eased < 0.92f) {
            float highlightRadius = r * 0.21f * (1.7f - stretchFactor * 0.38f);
            float highlightY = mainY - finalRadiusY * 0.47f;
            canvas.drawCircle(bubbleX, highlightY, highlightRadius, bubblePaint);
        }
    }

    private void drawActiveIcon(Canvas canvas, float bubbleX, float eased) {
        int iconIndex = (eased < 0.45f) ? fromIndex : toIndex;
        if (iconIndex < 0 || iconIndex >= items.size()) return;
        MorphNavTabItem.Model item = items.get(iconIndex);
        float scale = 1f + 0.085f * (float) Math.sin(Math.PI * eased * 1.1f);
        Drawable icon = item.getSelectedIconResId() != 0
                ? loadDrawable(item.getSelectedIconResId())
                : loadDrawable(item.getIconResId());

        if (icon != null) {
            float shakeX = (eased < 0.5f) ? getShakeOffset(iconIndex) : 0f;
            drawDrawable(canvas, icon, bubbleX + shakeX, activeIconY, activeIconColor, 1f, scale);
        }

    }

    private void drawDrawable(Canvas canvas, @NonNull Drawable drawable, float centerX, float centerY,
                              @ColorInt int tint, float alpha) {
        drawDrawable(canvas, drawable, centerX, centerY, tint, alpha, 1f);
    }

    private void drawLabels(Canvas canvas, float bubbleX, float eased) {
        if (!showLabels || !hasAnyLabel || items.isEmpty()) return;
        if (showLabelOnlyOnSelected) {
            int labelIndex = eased < 0.5f ? fromIndex : toIndex;
            if (labelIndex < 0 || labelIndex >= items.size()) return;

            MorphNavTabItem.Model item = items.get(labelIndex);
            CharSequence label = item.getLabel();
            if (label == null || label.length() == 0) return;

            float centerX = centerXs.get(labelIndex);
            labelPaint.setColor(selectedColor);

            float segmentWidth = barRect.width() / Math.max(1, items.size());
            float maxTextWidth = segmentWidth - dp(10f);

            CharSequence ellipsized = TextUtils.ellipsize(
                    label, labelPaint, maxTextWidth, TextUtils.TruncateAt.END);

            canvas.drawText(ellipsized.toString(), centerX, labelBaselineY, labelPaint);
            return;
        }
        float influenceRadius = bubbleDiameter * 0.9f;
        float segmentWidth = barRect.width() / Math.max(1, items.size());
        float maxTextWidth = segmentWidth - dp(10f);

        for (int i = 0; i < items.size(); i++) {
            MorphNavTabItem.Model item = items.get(i);
            CharSequence label = item.getLabel();
            if (label == null || label.length() == 0) continue;

            float centerX = centerXs.get(i);
            float distance = Math.abs(centerX - bubbleX);
            float t = clamp(1f - (distance / influenceRadius), 0f, 1f);
            eased = positionInterpolator.getInterpolation(t);

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
                    MorphNavTabItem.Model item = items.get(index);

                    if (clickListener != null) clickListener.onClickItem(item);

                    if (index == selectedIndex) {
                        if (reselectListener != null) reselectListener.onReselectItem(item);
                        startShake(index);
                    } else {
                        setSelectedIndex(index, true);
                    }
                    performClick();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void drawBadges(Canvas canvas) {
        if (badgeCounts.isEmpty() || items.isEmpty()) return;

        float eased = positionInterpolator.getInterpolation(progress);
        float bubbleX = lerp(getCenterX(fromIndex), getCenterX(toIndex), eased);

        float badgeRadius = dp(9.5f);
        float badgeOffsetX = itemIconSize * 0.38f;
        float badgeOffsetY = -itemIconSize * 0.35f;

        for (int i = 0; i < items.size(); i++) {
            String count = badgeCounts.get(i);
            if (count == null || count.isEmpty()) continue;

            float iconCenterX = centerXs.get(i);
            float iconCenterY = inactiveIconY;

            boolean isMovingToThis = (i == toIndex);
            boolean isMovingFromThis = (i == fromIndex);

            if (isMovingToThis && eased > 0.5f) {
                iconCenterX = bubbleX;
                iconCenterY = activeIconY;
            }
            else if (isMovingFromThis && eased < 0.5f) {
                iconCenterX = bubbleX;
                iconCenterY = activeIconY;
            }

            float badgeCenterX = iconCenterX + badgeOffsetX;
            float badgeCenterY = iconCenterY + badgeOffsetY;

            canvas.drawCircle(badgeCenterX, badgeCenterY, badgeRadius, badgePaint);

            float textY = badgeCenterY + (badgeTextPaint.getTextSize() * 0.35f);
            canvas.drawText(count, badgeCenterX, textY, badgeTextPaint);
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
        cancelShakeAnimations();
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

        if (!items.isEmpty() && showListener != null) {
            showListener.onShowItem(items.get(selectedIndex));
        }
    }

    private void startShake(int index) {
        if (index < 0 || index >= items.size()) return;
        ValueAnimator running = shakeAnimators.remove(index);
        if (running != null) running.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(SHAKE_DURATION_MS);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            float damping = (float) Math.pow(1f - t, 3.5f);

            float oscillation = (float) Math.sin(t * Math.PI * SHAKE_FREQUENCY);

            float amplitude = dp(SHAKE_AMPLITUDE_DP);
            float offset = oscillation * damping * amplitude;

            shakeOffsets.put(index, offset);
            invalidate();
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shakeOffsets.remove(index);
                shakeAnimators.remove(index);
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                shakeOffsets.remove(index);
                shakeAnimators.remove(index);
                invalidate();
            }
        });

        shakeAnimators.put(index, animator);
        animator.start();
    }


    private float getShakeOffset(int index) {
        Float value = shakeOffsets.get(index);
        return value != null ? value : 0f;
    }

    private void cancelShakeAnimations() {
        for (ValueAnimator animator : shakeAnimators.values()) {
            animator.cancel();
        }
        shakeAnimators.clear();
        shakeOffsets.clear();
    }
}
