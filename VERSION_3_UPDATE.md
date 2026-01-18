# Version 3.0 Update

## ✅ Changes Made

### Version Information
- **Version Code:** Updated from 2 to **3**
- **Version Name:** Updated from "2.0" to **"3.0"**

### Build Configuration
- **R8/ProGuard:** Enabled (`isMinifyEnabled = true`)
- **Resource Shrinking:** Enabled (`isShrinkResources = true`)
- **ProGuard Rules:** Updated with comprehensive rules for:
  - Kotlin metadata preservation
  - Compose components
  - Retrofit/Gson
  - OkHttp
  - Data classes
  - Native methods

### Warnings Addressed

#### 1. Deobfuscation File Warning
**Fixed by:**
- Enabling R8/ProGuard minification
- ProGuard will generate `mapping.txt` file automatically
- Location: `app/build/outputs/mapping/release/mapping.txt`

**To Upload to Google Play Console:**
1. Build release AAB: `gradlew bundleRelease`
2. Find mapping file: `app/build/outputs/mapping/release/mapping.txt`
3. Upload to Play Console: **App Bundle Explorer → Upload mapping file**

#### 2. Debug Symbols Warning
**Fixed by:**
- ProGuard rules now preserve necessary symbols
- Native code symbols preserved with `-keepclasseswithmembernames class * { native <methods>; }`

**Note:** If you have native libraries (.so files), you may need to upload symbol files separately.

---

## 📦 Building Version 3.0

### Build Release AAB
```bash
./gradlew bundleRelease
```

### Output Files
- **AAB:** `app/build/outputs/bundle/release/Fynical_v3.0_release.aab`
- **Mapping File:** `app/build/outputs/mapping/release/mapping.txt` (for deobfuscation)

---

## 📤 Uploading to Google Play Console

### Step 1: Upload AAB
1. Go to Google Play Console
2. Select your app
3. Go to **Production** (or testing track)
4. Click **Create new release**
5. Upload: `Fynical_v3.0_release.aab`

### Step 2: Upload Mapping File (Important!)
1. After uploading AAB, go to **App Bundle Explorer**
2. Click on the uploaded AAB
3. Scroll to **"Deobfuscation file"** section
4. Click **"Upload"**
5. Select: `app/build/outputs/mapping/release/mapping.txt`
6. Click **"Save"**

### Step 3: Add Release Notes
```
Version 3.0

- Enhanced share functionality in Denomination Manager
- Improved app performance with code optimization
- Updated YouTube tutorial link
- Bug fixes and improvements
```

---

## ✅ Benefits of R8/ProGuard

1. **Smaller App Size:** Code and resources are optimized
2. **Better Performance:** Unused code removed
3. **Security:** Code obfuscation
4. **Crash Analysis:** Mapping file enables readable stack traces

---

## ⚠️ Important Notes

1. **Test Thoroughly:** R8 may remove code it thinks is unused. Test all features.
2. **Mapping File:** Always upload mapping.txt to Play Console for crash analysis
3. **Keep Mapping Files:** Store mapping files securely for each release
4. **ProGuard Rules:** If you encounter issues, check ProGuard rules

---

## 🔍 Troubleshooting

### App Crashes After Enabling ProGuard
- Check ProGuard rules in `app/proguard-rules.pro`
- Add `-keep` rules for classes that are accessed via reflection
- Test on release build, not just debug

### Mapping File Not Generated
- Ensure `isMinifyEnabled = true` in release buildType
- Build release variant: `gradlew bundleRelease`
- Check `app/build/outputs/mapping/release/` folder

---

**Version:** 3.0
**Date:** January 2026
