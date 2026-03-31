package com.melikash98.morphnavbar;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class LabeledLiquidTabItem extends LiquidTabItem {
    private LabeledLiquidTabItem(@NonNull Drawable icon, @Nullable Drawable selectedIcon, @Nullable CharSequence contentDescription, @NonNull CharSequence label) {
        super(icon, selectedIcon, contentDescription, label);
    }

    @NonNull
    public static LabeledLiquidTabItem of(@NonNull Drawable icon, @NonNull CharSequence label) {
        return new LabeledLiquidTabItem(icon, null, null, label);
    }

    @NonNull
    public static LabeledLiquidTabItem of(@NonNull Drawable icon, @NonNull Drawable selectedIcon, @NonNull CharSequence label) {
        return new LabeledLiquidTabItem(icon, selectedIcon, null, label);
    }

    @NonNull
    public static LabeledLiquidTabItem of(@NonNull Drawable icon, @Nullable CharSequence contentDescription, @NonNull CharSequence label) {
        return new LabeledLiquidTabItem(icon, null, contentDescription, label);
    }

    @NonNull
    public static LabeledLiquidTabItem of(@NonNull Drawable icon, @NonNull Drawable selectedIcon, @Nullable CharSequence contentDescription, @NonNull CharSequence label) {
        return new LabeledLiquidTabItem(icon, selectedIcon, contentDescription, label);
    }
}
