# Quick Logo Generation for Google Play Console

## 🎯 Required: 512 x 512 PNG Logo

### Method 1: Using Android Studio (Easiest)

1. **Open Android Studio**
2. **Right-click** on `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
3. Select **"New > Image Asset"**
4. In Asset Studio:
   - **Icon Type:** Launcher Icons (Adaptive and Legacy)
   - **Foreground Layer:** 
     - Asset Type: **Image**
     - Path: `app/src/main/res/drawable/ic_abacus.xml`
   - **Background Layer:**
     - Asset Type: **Color**
     - Color: `#2196F3` (Blue)
   - **Legacy Icon:**
     - Check **"Generate"**
     - Shape: **Square**
     - Size: **512**
5. Click **"Next"** → **"Finish"**
6. Find generated PNG: `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
7. Copy and rename to `fynical-logo-512.png`

### Method 2: Using Online Tool (Canva)

1. Go to [Canva.com](https://www.canva.com)
2. Create custom size: **512 x 512 pixels**
3. Add background: Color `#2196F3` (blue)
4. Add abacus icon (upload or use Canva's icons)
5. Center the icon
6. Download as **PNG**

### Method 3: Manual Export

If you have the abacus icon as an image:
1. Open in image editor (Photoshop, GIMP, Paint.NET)
2. Create new canvas: 512 x 512 pixels
3. Fill background with `#2196F3`
4. Place abacus icon in center
5. Export as PNG

---

## 📤 Upload to Google Play Console

1. Go to **Google Play Console**
2. Select your app **"Fynical"**
3. Go to: **Store listing → Graphics**
4. Find **"App icon"** section
5. Click **"Upload"**
6. Select `fynical-logo-512.png`
7. Click **"Save"**

---

## ✅ Logo Checklist

- [ ] Exactly 512 x 512 pixels
- [ ] PNG format
- [ ] No transparency (solid background)
- [ ] Icon centered
- [ ] High quality (not pixelated)
- [ ] Professional appearance
- [ ] Matches app branding

---

**Quick Tip:** The abacus icon is already in your app at `app/src/main/res/drawable/ic_abacus.xml`. Use Android Studio's Asset Studio to generate the 512x512 PNG easily!
