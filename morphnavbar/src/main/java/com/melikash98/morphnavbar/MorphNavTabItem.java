package com.melikash98.morphnavbar;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public final class MorphNavTabItem {
    public MorphNavTabItem() {
    }

    public static final class Model {
        @NonNull
        private final CharSequence label;

        @DrawableRes
        private final int iconResId;

        @DrawableRes
        private final int selectedIconResId;

        @Nullable
        private final CharSequence contentDescription;

        public Model(@NonNull CharSequence label, @DrawableRes int iconResId) {
            this(label, iconResId, 0, null);
        }

        public Model(@NonNull CharSequence label,
                     @DrawableRes int iconResId,
                     @DrawableRes int selectedIconResId) {
            this(label, iconResId, selectedIconResId, null);
        }

        public Model(@NonNull CharSequence label,
                     @DrawableRes int iconResId,
                     @DrawableRes int selectedIconResId,
                     @Nullable CharSequence contentDescription) {
            this.label = label;
            this.iconResId = iconResId;
            this.selectedIconResId = selectedIconResId;
            this.contentDescription = contentDescription;
        }

        @NonNull
        public CharSequence getLabel() {
            return label;
        }

        public int getIconResId() {
            return iconResId;
        }

        public int getSelectedIconResId() {
            return selectedIconResId;
        }

        @Nullable
        public CharSequence getContentDescription() {
            return contentDescription;
        }
    }
}