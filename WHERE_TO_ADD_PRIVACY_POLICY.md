# Where to Add Privacy Policy URL in Google Play Console

## 📍 Location in Google Play Console

### Step 1: Navigate to App Content

1. Log in to [Google Play Console](https://play.google.com/console)
2. Select your app **"Fynical"**
3. In the left sidebar, go to:
   ```
   Policy → App content
   ```
   Or navigate directly to:
   ```
   https://play.google.com/console/u/0/developers/[YOUR_DEV_ID]/app/[APP_ID]/app-content
   ```

### Step 2: Find Privacy Policy Section

Scroll down to find the **"Privacy Policy"** section. It's usually near the top of the App content page.

### Step 3: Add Privacy Policy URL

1. Click on **"Privacy Policy"** section
2. You'll see a field: **"Privacy Policy URL"**
3. Enter your GitHub Pages URL:
   ```
   https://YOUR_USERNAME.github.io/fynical-privacy-policy/
   ```
4. Click **"Save"**

---

## 📋 Complete Path in Google Play Console

```
Google Play Console
  └── Your App (Fynical)
      └── Policy (Left Sidebar)
          └── App content
              └── Privacy Policy
                  └── Privacy Policy URL: [Enter your GitHub Pages URL here]
```

---

## 🔍 Visual Guide

### What You'll See:

```
┌─────────────────────────────────────────┐
│  App content                            │
├─────────────────────────────────────────┤
│                                          │
│  Privacy Policy                         │
│  ┌───────────────────────────────────┐  │
│  │ Privacy Policy URL *             │  │
│  │ https://username.github.io/...  │  │
│  └───────────────────────────────────┘  │
│                                          │
│  [Save] button                          │
│                                          │
└─────────────────────────────────────────┘
```

---

## ✅ Requirements

- **URL Must Be:**
  - Publicly accessible (no login required)
  - HTTPS (secure connection)
  - Accessible from any device/browser
  - Contains actual privacy policy content

- **GitHub Pages URL Format:**
  ```
  https://USERNAME.github.io/REPOSITORY_NAME/
  ```

---

## 🧪 Testing Your URL

Before adding to Google Play Console:

1. **Open URL in Browser:**
   - Go to: `https://YOUR_USERNAME.github.io/fynical-privacy-policy/`
   - Should load without errors

2. **Test on Mobile:**
   - Open URL on your phone
   - Should be mobile-friendly

3. **Test in Incognito:**
   - Open in private/incognito window
   - Should work without login

4. **Check Content:**
   - Privacy policy text should be visible
   - Contact information should be updated

---

## 📝 Step-by-Step Checklist

- [ ] Created GitHub repository for privacy policy
- [ ] Uploaded `index.html` file
- [ ] Enabled GitHub Pages
- [ ] Updated contact information in HTML
- [ ] Tested URL in browser (works correctly)
- [ ] Tested URL on mobile (mobile-friendly)
- [ ] Copied GitHub Pages URL
- [ ] Logged into Google Play Console
- [ ] Navigated to: Policy → App content
- [ ] Found Privacy Policy section
- [ ] Entered GitHub Pages URL
- [ ] Clicked Save
- [ ] Verified URL is saved correctly

---

## 🔗 Quick Links

- **Google Play Console:** https://play.google.com/console
- **GitHub Pages Setup Guide:** See `GITHUB_PAGES_SETUP.md`
- **Privacy Policy HTML:** See `docs/index.html`

---

## ⚠️ Important Notes

1. **URL Must Be Live Before Submission:**
   - Google verifies the URL during review
   - If URL is not accessible, app will be rejected

2. **Keep URL Updated:**
   - If you change repository name, update URL in Play Console
   - If you move to different hosting, update URL

3. **Contact Information:**
   - Make sure email and website in privacy policy are correct
   - Google may contact you using this information

4. **HTTPS Required:**
   - GitHub Pages automatically provides HTTPS
   - No additional SSL certificate needed

---

## 🆘 Troubleshooting

### URL Not Accepted

**Problem:** Google Play Console says URL is invalid

**Solutions:**
- Ensure URL starts with `https://`
- Check URL doesn't have trailing slash issues
- Verify repository is public
- Wait a few minutes after enabling GitHub Pages

### URL Not Accessible

**Problem:** Can't access privacy policy URL

**Solutions:**
- Check repository is public (not private)
- Verify GitHub Pages is enabled
- Wait 1-5 minutes for GitHub Pages to build
- Check for typos in URL

### Privacy Policy Not Found

**Problem:** 404 error when accessing URL

**Solutions:**
- Ensure file is named `index.html`
- Check file is in root directory (or docs folder)
- Verify GitHub Pages source is set correctly

---

## 📞 Example

**Your GitHub Username:** `johndoe`  
**Repository Name:** `fynical-privacy-policy`  
**Privacy Policy URL:** `https://johndoe.github.io/fynical-privacy-policy/`

**In Google Play Console:**
1. Go to: Policy → App content
2. Privacy Policy section
3. Enter: `https://johndoe.github.io/fynical-privacy-policy/`
4. Click Save

---

**Last Updated:** January 2026
