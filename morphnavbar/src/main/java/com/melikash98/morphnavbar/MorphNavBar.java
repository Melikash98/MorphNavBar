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
    private FrameLayout bubbleView;
    private ImageView bubbleIconView;

    private OnTabSelectedListener listener;

    private int selectedIndex = 0;

    private int barColor = Color.WHITE;
    private int bubbleColor = Color.parseColor("#11CFCB");
    private int selectedIconColor = Color.WHITE;
    private int unselectedIconColor = Color.parseColor("#11CFCB");

    private int barHeightPx;
    private int barCornerRadiusPx;
    private int bubbleSizePx;
    private int bubbleOverlapPx;
    private int iconSizePx;
    private int waveHeightPx;
    private int waveWidthPx;
    private int animationDuration = 340;
    private int shadowRadiusPx;
    private int shadowAlpha = 24;

    private float currentBubbleCenterX = -1f;
    private float waveMorphFraction = 1f;
    private int waveDirection = 1;

    private boolean firstLayoutDone = false;
    private ValueAnimator bubbleMoveAnimator;
    private ObjectAnimator waveMorphAnimator;

    public MorphNavBar(Context context) {
        this(context, null);
    }

    public MorphNavBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MorphNavBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults(context);
        readAttrs(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initDefaults(Context context) {
        barHeightPx = dp(context, 88);
        barCornerRadiusPx = dp(context, 28);
        bubbleSizePx = dp(context, 72);
        bubbleOverlapPx = dp(context, 26);
        iconSizePx = dp(context, 26);
        waveHeightPx = dp(context, 16);
        waveWidthPx = dp(context, 112);
        shadowRadiusPx = dp(context, 14);
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
        LayoutParams bgLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(backgroundView, bgLp);

        itemsContainer = new LinearLayout(context);
        itemsContainer.setOrientation(LinearLayout.HORIZONTAL);
        itemsContainer.setGravity(Gravity.CENTER_VERTICAL);
        itemsContainer.setClipChildren(false);
        itemsContainer.setClipToPadding(false);

        LayoutParams containerLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(itemsContainer, containerLp);

        bubbleView = new FrameLayout(context);
        bubbleView.setClipChildren(false);
        bubbleView.setClipToPadding(false);

        GradientDrawable bubbleDrawable = new GradientDrawable();
        bubbleDrawable.setShape(GradientDrawable.OVAL);
        bubbleDrawable.setColor(bubbleColor);
        bubbleView.setBackground(bubbleDrawable);
        bubbleView.setElevation(dp(context, 8));

        LayoutParams bubbleLp = new LayoutParams(bubbleSizePx, bubbleSizePx);
        bubbleLp.gravity = Gravity.TOP | Gravity.START;
        addView(bubbleView, bubbleLp);

        bubbleIconView = new ImageView(context);
        bubbleIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(selectedIconColor));

        FrameLayout.LayoutParams bubbleIconLp = new FrameLayout.LayoutParams(iconSizePx, iconSizePx, Gravity.CENTER);
        bubbleView.addView(bubbleIconView, bubbleIconLp);

        bubbleView.setVisibility(INVISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            int desiredHeight = barHeightPx + bubbleOverlapPx;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        backgroundView.layout(0, 0, width, height);

        int itemsTop = bubbleOverlapPx;
        int itemsBottom = Math.min(height, bubbleOverlapPx + barHeightPx);
        itemsContainer.layout(0, itemsTop, width, itemsBottom);

        bubbleView.layout(0, 0, bubbleSizePx, bubbleSizePx);

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
            final MorphTabItem item = items.get(i);

            TabButton button = new TabButton(getContext());
            button.iconView.setImageResource(item.iconRes);
            ImageViewCompat.setImageTintList(button.iconView, ColorStateList.valueOf(unselectedIconColor));

            button.setOnClickListener(v -> applySelection(index, true, true));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            itemsContainer.addView(button, lp);
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
        applySelection(index, true, true);
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

    public void setBubbleSizePx(int px) {
        this.bubbleSizePx = px;
        ViewGroup.LayoutParams lp = bubbleView.getLayoutParams();
        lp.width = px;
        lp.height = px;
        bubbleView.setLayoutParams(lp);
        requestLayout();
    }

    public void setBubbleOverlapPx(int px) {
        this.bubbleOverlapPx = px;
        requestLayout();
    }

    public void setIconSizePx(int px) {
        this.iconSizePx = px;

        ViewGroup.LayoutParams bubbleIconLp = bubbleIconView.getLayoutParams();
        bubbleIconLp.width = px;
        bubbleIconLp.height = px;
        bubbleIconView.setLayoutParams(bubbleIconLp);

        for (TabButton button : tabButtons) {
            ViewGroup.LayoutParams lp = button.iconView.getLayoutParams();
            lp.width = px;
            lp.height = px;
            button.iconView.setLayoutParams(lp);
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

        MorphTabItem item = items.get(index);

        if (!animate) {
            bubbleIconView.setAlpha(1f);
            bubbleIconView.setImageResource(item.iconRes);
            ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(selectedIconColor));
            syncBubblePosition(false);
        } else {
            animateBubbleToIndex(index);
            animateBubbleIconChange(item.iconRes);
            startWaveMorphAnimation();
        }

        if (notify && listener != null && oldIndex != index) {
            listener.onTabSelected(index, item);
        }
    }

    private void updateTabVisuals() {
        for (int i = 0; i < tabButtons.size(); i++) {
            TabButton button = tabButtons.get(i);
            boolean selected = i == selectedIndex;

            float alpha = selected ? 0f : 1f;
            float scale = selected ? 0.80f : 1.0f;

            button.iconView.animate()
                    .alpha(alpha)
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(180)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            ImageViewCompat.setImageTintList(
                    button.iconView,
                    ColorStateList.valueOf(selected ? selectedIconColor : unselectedIconColor)
            );
        }
    }

    private void animateBubbleIconChange(int newIconRes) {
        bubbleIconView.animate()
                .alpha(0f)
                .setDuration(animationDuration / 2L)
                .withEndAction(() -> {
                    bubbleIconView.setImageResource(newIconRes);
                    ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(selectedIconColor));
                    bubbleIconView.animate()
                            .alpha(1f)
                            .setDuration(animationDuration / 2L)
                            .start();
                })
                .start();
    }

    private void animateBubbleToIndex(int index) {
        if (index < 0 || index >= tabButtons.size()) return;

        View target = tabButtons.get(index);
        if (target.getWidth() == 0) {
            post(() -> animateBubbleToIndex(index));
            return;
        }

        float targetCenterX = target.getLeft() + (target.getWidth() / 2f);

        if (currentBubbleCenterX < 0) {
            currentBubbleCenterX = targetCenterX;
            bubbleView.setTranslationX(targetCenterX - (bubbleSizePx / 2f));
            backgroundView.setWaveCenterX(targetCenterX);
            return;
        }

        if (bubbleMoveAnimator != null) {
            bubbleMoveAnimator.cancel();
        }

        bubbleMoveAnimator = ValueAnimator.ofFloat(currentBubbleCenterX, targetCenterX);
        bubbleMoveAnimator.setDuration(animationDuration);
        bubbleMoveAnimator.setInterpolator(new OvershootInterpolator(0.70f));

        bubbleMoveAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            currentBubbleCenterX = value;

            bubbleView.setTranslationX(value - (bubbleSizePx / 2f));
            backgroundView.setWaveCenterX(value);
            backgroundView.invalidate();
        });

        bubbleMoveAnimator.start();

        bubbleView.animate()
                .scaleX(1.04f)
                .scaleY(1.04f)
                .setDuration(animationDuration / 2L)
                .withEndAction(() -> bubbleView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(animationDuration / 2L)
                        .start())
                .start();
    }

    private void startWaveMorphAnimation() {
        if (waveMorphAnimator != null) {
            waveMorphAnimator.cancel();
        }

        PropertyValuesHolder holder = PropertyValuesHolder.ofKeyframe(
                "waveMorphFraction",
                Keyframe.ofFloat(0f, 0.82f),
                Keyframe.ofFloat(0.25f, 1.26f),
                Keyframe.ofFloat(0.60f, 0.98f),
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

        float targetCenterX = target.getLeft() + (target.getWidth() / 2f);
        currentBubbleCenterX = targetCenterX;
        backgroundView.setWaveCenterX(targetCenterX);

        if (!animate) {
            bubbleView.setTranslationX(targetCenterX - (bubbleSizePx / 2f));
        }
    }

    private int dp(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    private class BackgroundView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Path path = new Path();

        private float waveCenterX = -1f;

        BackgroundView(Context context) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        void setWaveCenterX(float x) {
            this.waveCenterX = x;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            paint.setColor(barColor);
            paint.setShadowLayer(
                    shadowRadiusPx,
                    0f,
                    dp(getContext(), 4),
                    Color.argb(shadowAlpha, 0, 0, 0)
            );

            float w = getWidth();
            float top = bubbleOverlapPx;
            float bottom = top + barHeightPx;
            float radius = barCornerRadiusPx;

            if (waveCenterX < 0) {
                waveCenterX = w / 2f;
            }

            float waveWidthScale = 0.86f + (0.18f * waveMorphFraction);
            float waveHeightScale = 0.78f + (0.30f * waveMorphFraction);

            float localWaveWidth = waveWidthPx * waveWidthScale;
            float localWaveHeight = waveHeightPx * waveHeightScale;

            float waveStart = Math.max(radius, waveCenterX - (localWaveWidth / 2f));
            float waveEnd = Math.min(w - radius, waveCenterX + (localWaveWidth / 2f));
            float wavePeakY = top - localWaveHeight;

            float directionSkew = waveDirection * localWaveWidth * 0.05f * (1f - Math.abs(waveMorphFraction - 0.5f) * 2f);

            path.reset();

            path.moveTo(radius, top);
            path.lineTo(waveStart, top);

            path.cubicTo(
                    waveStart + localWaveWidth * 0.16f,
                    top,
                    waveCenterX - localWaveWidth * 0.14f + directionSkew,
                    wavePeakY,
                    waveCenterX,
                    wavePeakY
            );

            path.cubicTo(
                    waveCenterX + localWaveWidth * 0.14f + directionSkew,
                    wavePeakY,
                    waveEnd - localWaveWidth * 0.16f,
                    top,
                    waveEnd,
                    top
            );

            path.lineTo(w - radius, top);

            rect.set(w - 2f * radius, top, w, top + 2f * radius);
            path.arcTo(rect, 270, 90);

            path.lineTo(w, bottom - radius);
            rect.set(w - 2f * radius, bottom - 2f * radius, w, bottom);
            path.arcTo(rect, 0, 90);

            path.lineTo(radius, bottom);
            rect.set(0, bottom - 2f * radius, 2f * radius, bottom);
            path.arcTo(rect, 90, 90);

            path.lineTo(0, top + radius);
            rect.set(0, top, 2f * radius, top + 2f * radius);
            path.arcTo(rect, 180, 90);

            path.close();

            canvas.drawPath(path, paint);
        }
    }
}
