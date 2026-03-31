package com.melikash98.morphnavbar;

import androidx.annotation.NonNull;

/**
 * Callback for tab selection changes.
 */
public interface OnTabSelectedListener {
    void onTabSelected(int index, @NonNull LiquidTabItem.Model item);
}
