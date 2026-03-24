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
import java.util.List;

/**
 * Floating liquid bottom navigation inspired by the supplied video.
 * <p>
 * The component is intentionally self-contained: it draws the bar, the active bubble,
 * the center indicator line, and the built-in outline icons directly on canvas.
 */
public class MorphNavBar extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 520;

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF barRect = new RectF();
    private final RectF bubbleRect = new RectF();
    private final RectF tempRect = new RectF();
    private final Path tempPath = new Path();

    private final List<TabItem> items = new ArrayList<>();
    private final List<Float> itemCenters = new ArrayList<>();

    private final FastOutSlowInInterpolator interpolator = new FastOutSlowInInterpolator();
    private final DecelerateInterpolator iconInterpolator = new DecelerateInterpolator();

    private int selectedIndex = 0;
    private int animStartIndex = 0;
    private int animEndIndex = 0;
    private float animFraction = 1f;
    private ValueAnimator animator;
    private OnTabSelectedListener listener;

    private int barColor;
    private int selectedColor;
    private int unselectedColor;
    private int shadowColor;
    private int indicatorColor;

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

    private float computedWidth;
    private float computedHeight;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void initDefaults() {
        barColor = Color.WHITE;
        selectedColor = Color.parseColor("#00D1C0");
        unselectedColor = Color.parseColor("#00D1C0");
        shadowColor = Color.parseColor("#22000000");
        indicatorColor = Color.parseColor("#E9E9E9");

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
        animationDuration = DEFAULT_ANIMATION_DURATION;
    }

    private void readAttributes(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            return;
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LiquidBottomNavigationView, defStyleAttr, 0);
        try {
            barColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_barColor, barColor);
            selectedColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_selectedColor, selectedColor);
            unselectedColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_unselectedColor, unselectedColor);
            shadowColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_shadowColor, shadowColor);
            indicatorColor = a.getColor(R.styleable.LiquidBottomNavigationView_lbv_indicatorColor, indicatorColor);

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

        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);
        iconPaint.setStrokeWidth(dp(1.9f));
        iconPaint.setColor(unselectedColor);

        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(indicatorColor);

        tempPaint.setStyle(Paint.Style.FILL);
        tempPaint.setAntiAlias(true);
    }

    public void setOnTabSelectedListener(@Nullable OnTabSelectedListener listener) {
        this.listener = listener;
    }

    public void setTabs(@NonNull List<TabItem> tabItems) {
        items.clear();
        items.addAll(tabItems);
        if (selectedIndex >= items.size()) {
            selectedIndex = 0;
        }
        if (animEndIndex >= items.size()) {
            animEndIndex = selectedIndex;
        }
        requestLayout();
        invalidate();
    }

    public void setTabs(@NonNull TabItem... tabItems) {
        items.clear();
        for (TabItem item : tabItems) {
            items.add(item);
        }
        if (selectedIndex >= items.size()) {
            selectedIndex = 0;
        }
        if (animEndIndex >= items.size()) {
            animEndIndex = selectedIndex;
        }
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
        if (items.isEmpty() || index < 0 || index >= items.size() || index == selectedIndex) {
            return;
        }
        if (animator != null) {
            animator.cancel();
        }

        if (!animate || itemCenters.isEmpty()) {
            selectedIndex = index;
            animStartIndex = index;
            animEndIndex = index;
            animFraction = 1f;
            notifySelection(index);
            invalidate();
            return;
        }

        animStartIndex = selectedIndex;
        animEndIndex = index;
        animFraction = 0f;

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(animation -> {
            animFraction = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                selectedIndex = animEndIndex;
                animStartIndex = selectedIndex;
                animFraction = 1f;
                notifySelection(selectedIndex);
                invalidate();
            }
        });
        animator.start();
    }

    private void notifySelection(int index) {
        if (listener != null && index >= 0 && index < items.size()) {
            listener.onTabSelected(index, items.get(index));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int desiredWidth = (int) Math.ceil(getPaddingLeft() + getPaddingRight() + 360); // used only if wrap_content
        int width = resolveSize(desiredWidth, widthMeasureSpec);

        int desiredHeight = (int) Math.ceil(getPaddingTop() + getPaddingBottom() + barHeight + bubbleDiameter * 0.55f + barBottomMargin);
        int height = resolveSize(desiredHeight, heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        computedWidth = w;
        computedHeight = h;

        float left = getPaddingLeft() + barSideMargin;
        float right = w - getPaddingRight() - barSideMargin;
        float bottom = h - getPaddingBottom() - barBottomMargin;
        float top = bottom - barHeight;
        barRect.set(left, top, right, bottom);

        bubbleCenterY = top + bubbleDiameter * 0.52f;

        itemCenters.clear();
        if (!items.isEmpty()) {
            float seg = barRect.width() / items.size();
            for (int i = 0; i < items.size(); i++) {
                itemCenters.add(barRect.left + seg * (i + 0.5f));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (items.isEmpty()) {
            return;
        }

        drawShadow(canvas);
        drawBar(canvas);
        drawIndicatorLine(canvas);
        drawInactiveIcons(canvas);
        drawBubble(canvas);
        drawSelectedIcon(canvas);
    }

    private void drawShadow(Canvas canvas) {
        float blur = shadowBlur;
        float dy = shadowDy;
        tempPaint.setColor(shadowColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            tempPaint.setShadowLayer(blur, 0f, dy, shadowColor);
        } else {
            tempPaint.setShadowLayer(blur, 0f, dy, shadowColor);
        }
        canvas.drawRoundRect(barRect, barRadius, barRadius, tempPaint);
        tempPaint.clearShadowLayer();
    }

    private void drawBar(Canvas canvas) {
        barPaint.setColor(barColor);
        canvas.drawRoundRect(barRect, barRadius, barRadius, barPaint);
    }

    private void drawIndicatorLine(Canvas canvas) {
        float cx = barRect.centerX();
        float cy = barRect.bottom - dp(34);
        float left = cx - indicatorWidth / 2f;
        float top = cy - indicatorHeight / 2f;
        tempRect.set(left, top, left + indicatorWidth, top + indicatorHeight);
        canvas.drawRoundRect(tempRect, indicatorHeight, indicatorHeight, indicatorPaint);
    }

    private void drawBubble(Canvas canvas) {
        float t = animFraction;
        if (animStartIndex == animEndIndex) {
            t = 1f;
        }
        float eased = interpolator.getInterpolation(t);

        float startX = getCenterX(animStartIndex);
        float endX = getCenterX(animEndIndex);
        float currentX = lerp(startX, endX, eased);

        float r = bubbleDiameter / 2f;
        float horizontalDistance = Math.abs(endX - startX);

        bubblePaint.setColor(selectedColor);

        tempPath.reset();

        if (animStartIndex == animEndIndex || horizontalDistance < r * 0.75f) {
            tempPath.addCircle(currentX, bubbleCenterY, r, Path.Direction.CW);
        } else {
            // A two-circle union produces a convincing liquid travel effect
            // similar to the supplied video, while staying inexpensive to draw.
            float trail = 1f - eased;
            float lead = eased;

            float startRadius = r * (0.98f - 0.22f * eased);
            float endRadius = r * (0.78f + 0.18f * eased);

            float startCenter = lerp(startX, currentX, Math.min(1f, eased * 0.85f));
            float endCenter = lerp(currentX, endX, Math.min(1f, 0.30f + eased * 0.70f));

            tempPath.addCircle(startCenter, bubbleCenterY, startRadius, Path.Direction.CW);
            Path second = new Path();
            second.addCircle(endCenter, bubbleCenterY, endRadius, Path.Direction.CW);
            tempPath.addPath(second);
        }

        canvas.drawPath(tempPath, bubblePaint);

        // Adds a tiny "crest" at the top during motion, matching the video's soft wave.
        if (animStartIndex != animEndIndex) {
            float crest = (float) Math.sin(Math.PI * eased);
            float crestRadius = bubbleDiameter * (0.14f + 0.10f * crest);
            float crestX = lerp(startX, endX, eased * eased);
            canvas.drawCircle(crestX, barRect.top + crestRadius * 0.4f, crestRadius, bubblePaint);
        }
    }

    private void drawInactiveIcons(Canvas canvas) {
        for (int i = 0; i < items.size(); i++) {
            if (i == selectedIndex && animFraction >= 1f) {
                continue;
            }
            if (animStartIndex != animEndIndex && (i == animStartIndex || i == animEndIndex)) {
                // Leave them visible; the bubble will cover the outgoing icon and the target
                // icon while the move is in progress, just like the video.
            }
            drawIconForIndex(canvas, i, unselectedColor, 1f, 0f);
        }
    }

    private void drawSelectedIcon(Canvas canvas) {
        if (items.isEmpty()) {
            return;
        }

        float t = animFraction;
        if (animStartIndex == animEndIndex) {
            drawIconForIndex(canvas, selectedIndex, Color.WHITE, 1.0f, 1f);
            return;
        }

        float eased = interpolator.getInterpolation(t);
        float iconScale = 1.0f + 0.08f * (float) Math.sin(Math.PI * eased);
        int iconIndex = eased < 0.52f ? animStartIndex : animEndIndex;
        drawIconForIndex(canvas, iconIndex, Color.WHITE, iconScale, 1f);
    }

    private void drawIconForIndex(Canvas canvas, int index, @ColorInt int color, float scale, float alpha) {
        if (index < 0 || index >= items.size()) {
            return;
        }
        TabItem item = items.get(index);
        float cx = getCenterX(index);
        float cy = bubbleCenterY;

        Drawable drawable = item.getDrawable();
        if (drawable != null) {
            drawDrawableIcon(canvas, drawable, cx, cy, color, scale, alpha);
            return;
        }

        iconPaint.setColor(color);
        iconPaint.setAlpha((int) (255f * clamp(alpha, 0f, 1f)));
        iconPaint.setStrokeWidth(dp(1.9f));

        canvas.save();
        canvas.translate(cx, cy);
        canvas.scale(scale, scale);
        switch (item.getType()) {
            case LOCK:
                drawLockIcon(canvas, iconPaint);
                break;
            case BELL:
                drawBellIcon(canvas, iconPaint);
                break;
            case TRASH:
                drawTrashIcon(canvas, iconPaint);
                break;
            case BAG:
                drawBagIcon(canvas, iconPaint);
                break;
        }
        canvas.restore();
    }

    private void drawDrawableIcon(Canvas canvas, Drawable drawable, float cx, float cy, @ColorInt int color, float scale, float alpha) {
        Drawable d = drawable.mutate();
        d.setTint(color);
        int size = Math.round(itemIconSize * scale);
        int half = size / 2;
        d.setBounds(Math.round(cx) - half, Math.round(cy) - half, Math.round(cx) + half, Math.round(cy) + half);
        int oldAlpha = d.getAlpha();
        d.setAlpha((int) (255f * clamp(alpha, 0f, 1f)));
        d.draw(canvas);
        d.setAlpha(oldAlpha);
    }

    private void drawLockIcon(Canvas canvas, Paint paint) {
        // Body
        canvas.drawRoundRect(6.5f, 11.0f, 17.5f, 20.0f, 2.2f, 2.2f, paint);
        // Shackle
        canvas.drawLine(9.0f, 11.0f, 9.0f, 7.6f, paint);
        canvas.drawArc(new RectF(9.0f, 3.9f, 15.0f, 9.9f), 180f, -180f, false, paint);
        canvas.drawLine(15.0f, 7.6f, 15.0f, 11.0f, paint);
        // Keyhole
        canvas.drawLine(12.0f, 14.7f, 12.0f, 16.6f, paint);
    }

    private void drawBellIcon(Canvas canvas, Paint paint) {
        // Dome
        canvas.drawArc(new RectF(6.0f, 5.8f, 18.0f, 17.7f), 200f, 140f, false, paint);
        // Stem
        canvas.drawLine(12.0f, 4.8f, 12.0f, 6.2f, paint);
        // Body sides to clapper level
        canvas.drawLine(6.9f, 11.9f, 6.9f, 14.3f, paint);
        canvas.drawLine(17.1f, 11.9f, 17.1f, 14.3f, paint);
        // Clapper base
        canvas.drawLine(6.0f, 14.4f, 18.0f, 14.4f, paint);
        // Clapper
        canvas.drawArc(new RectF(10.2f, 14.0f, 13.8f, 17.6f), 0f, 180f, false, paint);
    }

    private void drawTrashIcon(Canvas canvas, Paint paint) {
        // Lid
        canvas.drawLine(6.5f, 7.6f, 17.5f, 7.6f, paint);
        canvas.drawLine(8.5f, 6.0f, 15.5f, 6.0f, paint);
        canvas.drawLine(9.2f, 6.0f, 10.2f, 7.6f, paint);
        canvas.drawLine(13.8f, 6.0f, 14.8f, 7.6f, paint);
        // Body
        canvas.drawRoundRect(7.7f, 8.0f, 16.3f, 20.2f, 1.7f, 1.7f, paint);
        // Inner stripes
        canvas.drawLine(11.1f, 10.5f, 11.1f, 18.0f, paint);
        canvas.drawLine(13.7f, 10.5f, 13.7f, 18.0f, paint);
    }

    private void drawBagIcon(Canvas canvas, Paint paint) {
        // Handle
        canvas.drawArc(new RectF(8.2f, 4.8f, 15.8f, 10.6f), 200f, 140f, false, paint);
        // Body
        canvas.drawRoundRect(6.4f, 8.8f, 17.6f, 20.4f, 2.0f, 2.0f, paint);
        // Neck opening curve
        canvas.drawLine(8.1f, 8.8f, 15.9f, 8.8f, paint);
        // Tiny center mark similar to the reference icon
        canvas.drawArc(new RectF(10.8f, 10.0f, 13.2f, 12.4f), 0f, 180f, false, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (items.isEmpty()) {
            return super.onTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                int index = hitTest(event.getX(), event.getY());
                if (index != -1 && index != selectedIndex) {
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
        if (itemCenters.isEmpty()) {
            return -1;
        }
        float top = barRect.top - bubbleDiameter * 0.18f;
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

    private float getCenterX(int index) {
        if (itemCenters.isEmpty()) {
            return barRect.centerX();
        }
        index = Math.max(0, Math.min(index, itemCenters.size() - 1));
        return itemCenters.get(index);
    }

    private void cancelAnimator() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelAnimator();
        super.onDetachedFromWindow();
    }

    public void setBarColor(@ColorInt int color) {
        barColor = color;
        invalidate();
    }

    public void setSelectedColor(@ColorInt int color) {
        selectedColor = color;
        bubblePaint.setColor(color);
        invalidate();
    }

    public void setUnselectedColor(@ColorInt int color) {
        unselectedColor = color;
        iconPaint.setColor(color);
        invalidate();
    }

    public void setIndicatorColor(@ColorInt int color) {
        indicatorColor = color;
        indicatorPaint.setColor(color);
        invalidate();
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = Math.max(1, animationDuration);
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
