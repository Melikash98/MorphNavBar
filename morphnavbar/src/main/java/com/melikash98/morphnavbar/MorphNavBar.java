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

    private ValueAnimator selectionAnimator;

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
        bubbleView.setPivotX(bubbleSizePx / 2f);
        bubbleView.setPivotY(bubbleSizePx / 2f);

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
        bubbleIconView.setAlpha(0f);
        bubbleIconView.setScaleX(0.88f);
        bubbleIconView.setScaleY(0.88f);
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
            if (currentBubbleCenterX < 0f) {
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
        bubbleView.setPivotX(px / 2f);
        bubbleView.setPivotY(px / 2f);
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

    private void applySelection(int index, boolean animate, boolean notify) {
        if (items.isEmpty()) return;
        if (index < 0 || index >= items.size()) return;

        int oldIndex = selectedIndex;
        selectedIndex = index;

        MorphTabItem item = items.get(index);

        if (bubbleView.getVisibility() != VISIBLE) {
            bubbleView.setVisibility(VISIBLE);
        }

        if (!animate) {
            cancelAnimator();
            updateTabVisuals();
            bubbleView.setAlpha(1f);
            bubbleView.setScaleX(1f);
            bubbleView.setScaleY(1f);
            bubbleView.setTranslationY(0f);

            bubbleIconView.setImageResource(item.iconRes);
            ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(selectedIconColor));
            bubbleIconView.setAlpha(1f);
            bubbleIconView.setScaleX(1f);
            bubbleIconView.setScaleY(1f);

            syncBubblePosition(false);
            backgroundView.setWaveState(currentBubbleCenterX, 0f, 1);
            backgroundView.invalidate();

            if (notify && listener != null && oldIndex != index) {
                listener.onTabSelected(index, item);
            }
            return;
        }

        if (oldIndex == index && currentBubbleCenterX >= 0f) {
            syncBubblePosition(true);
            return;
        }

        if (oldIndex < 0 || oldIndex >= tabButtons.size()) {
            cancelAnimator();
            updateTabVisuals();
            bubbleView.setVisibility(VISIBLE);
            bubbleIconView.setImageResource(item.iconRes);
            ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(selectedIconColor));
            bubbleIconView.setAlpha(1f);
            bubbleIconView.setScaleX(1f);
            bubbleIconView.setScaleY(1f);
            syncBubblePosition(false);
            backgroundView.setWaveState(currentBubbleCenterX, 0f, 1);
            backgroundView.invalidate();
            if (notify && listener != null) {
                listener.onTabSelected(index, item);
            }
            return;
        }

        animateSelection(oldIndex, index, item, notify);
    }

    private void animateSelection(int oldIndex, int newIndex, MorphTabItem newItem, boolean notify) {
        if (oldIndex < 0 || oldIndex >= tabButtons.size()) return;
        if (newIndex < 0 || newIndex >= tabButtons.size()) return;

        cancelAnimator();
        updateTabVisuals();

        View startView = tabButtons.get(oldIndex);
        View endView = tabButtons.get(newIndex);

        if (startView.getWidth() == 0 || endView.getWidth() == 0) {
            post(() -> animateSelection(oldIndex, newIndex, newItem, notify));
            return;
        }

        final float startX = startView.getLeft() + (startView.getWidth() / 2f);
        final float endX = endView.getLeft() + (endView.getWidth() / 2f);

        if (currentBubbleCenterX < 0f) {
            currentBubbleCenterX = startX;
        }

        bubbleView.setVisibility(VISIBLE);
        bubbleView.setAlpha(1f);

        bubbleIconView.setImageResource(newItem.iconRes);
        ImageViewCompat.setImageTintList(bubbleIconView, ColorStateList.valueOf(selectedIconColor));
        bubbleIconView.setAlpha(0f);
        bubbleIconView.setScaleX(0.84f);
        bubbleIconView.setScaleY(0.84f);

        final int direction = endX >= startX ? 1 : -1;
        final float travelDistance = Math.abs(endX - startX);
        final float maxTravel = Math.max(getWidth(), 1);

        selectionAnimator = ValueAnimator.ofFloat(0f, 1f);
        selectionAnimator.setDuration(animationDuration);
        selectionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        selectionAnimator.addUpdateListener(animation -> {
            float raw = (float) animation.getAnimatedValue();
            float t = easeInOutCubic(raw);
            float bell = 1f - Math.abs(2f * t - 1f);

            currentBubbleCenterX = lerp(startX, endX, t);

            float distanceFactor = clamp(travelDistance / maxTravel, 0f, 1f);
            float stretchX = 1f + (0.20f * bell) + (0.06f * distanceFactor);
            float squashY = 1f - (0.10f * bell) - (0.02f * distanceFactor);

            float lift = -dp(getContext(), 4) * bell;

            bubbleView.setTranslationX(currentBubbleCenterX - (bubbleSizePx / 2f));
            bubbleView.setTranslationY(lift);
            bubbleView.setScaleX(stretchX);
            bubbleView.setScaleY(squashY);

            float iconFadeIn = clamp((t - 0.10f) / 0.45f, 0f, 1f);
            bubbleIconView.setAlpha(iconFadeIn);
            bubbleIconView.setScaleX(0.84f + 0.16f * iconFadeIn);
            bubbleIconView.setScaleY(0.84f + 0.16f * iconFadeIn);

            backgroundView.setWaveState(currentBubbleCenterX, bell, direction);
            backgroundView.invalidate();
        });

        selectionAnimator.start();

        if (notify && listener != null && oldIndex != newIndex) {
            listener.onTabSelected(newIndex, newItem);
        }
    }

    private void updateTabVisuals() {
        for (int i = 0; i < tabButtons.size(); i++) {
            TabButton button = tabButtons.get(i);
            boolean selected = i == selectedIndex;

            button.iconView.animate().cancel();

            button.iconView.animate()
                    .alpha(selected ? 0f : 1f)
                    .scaleX(selected ? 0.78f : 1f)
                    .scaleY(selected ? 0.78f : 1f)
                    .setDuration(180)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();

            ImageViewCompat.setImageTintList(
                    button.iconView,
                    ColorStateList.valueOf(selected ? selectedIconColor : unselectedIconColor)
            );
        }
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
        backgroundView.setWaveState(targetCenterX, 0f, 1);

        if (!animate) {
            bubbleView.setTranslationX(targetCenterX - (bubbleSizePx / 2f));
            bubbleView.setTranslationY(0f);
            bubbleView.setScaleX(1f);
            bubbleView.setScaleY(1f);
        } else {
            bubbleView.animate().translationX(targetCenterX - (bubbleSizePx / 2f)).translationY(0f).setDuration(120).start();
        }
    }

    private void cancelAnimator() {
        if (selectionAnimator != null) {
            selectionAnimator.cancel();
            selectionAnimator = null;
        }
    }

    private float lerp(float start, float end, float t) {
        return start + ((end - start) * t);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float easeInOutCubic(float t) {
        if (t < 0.5f) {
            return 4f * t * t * t;
        }
        float f = (-2f * t) + 2f;
        return 1f - ((f * f * f) / 2f);
    }

    private int dp(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density);
    }

    private class BackgroundView extends View {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Path path = new Path();

        private float waveCenterX = -1f;
        private float waveMorph = 0f;
        private int waveDirection = 1;

        BackgroundView(Context context) {
            super(context);
            paint.setStyle(Paint.Style.FILL);
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        void setWaveState(float x, float morph, int direction) {
            this.waveCenterX = x;
            this.waveMorph = morph;
            this.waveDirection = direction == 0 ? 1 : direction;
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

            if (waveCenterX < 0f) {
                waveCenterX = w / 2f;
            }

            float widthFactor = 0.86f + (0.18f * waveMorph);
            float heightFactor = 0.80f + (0.32f * waveMorph);

            float localWaveWidth = waveWidthPx * widthFactor;
            float localWaveHeight = waveHeightPx * heightFactor;

            float waveStart = clamp(waveCenterX - (localWaveWidth / 2f), radius, w - radius);
            float waveEnd = clamp(waveCenterX + (localWaveWidth / 2f), radius, w - radius);
            float wavePeakY = top - localWaveHeight;

            float skew = waveDirection * localWaveWidth * 0.05f * (0.4f + (waveMorph * 0.6f));

            path.reset();

            path.moveTo(radius, top);
            path.lineTo(waveStart, top);

            path.cubicTo(
                    waveStart + localWaveWidth * 0.16f,
                    top,
                    waveCenterX - localWaveWidth * 0.14f + skew,
                    wavePeakY,
                    waveCenterX,
                    wavePeakY
            );

            path.cubicTo(
                    waveCenterX + localWaveWidth * 0.14f + skew,
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

    private static class TabButton extends FrameLayout {
        final ImageView iconView;

        TabButton(Context context) {
            super(context);
            setClickable(true);
            setFocusable(true);
            setBackground(null);

            iconView = new ImageView(context);
            iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            LayoutParams lp = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
            );
            addView(iconView, lp);
        }
    }
}
