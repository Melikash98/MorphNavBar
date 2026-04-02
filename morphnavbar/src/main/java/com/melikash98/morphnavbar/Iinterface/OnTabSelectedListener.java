package com.melikash98.morphnavbar.Iinterface;

import androidx.annotation.NonNull;

import com.melikash98.morphnavbar.LiquidTabItem;

/**
 * Callback for tab selection changes.
 */
public interface OnTabSelectedListener {
    void onTabSelected(int index, @NonNull LiquidTabItem.Model item);
}
