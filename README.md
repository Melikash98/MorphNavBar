<img src="https://raw.githubusercontent.com/Melikash98/MorphNavBar/main/logo_lib.png" alt="Logo" width="500px"   height="250px" style="margin-right: 10px;padding-top: 6rem;" />

# Editify

[![](https://jitpack.io/v/Melikash98/MorphNavBar.svg)](https://jitpack.io/#Melikash98/MorphNavBar)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**A beautiful, lightweight, fully customizable animated navigation bar with morphing bubble selection, badges, labels, and smooth tab transitions.**


MorphNavBar is a modern Android custom bottom navigation component built for stylish apps that need fluid motion, smooth tab switching, badges, and full XML / Java customization.

---

## ✨ Features

- **Morphing bar animation** with dynamic cubic Bézier top bump synced to active tab movement  
- **Elastic selection bubble** with horizontal stretch and smooth rebound transition  
- **Proximity icon fading** as the bubble approaches and passes each tab  
- **Damped shake feedback** on tap and reselect interactions with spring-like motion  
- **Smart badge tracking** that follows icons during animated tab transitions  
- **Smooth label color blending** between inactive and selected states  
- **Highly customizable** colors, sizes, labels, badges, shadows, and timings  
- **Rich listener support** for click, select, reselect, and show events  
- **Works with both Kotlin and Java** projects  
- **Zero heavy dependencies** – pure AndroidX custom View implementation  
- **JitPack ready** for fast and simple integration  

---

## 📺 Demo

<img src="https://raw.githubusercontent.com/Melikash98/MorphNavBar/main/demo_lib.gif" alt="textLib.gif" width="45%"   height="45%" style="margin-right: 10px;padding-top: 6rem;" />

---

## 📦 Installation

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
     implementation 'com.github.Melikash98:Editify:v1.4.8'
}
```
## 🛠️ Usage

### XML
```xml
<com.melikash98.editify.CustomInputEdit
    android:id="@+id/myCustomInput"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"

    <!-- Hint configuration -->
    app:hintText="Username"                    <!-- Main hint text -->
    app:hintIcon="@drawable/ic_user"          <!-- Icon inside hint -->
    app:hintColor="@color/gray"               <!-- Default hint color -->
    app:hintActiveColor="@color/green"        <!-- Color when focused/active -->
    app:hintBackgroundColor="@color/white"    <!-- Background of the floating hint -->

    <!-- Input text styling -->
    app:inputColor="@color/black"             <!-- Text color inside the input field -->
    app:textColor="@color/black"              <!-- Fallback text color -->

    <!-- Font & Size Configuration -->
    app:hintFamily="@font/vazirmatn_medium"   <!-- Hint font (supports @font/ or font family name) -->
    app:hintSize="16sp"                       <!-- Hint text size -->

    app:inputFamily="@font/vazirmatn"         <!-- Input field font -->
    app:inputSize="17sp"                      <!-- Input text size -->

    app:helperFamily="@font/vazirmatn_light"  <!-- Helper/Warning/Error font -->
    app:helperSize="13.5sp"                   <!-- Helper/Warning/Error text size -->

    <!-- Background states -->
    app:activeBackground="@drawable/input_active"     <!-- Background when focused -->
    app:inactiveBackground="@drawable/input_inactive" <!-- Default background -->

    <!-- Helper / Warning / Error messages -->
    app:helperText="Enter your username"      <!-- Helper message -->
    app:warningText="Please check your input" <!-- Warning message -->
    app:errorText="This field is required"    <!-- Error message -->

    <!-- Password toggle icons -->
    app:passShow="@drawable/ic_show"          <!-- Icon when password is visible -->
    app:passHide="@drawable/ic_hide"          <!-- Icon when password is hidden -->

    <!-- Layout direction -->
    app:rightDirection="false"                <!-- Set true for RTL languages -->

    <!-- Input Type (especially for passwords) -->
    <!-- app:inputType="129" -->              <!-- Text Password (hidden) -->
    <!-- app:inputType="18"  -->              <!-- Number Password -->
    <!-- app:inputType="1"   -->              <!-- Normal text (default) -->
    />
```


---
## 🎯 Java Usage

```java
// Get reference to the custom input view
CustomInputEdit input = findViewById(R.id.myCustomInput);

// Get current text value (trimmed)
String text = input.getText();

// Set text programmatically
input.setText("Hello");

// Show helper message (green state)
input.setHelperText("Helper message");

// Show warning message (yellow state)
input.setWarningText("Warning message");

// Show error message (red state)
input.setErrorText("Error message");
```

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


