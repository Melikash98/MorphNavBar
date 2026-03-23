package com.melikash98.morphnavbar;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class MorphTabItem {
    @DrawableRes
    public final int iconRes;
    @NonNull
    public final int id;

    public MorphTabItem(@DrawableRes int iconRes, @NonNull int id) {
        this.iconRes = iconRes;
        this.id = id;
    }
}
