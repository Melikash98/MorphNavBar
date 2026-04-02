package com.melikash98.morphnavbar.Interface;

import androidx.annotation.NonNull;

import com.melikash98.morphnavbar.MorphNavTabItem;

/**
 * Callback for tab selection changes.
 */
public interface OnTabSelectedListener {
    void onTabSelected(int index, @NonNull MorphNavTabItem.Model item);
}
