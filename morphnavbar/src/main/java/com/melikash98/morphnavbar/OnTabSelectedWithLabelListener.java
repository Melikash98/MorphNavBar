package com.melikash98.morphnavbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OnTabSelectedWithLabelListener {
    void onTabSelected(int index, @NonNull LiquidTabItem item, @Nullable CharSequence label);
}
