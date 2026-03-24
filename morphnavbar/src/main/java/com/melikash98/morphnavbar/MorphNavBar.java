package com.melikash98.morphnavbar;

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
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;
import android.os.Build;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class MorphNavBar extends FrameLayout {
    public interface OnTabSelectedListener {
        void onTabSelected(int index, MorphTabItem item);
    }

    private final List<MorphTabItem> items = new ArrayList<>();
    private final List<TabButton> tabButtons = new ArrayList<>();

    private BackgroundView backgroundView;
    private LinearLayout itemsContainer;
    private View bubbleView;
    private ImageView bubbleIconView;

    private OnTabSelectedListener listener;

    private int selectedIndex = 0;

    private int barColor = Color.WHITE;
    private int bubbleColor = Color.parseColor("#17D8D0");
    private int selectedIconColor = Color.WHITE;
    private int unselectedIconColor = Color.parseColor("#17D8D0");

    private int barHeightPx;
    private int barCornerRadiusPx;
    private int barInsetPx;
    private int bubbleSizePx;
    private int bubbleOverlapPx;
    private int iconSizePx;
    private int waveHeightPx;
    private int waveWidthPx;
    private int animationDuration = 320;
    private int shadowRadiusPx;
    private int shadowAlpha = 18;

    private int indicatorWidthPx;
    private int indicatorHeightPx;
    private int indicatorBottomInsetPx;
    private int indicatorColor = Color.argb(18, 0, 0, 0);

    private float currentBubbleCenterX = -1f;
    private float waveMorphFraction = 1f;
    private int waveDirection = 1;

    private float bubbleTravelStretchX = 1.46f;
    private float bubbleTravelSquashY = 0.90f;
    private float selectionIconSwapFraction = 0.72f;

    private boolean firstLayoutDone = false;
    private ValueAnimator bubbleMoveAnimator;
    private ObjectAnimator waveMorphAnimator;
    private Runnable pendingIconSwap;

    private final Interpolator motionInterpolator;
    private final Interpolator iconInterpolator = new AccelerateDecelerateInterpolator();

    public MorphNavBar(Context context) {
        this(context, null);
    }

    public MorphNavBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MorphNavBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        motionInterpolator = createMotionInterpolator();
        initDefaults(context);
        readAttrs(context, attrs, defStyleAttr);
        initViews(context);
    }

    private Interpolator createMotionInterpolator() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new PathInterpolator(0.22f, 0f, 0.18f, 1f);
        }
        return new AccelerateDecelerateInterpolator();
    }

    private void initDefaults(Context context) {
        barHeightPx = dp(context, 86);
        barCornerRadiusPx = dp(context, 28);
        barInsetPx = dp(context, 18);

        bubbleSizePx = dp(context, 54);
        bubbleOverlapPx = dp(context, 26);

        iconSizePx = dp(context, 20);

        waveHeightPx = dp(context, 14);
        waveWidthPx = dp(context, 70);

        shadowRadiusPx = dp(context, 18);
        indicatorWidthPx = dp(context, 60);
        indicatorHeightPx = dp(context, 2);
        indicatorBottomInsetPx = dp(context, 14);
    }

    private void readAttrs(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) return;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MorphNavBar, defStyleAttr, 0);
        try {
            barColor = ta.getColor(R.styleable.MorphNavBar_barColor, barColor);
            bubbleColor = ta.getColor(R.styleable.MorphNavBar_bubbleColor, bubbleColor);
            selectedIconColor = ta.getColor(R.styleable.MorphNavBar_selectedIconColor, selectedIconColor);
            unselectedIconColor = ta.getColor(R.styleable.MorphNavBar_unselectedIconColor, unselectedIconColor);

            barHeightPx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_barHeight, barHeightPx);
            barCornerRadiusPx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_barCornerRadius, barCornerRadiusPx);
            bubbleSizePx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_bubbleSize, bubbleSizePx);
            bubbleOverlapPx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_bubbleOverlap, bubbleOverlapPx);
            iconSizePx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_iconSize, iconSizePx);

            waveHeightPx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_waveHeight, waveHeightPx);
            waveWidthPx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_waveWidth, waveWidthPx);

            animationDuration = ta.getInt(R.styleable.MorphNavBar_animationDuration, animationDuration);
            shadowRadiusPx = ta.getDimensionPixelSize(R.styleable.MorphNavBar_shadowRadius, shadowRadiusPx);
            shadowAlpha = ta.getInt(R.styleable.MorphNavBar_shadowAlpha, shadowAlpha);
        } finally {
            ta.recycle();
        }
    }

    private void initViews(Context context) {
        setClipChildren(false);
        setClipToPadding(false);

        backgroundView = new BackgroundView(context);
        addView(backgroundView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        itemsContainer = new LinearLayout(context);
        itemsContainer.setOrientation(LinearLayout.HORIZONTAL);
        itemsContainer.setGravity(Gravity.CENTER_VERTICAL);
        itemsContainer.setClipChildren(false);
        itemsContainer.setClipToPadding(false);
        addView(itemsContainer, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        bubbleView = new View(context);
        bubbleView.setBackground(createBubbleDrawable());
        bubbleView.setElevation(dp(context, 10));

        LayoutParams bubbleLp = new LayoutParams(bubbleSizePx, bubbleSizePx);
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        addView(bubbleView, bubbleLp);

        bubbleIconView = new ImageView(context);
        bubbleIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(selectedIconColor));
        addView(
                bubbleIconView,
                new LayoutParams(iconSizePx, iconSizePx, Gravity.TOP | Gravity.START)
        );

        bubbleView.setVisibility(INVISIBLE);
        bubbleIconView.setVisibility(INVISIBLE);
    }

    private GradientDrawable createBubbleDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(bubbleColor);
        drawable.setCornerRadius(bubbleSizePx / 2f);
        return drawable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            int desiredHeight = Math.max(barHeightPx + bubbleOverlapPx, bubbleSizePx + bubbleOverlapPx);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        backgroundView.layout(0, 0, width, height);

        int barLeft = barInsetPx;
        int barRight = Math.max(barLeft, width - barInsetPx);

        int itemsTop = bubbleOverlapPx;
        int itemsBottom = Math.min(height, bubbleOverlapPx + barHeightPx);
        itemsContainer.layout(barLeft, itemsTop, barRight, itemsBottom);

        if (!items.isEmpty()) {
            if (!firstLayoutDone) {
                firstLayoutDone = true;
                post(() -> applySelection(selectedIndex, false, false));
            } else {
                syncBubblePosition(false);
            }
        }
    }

    public void setItems(List<MorphTabItem> newItems) {
        items.clear();
        items.addAll(newItems);

        tabButtons.clear();
        itemsContainer.removeAllViews();

        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            MorphTabItem item = items.get(i);

            TabButton button = new TabButton(getContext());
            button.iconView.setImageResource(item.iconRes);
            ImageViewCompat.setImageTintList(
                    button.iconView,
                    ColorStateList.valueOf(unselectedIconColor)
            );

            ViewGroup.LayoutParams iconLp = button.iconView.getLayoutParams();
            iconLp.width = iconSizePx;
            iconLp.height = iconSizePx;
            button.iconView.setLayoutParams(iconLp);

            button.setOnClickListener(v -> setSelectedIndex(index, true));

            itemsContainer.addView(
                    button,
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            );

            tabButtons.add(button);
        }

        if (!items.isEmpty()) {
            if (selectedIndex >= items.size()) selectedIndex = 0;
            post(() -> applySelection(selectedIndex, false, false));
        }

        backgroundView.invalidate();
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        setSelectedIndex(index, true);
    }

    public void setSelectedIndex(int index, boolean animate) {
        applySelection(index, animate, true);
    }

    public void setBarColor(int color) {
        this.barColor = color;
        backgroundView.invalidate();
    }

    public void setBubbleColor(int color) {
        this.bubbleColor = color;
        if (bubbleView.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) bubbleView.getBackground()).setColor(color);
        }
    }

    public void setSelectedIconColor(int color) {
        this.selectedIconColor = color;
        updateTabVisuals();
        ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(color));
    }

    public void setUnselectedIconColor(int color) {
        this.unselectedIconColor = color;
        updateTabVisuals();
    }

    public void setBarHeightPx(int px) {
        this.barHeightPx = px;
        requestLayout();
    }

    public void setBarCornerRadiusPx(float px) {
        this.barCornerRadiusPx = (int) px;
        backgroundView.invalidate();
    }

    public void setBarInsetPx(int px) {
        this.barInsetPx = Math.max(0, px);
        requestLayout();
    }

    public void setBubbleSizePx(int px) {
        this.bubbleSizePx = px;

        ViewGroup.LayoutParams lp = bubbleView.getLayoutParams();
        lp.width = px;
        lp.height = px;
        bubbleView.setLayoutParams(lp);

        if (bubbleView.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) bubbleView.getBackground()).setCornerRadius(px / 2f);
        }

        requestLayout();
    }

    public void setBubbleOverlapPx(int px) {
        this.bubbleOverlapPx = px;
        requestLayout();
    }

    public void setIconSizePx(int px) {
        this.iconSizePx = px;

        ViewGroup.LayoutParams lp = bubbleIconView.getLayoutParams();
        lp.width = px;
        lp.height = px;
        bubbleIconView.setLayoutParams(lp);

        for (TabButton button : tabButtons) {
            ViewGroup.LayoutParams childLp = button.iconView.getLayoutParams();
            childLp.width = px;
            childLp.height = px;
            button.iconView.setLayoutParams(childLp);
        }

        requestLayout();
    }

    public void setWaveHeightPx(int px) {
        this.waveHeightPx = px;
        backgroundView.invalidate();
    }

    public void setWaveWidthPx(int px) {
        this.waveWidthPx = px;
        backgroundView.invalidate();
    }

    public void setAnimationDuration(int ms) {
        this.animationDuration = ms;
    }

    public void setShadowRadiusPx(int px) {
        this.shadowRadiusPx = px;
        backgroundView.invalidate();
    }

    public void setShadowAlpha(int alpha) {
        this.shadowAlpha = alpha;
        backgroundView.invalidate();
    }

    public void setBubbleTravelStretchX(float scale) {
        this.bubbleTravelStretchX = scale;
    }

    public void setBubbleTravelSquashY(float scale) {
        this.bubbleTravelSquashY = scale;
    }

    public void setSelectionIconSwapFraction(float fraction) {
        this.selectionIconSwapFraction = fraction;
    }

    public float getWaveMorphFraction() {
        return waveMorphFraction;
    }

    public void setWaveMorphFraction(float value) {
        this.waveMorphFraction = value;
        backgroundView.invalidate();
    }

    private void applySelection(int index, boolean animate, boolean notify) {
        if (items.isEmpty()) return;
        if (index < 0 || index >= items.size()) return;

        int oldIndex = selectedIndex;
        selectedIndex = index;
        waveDirection = (index >= oldIndex) ? 1 : -1;

        updateTabVisuals();

        if (bubbleView.getVisibility() != VISIBLE) {
            bubbleView.setVisibility(VISIBLE);
        }
        if (bubbleIconView.getVisibility() != VISIBLE) {
            bubbleIconView.setVisibility(VISIBLE);
        }

        if (!animate) {
            cancelRunningAnimations();
            MorphTabItem item = items.get(index);
            bubbleIconView.setAlpha(1f);
            bubbleIconView.setImageResource(item.iconRes);
            ImageViewCompat.setImageTintList(
                    bubbleIconView,
                    ColorStateList.valueOf(selectedIconColor)
            );
            syncBubblePosition(false);
        } else {
            MorphTabItem item = items.get(index);
            animateBubbleToIndex(index);
            startWaveMorphAnimation();
            scheduleDelayedIconSwap(item.iconRes);
        }

        if (notify && listener != null && oldIndex != index) {
            listener.onTabSelected(index, items.get(index));
        }
    }

    private void updateTabVisuals() {
        for (int i = 0; i < tabButtons.size(); i++) {
            TabButton button = tabButtons.get(i);
            boolean selected = i == selectedIndex;

            button.iconView.animate()
                    .alpha(selected ? 0f : 1f)
                    .scaleX(selected ? 0.74f : 1f)
                    .scaleY(selected ? 0.74f : 1f)
                    .setDuration(140)
                    .setInterpolator(iconInterpolator)
                    .start();

            ImageViewCompat.setImageTintList(
                    button.iconView,
                    ColorStateList.valueOf(selected ? selectedIconColor : unselectedIconColor)
            );
        }
    }

    private void scheduleDelayedIconSwap(final int newIconRes) {
        cancelPendingIconSwap();

        final long delay = Math.round(animationDuration * selectionIconSwapFraction);
        pendingIconSwap = new Runnable() {
            @Override
            public void run() {
                bubbleIconView.animate()
                        .alpha(0f)
                        .setDuration(Math.max(60, animationDuration / 5L))
                        .setInterpolator(iconInterpolator)
                        .withEndAction(() -> {
                            bubbleIconView.setImageResource(newIconRes);
                            ImageViewCompat.setImageTintList(
                                    bubbleIconView,
                                    ColorStateList.valueOf(selectedIconColor)
                            );
                            bubbleIconView.animate()
                                    .alpha(1f)
                                    .setDuration(Math.max(70, animationDuration / 4L))
                                    .setInterpolator(iconInterpolator)
                                    .start();
                        })
                        .start();
            }
        };
        postDelayed(pendingIconSwap, delay);
    }

    private void cancelPendingIconSwap() {
        if (pendingIconSwap != null) {
            removeCallbacks(pendingIconSwap);
            pendingIconSwap = null;
        }
        bubbleIconView.animate().cancel();
    }

    private void animateBubbleToIndex(int index) {
        if (index < 0 || index >= tabButtons.size()) return;

        View target = tabButtons.get(index);
        if (target.getWidth() == 0) {
            post(() -> animateBubbleToIndex(index));
            return;
        }

        float targetCenterX = getAbsoluteCenterX(target);

        if (currentBubbleCenterX < 0) {
            currentBubbleCenterX = targetCenterX;
            positionBubble(targetCenterX, 1f, 1f);
            backgroundView.setWaveCenterX(targetCenterX);
            backgroundView.invalidate();
            return;
        }

        if (bubbleMoveAnimator != null) {
            bubbleMoveAnimator.cancel();
        }

        bubbleMoveAnimator = ValueAnimator.ofFloat(currentBubbleCenterX, targetCenterX);
        bubbleMoveAnimator.setDuration(animationDuration);
        bubbleMoveAnimator.setInterpolator(motionInterpolator);
        bubbleMoveAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            float fraction = animation.getAnimatedFraction();
            float waveFraction = animation.getAnimatedFraction();

            currentBubbleCenterX = value;
            positionBubble(value,
                    1f + (bubbleTravelStretchX - 1f) * (float) Math.sin(Math.PI * fraction),
                    1f - (1f - bubbleTravelSquashY) * (float) Math.sin(Math.PI * fraction));

            backgroundView.setWaveCenterX(value);
            backgroundView.invalidate();

            float swell = (float) Math.sin(Math.PI * fraction);
            bubbleView.setScaleX(1f + 0.02f * swell);
            bubbleView.setScaleY(1f - 0.02f * swell);

            // Slightly asymmetrical morph to mimic the liquid slide in the reference.
            waveMorphFraction = 0.92f + (0.08f * waveFraction);
        });
        bubbleMoveAnimator.start();
    }

    private void positionBubble(float centerX, float scaleX, float scaleY) {
        bubbleView.setVisibility(VISIBLE);
        bubbleIconView.setVisibility(VISIBLE);

        bubbleView.setTranslationX(centerX - (bubbleSizePx / 2f));
        bubbleView.setTranslationY(0f);
        bubbleView.setScaleX(scaleX);
        bubbleView.setScaleY(scaleY);

        bubbleIconView.setTranslationX(centerX - (iconSizePx / 2f));
        bubbleIconView.setTranslationY((bubbleSizePx / 2f) - (iconSizePx / 2f));
    }

    private void startWaveMorphAnimation() {
        if (waveMorphAnimator != null) {
            waveMorphAnimator.cancel();
        }

        PropertyValuesHolder holder = PropertyValuesHolder.ofKeyframe(
                "waveMorphFraction",
                Keyframe.ofFloat(0f, 0.94f),
                Keyframe.ofFloat(0.22f, 1.12f),
                Keyframe.ofFloat(0.58f, 0.98f),
                Keyframe.ofFloat(1f, 1f)
        );

        waveMorphAnimator = ObjectAnimator.ofPropertyValuesHolder(this, holder);
        waveMorphAnimator.setDuration(animationDuration);
        waveMorphAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        waveMorphAnimator.start();
    }

    private void syncBubblePosition(boolean animate) {
        if (items.isEmpty() || selectedIndex < 0 || selectedIndex >= tabButtons.size()) return;

        View target = tabButtons.get(selectedIndex);
        if (target.getWidth() == 0) {
            post(() -> syncBubblePosition(animate));
            return;
        }

        float targetCenterX = getAbsoluteCenterX(target);
        currentBubbleCenterX = targetCenterX;

        if (!animate) {
            positionBubble(targetCenterX, 1f, 1f);
            backgroundView.setWaveCenterX(targetCenterX);
            backgroundView.invalidate();
        }
    }

    private float getAbsoluteCenterX(View child) {
        return itemsContainer.getLeft() + child.getLeft() + (child.getWidth() / 2f);
    }

    private void cancelRunningAnimations() {
        if (bubbleMoveAnimator != null) {
            bubbleMoveAnimator.cancel();
            bubbleMoveAnimator = null;
        }
        if (waveMorphAnimator != null) {
            waveMorphAnimator.cancel();
            waveMorphAnimator = null;
        }
        cancelPendingIconSwap();
        bubbleView.animate().cancel();
        bubbleIconView.animate().cancel();
    }

    private int dp(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private class BackgroundView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Path path = new Path();

        private float waveCenterX = -1f;

        BackgroundView(Context context) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
            indicatorPaint.setStyle(Paint.Style.FILL);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        void setWaveCenterX(float x) {
            this.waveCenterX = x;
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float top = bubbleOverlapPx;
            float bottom = top + barHeightPx;
            float left = barInsetPx;
            float right = w - barInsetPx;
            float radius = barCornerRadiusPx;

            if (waveCenterX < 0) {
                waveCenterX = w / 2f;
            }

            paint.setColor(barColor);
            paint.setShadowLayer(
                    shadowRadiusPx,
                    0f,
                    dp(getContext(), 4),
                    Color.argb(shadowAlpha, 0, 0, 0)
            );

            float waveWidthScale = 0.94f + (0.08f * waveMorphFraction);
            float waveHeightScale = 0.84f + (0.22f * waveMorphFraction);

            float localWaveWidth = waveWidthPx * waveWidthScale;
            float localWaveHeight = waveHeightPx * waveHeightScale;

            float waveStart = Math.max(left + radius, waveCenterX - (localWaveWidth / 2f));
            float waveEnd = Math.min(right - radius, waveCenterX + (localWaveWidth / 2f));
            float wavePeakY = top - localWaveHeight;

            float directionSkew =
                    waveDirection * localWaveWidth * 0.035f * (1f - Math.abs(waveMorphFraction - 0.5f) * 2f);

            path.reset();

            path.moveTo(left + radius, top);
            path.lineTo(waveStart, top);

            path.cubicTo(
                    waveStart + localWaveWidth * 0.10f,
                    top,
                    waveCenterX - localWaveWidth * 0.18f + directionSkew,
                    wavePeakY,
                    waveCenterX,
                    wavePeakY
            );

            path.cubicTo(
                    waveCenterX + localWaveWidth * 0.18f + directionSkew,
                    wavePeakY,
                    waveEnd - localWaveWidth * 0.10f,
                    top,
                    waveEnd,
                    top
            );

            path.lineTo(right - radius, top);
            rect.set(right - 2f * radius, top, right, top + 2f * radius);
            path.arcTo(rect, 270, 90);

            path.lineTo(right, bottom - radius);
            rect.set(right - 2f * radius, bottom - 2f * radius, right, bottom);
            path.arcTo(rect, 0, 90);

            path.lineTo(left + radius, bottom);
            rect.set(left, bottom - 2f * radius, left + 2f * radius, bottom);
            path.arcTo(rect, 90, 90);

            path.lineTo(left, top + radius);
            rect.set(left, top, left + 2f * radius, top + 2f * radius);
            path.arcTo(rect, 180, 90);

            path.close();

            canvas.drawPath(path, paint);

            indicatorPaint.setColor(indicatorColor);
            float indicatorLeft = (w - indicatorWidthPx) / 2f;
            float indicatorTop = bottom - indicatorBottomInsetPx;
            canvas.drawRoundRect(
                    indicatorLeft,
                    indicatorTop,
                    indicatorLeft + indicatorWidthPx,
                    indicatorTop + indicatorHeightPx,
                    indicatorHeightPx / 2f,
                    indicatorHeightPx / 2f,
                    indicatorPaint
            );
        }
    }
}
