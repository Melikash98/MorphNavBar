package com.melikash98.morphnavbar;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class TabButton  extends FrameLayout {
    public final ImageView iconView;

    public TabButton(Context context) {
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
