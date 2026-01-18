# Logo Generation Guide for Google Play Console

## 📱 Required Logo: 512 x 512 PNG

Google Play Console requires a **512 x 512 pixels PNG** file for the app icon.

---

## 🎨 Creating the Logo

### Option 1: Export from Android Studio (Recommended)

1. **Open Android Studio**
2. **Open the project**
3. Go to: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
4. Right-click on the adaptive icon
5. Select **"Generate Image Asset"** or **"New > Image Asset"**
6. In the Asset Studio:
   - **Icon Type:** Launcher Icons (Adaptive and Legacy)
   - **Foreground Layer:** Use `ic_abacus.xml` (already set)
   - **Background Layer:** Use color `#2196F3` (blue)
7. **Export Settings:**
   - Check **"Generate PNG"**
   - Set size to **512 x 512**
   - Export location: Choose a folder
8. Click **"Next"** and **"Finish"**
9. The 512x512 PNG will be generated

### Option 2: Using Image Editing Software

1. **Open your design tool** (Photoshop, GIMP, Canva, Figma)
2. **Create new image:** 512 x 512 pixels
3. **Background:** Solid color `#2196F3` (blue) or gradient
4. **Add abacus icon:**
   - Use the vector from `app/src/main/res/drawable/ic_abacus.xml`
   - Or recreate the abacus design
   - Center it in the canvas
5. **Export as PNG:**
   - 32-bit color
   - No transparency (or solid background)
   - 512 x 512 pixels

### Option 3: Using Online Tools

1. **Canva:**
   - Go to [Canva.com](https://www.canva.com)
   - Create custom size: 512 x 512 pixels
   - Add abacus icon/image
   - Add "Fynical" text (optional)
   - Download as PNG

2. **Figma:**
   - Create 512 x 512 frame
   - Import abacus icon
   - Export as PNG

---

## 📐 Logo Specifications

### Required
- **Size:** 512 x 512 pixels (exact)
- **Format:** PNG
- **Color Depth:** 32-bit
- **Background:** Solid color or gradient (no transparency)
- **Content:** App icon (abacus design)

### Design Guidelines
- **Simple:** Should be recognizable at small sizes
- **Clear:** High contrast between icon and background
- **Professional:** Clean, modern design
- **No Text:** Icon only (no app name text)
- **Centered:** Icon centered in the square

---

## 🎯 Quick Steps

### Using Android Studio Asset Studio:

1. **File → New → Image Asset**
2. **Icon Type:** Launcher Icons (Adaptive and Legacy)
3. **Foreground:** `ic_abacus.xml` (current)
4. **Background:** Color `#2196F3`
5. **Legacy Icon:**
   - Check **"Generate"**
   - Set **"Shape"** to Square
   - Set **"Size"** to 512
6. Click **"Next"** → **"Finish"**
7. Find the generated PNG in: `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
8. Copy and rename to `fynical-logo-512.png`

---

## 📂 File Location

After generation, save the logo as:
```
fynical-logo-512.png
```

**Location for Google Play Console:**
- Upload this file when creating/editing your app listing
- Go to: **Store listing → Graphics → App icon**

---

## ✅ Checklist

- [ ] Logo is exactly 512 x 512 pixels
- [ ] Format is PNG
- [ ] No transparency (solid background)
- [ ] Icon is centered
- [ ] High quality (not pixelated)
- [ ] Looks good at small sizes
- [ ] Professional appearance
- [ ] Matches app branding

---

## 🎨 Design Tips

1. **Use High Resolution:** Start with vector or high-res image
2. **Test at Small Size:** View at 48x48 to ensure readability
3. **Contrast:** Ensure icon stands out from background
4. **Simple:** Avoid too many details
5. **Consistent:** Match your app's color scheme

---

## 🔗 Resources

- **Current Icon:** `app/src/main/res/drawable/ic_abacus.xml`
- **Adaptive Icon Config:** `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- **Color:** `#2196F3` (Blue) - from `app/src/main/res/values/colors.xml`

---

## 📝 Quick Command (If using ImageMagick)

```bash
# Convert existing icon to 512x512 PNG (if you have a source image)
convert source-icon.png -resize 512x512 -background "#2196F3" -gravity center -extent 512x512 fynical-logo-512.png
```

---

**Note:** The logo must be a clean, professional representation of your app. The abacus icon is perfect for a financial calculator app!

**Last Updated:** January 2026
