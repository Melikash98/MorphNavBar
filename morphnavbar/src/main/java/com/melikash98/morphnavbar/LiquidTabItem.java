package com.melikash98.morphnavbar;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class LiquidTabItem {
    private final Drawable icon;
    @Nullable
    private final Drawable selectedIcon;
    @Nullable
    private final CharSequence contentDescription;
    @Nullable
    private final CharSequence label;

    protected LiquidTabItem(@NonNull Drawable icon,
                            @Nullable Drawable selectedIcon,
                            @Nullable CharSequence contentDescription,
                            @Nullable CharSequence label) {
        this.icon = icon;
        this.selectedIcon = selectedIcon;
        this.contentDescription = contentDescription;
        this.label = label;
    }

    @NonNull
    public static LiquidTabItem of(@NonNull Drawable icon, @Nullable CharSequence contentDescription) {
        return new LiquidTabItem(icon, null, contentDescription, null);
    }

    @NonNull
    public static LiquidTabItem of(@NonNull Drawable icon, @NonNull Drawable selectedIcon,
                                   @Nullable CharSequence contentDescription) {
        return new LiquidTabItem(icon, selectedIcon, contentDescription, null);
    }

    @NonNull
    public static LiquidTabItem of(@NonNull Drawable icon, @Nullable CharSequence contentDescription,
                                   @Nullable CharSequence label) {
        return new LiquidTabItem(icon, null, contentDescription, label);
    }

    @NonNull
    public static LiquidTabItem of(@NonNull Drawable icon, @NonNull Drawable selectedIcon,
                                   @Nullable CharSequence contentDescription, @Nullable CharSequence label) {
        return new LiquidTabItem(icon, selectedIcon, contentDescription, label);
    }

    @NonNull
    public Drawable getIcon() {
        return icon;
    }

    @Nullable
    public Drawable getSelectedIcon() {
        return selectedIcon;
    }

    @Nullable
    public CharSequence getContentDescription() {
        return contentDescription;
    }

    @Nullable
    public CharSequence getLabel() {
        return label;
    }
}