package com.melikash98.morphnavbar;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A single tab item rendered by {@link MorphNavBar}.
 *
 * The library does not hardcode any icons. Consumers can provide any drawable.
 * A separate selected drawable is optional. If omitted, the base drawable is
 * tinted according to the active/inactive state colors.
 */
public final class LiquidTabItem {

    @NonNull
    private final Drawable icon;
    @Nullable
    private final Drawable selectedIcon;
    @Nullable
    private final CharSequence contentDescription;

    private LiquidTabItem(@NonNull Drawable icon,
                          @Nullable Drawable selectedIcon,
                          @Nullable CharSequence contentDescription) {
        this.icon = icon;
        this.selectedIcon = selectedIcon;
        this.contentDescription = contentDescription;
    }

    @NonNull
    public static LiquidTabItem of(@NonNull Drawable icon, @Nullable CharSequence contentDescription) {
        return new LiquidTabItem(icon, null, contentDescription);
    }

    @NonNull
    public static LiquidTabItem of(@NonNull Drawable icon, @NonNull Drawable selectedIcon,
                                   @Nullable CharSequence contentDescription) {
        return new LiquidTabItem(icon, selectedIcon, contentDescription);
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
}
