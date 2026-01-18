# Building App Bundle (AAB) for Google Play Store

## Why App Bundle (AAB)?

Google Play requires **App Bundle (AAB)** format for production releases, not APK. AAB allows Google Play to optimize app delivery for different device configurations.

---

## Prerequisites

1. **Keystore File** - For signing the release build
2. **Key Alias** - Your signing key alias
3. **Key Password** - Password for your keystore
4. **Key Store Password** - Password for the keystore file

---

## Step 1: Create Keystore (If Not Already Created)

### Option A: Using Android Studio
1. Open Android Studio
2. Go to **Build** → **Generate Signed Bundle / APK**
3. Select **Android App Bundle**
4. Click **Create new...** to create a new keystore
5. Fill in the keystore information:
   - **Key store path:** Choose location and filename (e.g., `fynical-release-key.jks`)
   - **Password:** Create a strong password
   - **Key alias:** e.g., `fynical-key`
   - **Key password:** Create a strong password
   - **Validity:** 25 years (recommended)
   - **Certificate information:** Fill in your details
6. Click **OK** to create the keystore

### Option B: Using Command Line
```bash
keytool -genkey -v -keystore fynical-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias fynical-key
```

**Important:** 
- Store the keystore file securely
- Keep passwords safe (use a password manager)
- Never commit keystore to version control
- Make backups of your keystore

---

## Step 2: Configure Signing in build.gradle.kts

Add signing configuration to `app/build.gradle.kts`:

```kotlin
android {
    // ... existing configuration ...
    
    signingConfigs {
        create("release") {
            storeFile = file("path/to/your/keystore.jks")
            storePassword = "your-keystore-password"
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

**⚠️ Security Note:** For better security, use `local.properties` or environment variables instead of hardcoding passwords:

### Using local.properties (Recommended)

1. Create/Edit `local.properties` in project root:
```properties
storePassword=your-keystore-password
keyPassword=your-key-password
```

2. Update `build.gradle.kts`:
```kotlin
val keystorePropertiesFile = rootProject.file("local.properties")
val keystoreProperties = java.util.Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(java.io.FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/your/keystore.jks")
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = "your-key-alias"
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    // ... rest of configuration
}
```

3. Add `local.properties` to `.gitignore`:
```
local.properties
*.jks
*.keystore
```

---

## Step 3: Build App Bundle (AAB)

### Using Gradle Command Line

```bash
# Windows
.\gradlew.bat bundleRelease

# Linux/Mac
./gradlew bundleRelease
```

### Using Android Studio
1. Go to **Build** → **Generate Signed Bundle / APK**
2. Select **Android App Bundle**
3. Click **Next**
4. Select your keystore file
5. Enter passwords
6. Select **release** build variant
7. Click **Finish**
8. AAB file will be generated in `app/build/outputs/bundle/release/`

---

## Step 4: Locate the AAB File

After building, the AAB file will be located at:
```
app/build/outputs/bundle/release/Fynical_v1.0_release.aab
```

Or with default naming:
```
app/build/outputs/bundle/release/app-release.aab
```

---

## Step 5: Verify the AAB

### Check AAB File
- File should have `.aab` extension
- File size should be reasonable (usually smaller than APK)
- File should be signed (you can verify using `bundletool`)

### Using bundletool to Verify
```bash
# Download bundletool from: https://github.com/google/bundletool/releases
java -jar bundletool.jar build-apks --bundle=app-release.aab --output=app.apks
```

---

## Step 6: Upload to Google Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app
3. Go to **Production** (or **Internal testing** / **Closed testing**)
4. Click **Create new release**
5. Upload the AAB file
6. Add release notes
7. Review and publish

---

## Troubleshooting

### Error: Keystore file not found
- Check the path in `build.gradle.kts`
- Use absolute path or relative path from project root
- Ensure keystore file exists

### Error: Wrong password
- Double-check keystore password and key password
- Ensure no extra spaces or special characters

### Error: Key alias not found
- Verify the key alias name
- List aliases: `keytool -list -v -keystore your-keystore.jks`

### AAB file too large
- Enable ProGuard/R8 minification
- Remove unused resources
- Optimize images

---

## Version Management

### Updating Version for New Release

In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.1"  // Update version name
}
```

**Rules:**
- `versionCode` must be higher than previous release
- `versionName` is user-facing (can be any format)
- Google Play uses `versionCode` to determine updates

---

## Google Play App Signing

### Recommended: Use Google Play App Signing

1. **Upload Key:** The key you use to sign the AAB (upload key)
2. **App Signing Key:** Google manages this key (more secure)

**Benefits:**
- Google manages the app signing key securely
- Can reset upload key if lost
- Better security

**Setup:**
1. Upload your first AAB
2. Google Play will ask to enroll in App Signing
3. Upload your upload key certificate
4. Google will manage app signing from then on

---

## Best Practices

1. **Backup Keystore:** Store keystore in multiple secure locations
2. **Document Passwords:** Store passwords securely (password manager)
3. **Version Control:** Never commit keystore to Git
4. **Test Before Release:** Test release build before uploading
5. **Increment Version:** Always increment versionCode
6. **Release Notes:** Write clear release notes for users

---

## Quick Build Script

Create `build-release-aab.bat` (Windows) or `build-release-aab.sh` (Linux/Mac):

### Windows (build-release-aab.bat)
```batch
@echo off
echo Building Release AAB for Fynical...
call gradlew.bat bundleRelease
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo AAB Build Successful!
    echo ========================================
    echo.
    echo AAB Location: app\build\outputs\bundle\release\
    echo.
    dir /b app\build\outputs\bundle\release\*.aab
    echo.
    pause
) else (
    echo Build failed!
    pause
)
```

### Linux/Mac (build-release-aab.sh)
```bash
#!/bin/bash
echo "Building Release AAB for Fynical..."
./gradlew bundleRelease
if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "AAB Build Successful!"
    echo "========================================"
    echo ""
    echo "AAB Location: app/build/outputs/bundle/release/"
    echo ""
    ls -lh app/build/outputs/bundle/release/*.aab
else
    echo "Build failed!"
fi
```

---

## Summary

1. ✅ Create keystore (if not exists)
2. ✅ Configure signing in build.gradle.kts
3. ✅ Build AAB: `gradlew bundleRelease`
4. ✅ Locate AAB in `app/build/outputs/bundle/release/`
5. ✅ Upload to Google Play Console
6. ✅ Submit for review

---

**Last Updated:** January 2026
