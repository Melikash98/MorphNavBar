package com.melikash98.morphnavbar;

import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents a single tab item.
 * Use either a built-in {@link TabType} or a custom {@link Drawable}.
 */
public final class TabItem {
    @NonNull
    private final TabType type;
    @Nullable
    private final Drawable drawable;
    @Nullable
    private final String contentDescription;

    private TabItem(@NonNull TabType type, @Nullable Drawable drawable, @Nullable String contentDescription) {
        this.type = type;
        this.drawable = drawable;
        this.contentDescription = contentDescription;
    }

    @NonNull
    public static TabItem of(@NonNull TabType type) {
        return new TabItem(type, null, type.name());
    }

    @NonNull
    public static TabItem of(@NonNull TabType type, @Nullable String contentDescription) {
        return new TabItem(type, null, contentDescription);
    }

    @NonNull
    public static TabItem custom(@NonNull Drawable drawable, @Nullable String contentDescription) {
        return new TabItem(TabType.CUSTOM, drawable, contentDescription);
    }

    @NonNull
    public TabType getType() {
        return type;
    }

    @Nullable
    public Drawable getDrawable() {
        return drawable;
    }

    @Nullable
    public String getContentDescription() {
        return contentDescription;
    }
}
