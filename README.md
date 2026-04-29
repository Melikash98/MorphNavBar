<img src="https://raw.githubusercontent.com/Melikash98/MorphNavBar/main/logo_lib.png" alt="Logo" width="500px"   height="250px" style="margin-right: 10px;padding-top: 6rem;" />

# Editify

[![](https://jitpack.io/v/Melikash98/MorphNavBar.svg)](https://jitpack.io/#Melikash98/MorphNavBar)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**A beautiful, lightweight, fully customizable animated navigation bar with morphing bubble selection, badges, labels, and smooth tab transitions.**


MorphNavBar is a modern Android custom bottom navigation component built for stylish apps that need fluid motion, smooth tab switching, badges, and full XML / Java customization.

---

## ✨ Features

- **Morphing Bar** — The bar’s top edge morphs with a cubic Bézier bump above the active tab.  
- **Elastic Bubble** — The selection bubble stretches up to **2.4×** horizontally and compresses vertically while moving.  
- **Proximity Fade** — Inactive icons fade near the bubble and return after it passes.  
- **Damped Shake** — Each tap triggers a **7.8 Hz** damped shake for **760 ms** on the previous tab.  
- **Badge Tracking** — Badges follow their icon during transition and move with the active state.  
- **Label Blending** — Label colors smoothly blend between inactive and selected states by bubble proximity. 

---

##  Demo

<img src="https://raw.githubusercontent.com/Melikash98/MorphNavBar/main/demo_lib.gif" alt="textLib.gif" width="45%"   height="45%" style="margin-right: 10px;padding-top: 6rem;" />

---

##  Installation

### 1. Add JitPack repository

In your **root** `settings.gradle` (or `settings.gradle.kts`):

```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
### Step 2: Add dependency

```gradle
dependencies {
	        implementation 'com.github.Melikash98:MorphNavBar:v2.2.8'
	}
```
##  Usage

### XML
```xml
<com.melikash98.morphnavbar.MorphNavBar
    android:id="@+id/morphNavBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"

    <!-- Colors -->
    app:lbv_barColor="@android:color/white"                 <!-- Main bar background color -->
    app:lbv_shadowColor="#22000000"                         <!-- Bar shadow color -->
    app:lbv_selectedColor="#00CFC0"                         <!-- Active bubble color -->
    app:lbv_unselectedColor="#00CFC0"                       <!-- Stored unselected accent color -->
    app:lbv_activeIconColor="@android:color/white"          <!-- Selected tab icon tint -->
    app:lbv_inactiveIconColor="#00CFC0"                     <!-- Inactive tab icon tint -->

    <!-- Dimensions -->
    app:lbv_barRadius="26dp"                                <!-- Corner radius of the bar -->
    app:lbv_barHeight="100dp"                               <!-- Total height of the navigation bar -->
    app:lbv_barSideMargin="0dp"                             <!-- Left and right outer margin -->
    app:lbv_barBottomMargin="0dp"                           <!-- Bottom margin of the bar -->
    app:lbv_itemIconSize="34dp"                             <!-- Size of each tab icon -->
    app:lbv_shadowBlur="12dp"                               <!-- Shadow blur radius -->
    app:lbv_shadowDy="4dp"                                  <!-- Shadow vertical offset -->

    <!-- Animation -->
    app:lbv_animationDuration="1400"                        <!-- Selection animation duration in ms -->

    <!-- Labels -->
    app:lbv_showLabels="true"                               <!-- Show labels below tabs -->
    app:lbv_showLabelOnlyOnSelected="false"                 <!-- Show label only for selected tab -->
    app:lbv_labelTextSize="14sp"                            <!-- Label text size -->
    app:lbv_labelFontFamily="sans-serif"                    <!-- Label typeface family -->

    <!-- Badges -->
    app:lbv_badgeBackgroundColor="@android:color/holo_red_light"  <!-- Badge background color -->
    app:lbv_badgeTextColor="@android:color/white"                 <!-- Badge text color -->
    app:lbv_badgeTextSize="11sp"                                  <!-- Badge text size -->
/>
```


---
## 🎯 Java Usage


**Define tabs**
Each tab is a MorphNavTabItem.Model. You can optionally provide a separate drawable for the active (inside-bubble) state:
MorphNavBar navBar = findViewById(R.id.morphNavBar);
```java

// Varargs — set all tabs at once
navBar.setTabs(
    new MorphNavTabItem.Model("Home",    R.drawable.ic_home,    R.drawable.ic_home_filled),
    new MorphNavTabItem.Model("Search",  R.drawable.ic_search,  R.drawable.ic_search_filled),
    new MorphNavTabItem.Model("Inbox",   R.drawable.ic_inbox,   R.drawable.ic_inbox_filled),
    new MorphNavTabItem.Model("Profile", R.drawable.ic_profile, R.drawable.ic_profile_filled)
);
```
Or add tabs incrementally:

```java

navBar.setSelectedIndex(2);           // animated morph transition
navBar.setSelectedIndex(2, false);    // instant jump without animation
navBar.show(0);                       // alias for setSelectedIndex(0)

int current = navBar.getSelectedIndex();
int total   = navBar.getItemCount();

```
Navigate programmatically

```java
navBar.setSelectedIndex(2);           // animated morph transition
navBar.setSelectedIndex(2, false);    // instant jump without animation
navBar.show(0);                       // alias for setSelectedIndex(0)

int current = navBar.getSelectedIndex();
int total   = navBar.getItemCount();

```
Badge counts

```java

navBar.setCount(1, "5");     // string badge on tab at index 1
navBar.setCount(2, 99);      // numeric badge on tab at index 2
navBar.clearCount(1);        // remove badge from tab 1
navBar.clearAllCounts();     // remove all badges

```
Badges automatically track their icon during the morph animation — they follow the bubble from inactiveIconY to activeIconY at the transition midpoint.
---
## 🎨 Attributes


| Attribute | Description |
|----------|------------|
| hintText | Hint text |
| input | Default input text |
| helperText | Helper message |
| warningText | Warning message |
| errorText | Error message |
| hintIcon | Icon shown inside the hint |
| passShow | Show password icon |
| passHide | Hide password icon |
| helperIcon | Helper icon |
| warningIcon | Warning icon |
| errorIcon | Error icon |
| activeBackground | Background when the field is focused/active |
| inactiveBackground | Default (inactive) background |
| hintColor | Default hint color |
| hintActiveColor | Hint color when focused |
| inputColor | Color of text inside the input field |
| helperColor | Helper text and icon color |
| warningColor | Warning text and icon color |
| errorColor | Error text and icon color |
| inputType | Input type (especially useful for passwords) |
| rightDirection | Enable RTL layout (true/false) |
| hintFamily | Font family for hint (@font/ resource or font name) |
| hintSize | Text size for hint |
| inputFamily | Font family for input field |
| inputSize | Text size for input field |
| helperFamily | Font family for helper/warning/error texts |
| helperSize | Text size for helper/warning/error texts |

---

## 🔤 Input Types

```xml
app:inputType="1"      <!-- Normal Text -->
app:inputType="129"    <!-- Password (Text) -->
app:inputType="18"     <!-- Password (Number) -->
app:inputType="145"    <!-- Visible Password -->
```

---

## 📱 RTL Support

```xml
app:rightDirection="true"
```

---

## 📄 License
This project is licensed under the MIT License.

---
## Keywords

android custom edittext, floating hint edittext, material input field android, password toggle edittext, android ui library, custom input view

---
## 👩‍💻 Author

Melikash98


