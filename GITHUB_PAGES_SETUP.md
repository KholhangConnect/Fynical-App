# Setting Up Privacy Policy on GitHub Pages

## Step-by-Step Guide

### Step 1: Create GitHub Repository (If Not Already Created)

1. Go to [GitHub](https://github.com)
2. Click the **"+"** icon in the top right
3. Select **"New repository"**
4. Repository name: `fynical-privacy-policy` (or any name you prefer)
5. Description: "Privacy Policy for Fynical App"
6. Make it **Public** (required for GitHub Pages)
7. **DO NOT** initialize with README (we'll add files manually)
8. Click **"Create repository"**

---

### Step 2: Upload Privacy Policy Files

#### Option A: Using GitHub Web Interface

1. Go to your newly created repository
2. Click **"Add file"** → **"Create new file"**
3. Name the file: `index.html`
4. Copy the entire content from `docs/index.html` in this project
5. Paste it into the GitHub editor
6. Scroll down and click **"Commit new file"**

#### Option B: Using Git Command Line

```bash
# Navigate to your project directory
cd "E:\ANDROID PROJECT\BANKERS APP\EMI,Denomination"

# Initialize git (if not already initialized)
git init

# Add remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/fynical-privacy-policy.git

# Copy the index.html to repository root
# Then commit and push
git add index.html
git commit -m "Add privacy policy"
git branch -M main
git push -u origin main
```

---

### Step 3: Enable GitHub Pages

1. Go to your repository on GitHub
2. Click on **"Settings"** tab (top menu)
3. Scroll down to **"Pages"** section (left sidebar)
4. Under **"Source"**, select:
   - **Branch:** `main` (or `master`)
   - **Folder:** `/ (root)`
5. Click **"Save"**
6. Wait a few minutes for GitHub Pages to build

---

### Step 4: Get Your Privacy Policy URL

After enabling GitHub Pages, your privacy policy will be available at:

```
https://YOUR_USERNAME.github.io/fynical-privacy-policy/
```

**Example:**
- If your username is `johnsmith`
- Repository name is `fynical-privacy-policy`
- URL will be: `https://johnsmith.github.io/fynical-privacy-policy/`

---

### Step 5: Update Contact Information

1. Go to your repository
2. Click on `index.html`
3. Click the **pencil icon** (Edit) to edit the file
4. Find this section:
   ```html
                <strong>Email:</strong> <a href="mailto:kholhangconnect@gmail.com">kholhangconnect@gmail.com</a><br>
   <strong>Website:</strong> <a href="https://yourwebsite.com">https://yourwebsite.com</a>
   ```
5. Replace with your actual email and website
6. Click **"Commit changes"**

---

### Step 6: Verify Privacy Policy is Live

1. Open your browser
2. Go to: `https://YOUR_USERNAME.github.io/fynical-privacy-policy/`
3. You should see the privacy policy page
4. Test on mobile device to ensure it's mobile-friendly

---

### Step 7: Use URL in Google Play Console

When filling out the Google Play Console forms:

1. **Privacy Policy URL:** `https://YOUR_USERNAME.github.io/fynical-privacy-policy/`
2. Paste this URL in the privacy policy field
3. Google will verify it's accessible

---

## Alternative: Use Existing Repository

If you already have a GitHub repository for your app:

### Option 1: Add to Existing Repository

1. Create a `docs` folder in your repository
2. Add `index.html` to the `docs` folder
3. Enable GitHub Pages from `docs` folder
4. URL will be: `https://YOUR_USERNAME.github.io/REPO_NAME/`

### Option 2: Create Separate Repository

- Create a dedicated repository just for privacy policy
- Follow steps above
- Keeps privacy policy separate and easy to update

---

## Quick Setup Script

If you prefer to use the command line, here's a quick setup:

```bash
# 1. Create new directory
mkdir fynical-privacy-policy
cd fynical-privacy-policy

# 2. Copy index.html from docs folder
# (Copy the file manually or use copy command)

# 3. Initialize git
git init
git add index.html
git commit -m "Initial commit: Privacy Policy"

# 4. Add remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/fynical-privacy-policy.git
git branch -M main
git push -u origin main

# 5. Then enable GitHub Pages in repository settings
```

---

## Troubleshooting

### Privacy Policy Not Loading

1. **Check Repository is Public:**
   - Go to Settings → General
   - Ensure repository is set to "Public"

2. **Check GitHub Pages is Enabled:**
   - Go to Settings → Pages
   - Verify source is set correctly

3. **Wait a Few Minutes:**
   - GitHub Pages can take 1-5 minutes to build
   - Check again after waiting

4. **Check File Name:**
   - File must be named `index.html`
   - Must be in root directory (or docs folder if using that)

### URL Not Working

- Ensure you're using the correct format: `https://USERNAME.github.io/REPO_NAME/`
- Check for typos in username or repository name
- Try accessing in incognito/private browser window

### Updating Privacy Policy

1. Edit `index.html` in your repository
2. Commit changes
3. GitHub Pages will automatically update (may take a few minutes)

---

## Example URLs

Here are some example privacy policy URLs:

- `https://johndoe.github.io/fynical-privacy-policy/`
- `https://yourcompany.github.io/fynical-app/privacy/`
- `https://username.github.io/app-docs/`

---

## Important Notes

✅ **Repository Must Be Public** - GitHub Pages only works with public repositories (free tier)

✅ **File Must Be Named `index.html`** - This is the default file GitHub Pages serves

✅ **Update Contact Info** - Don't forget to replace email and website placeholders

✅ **Test the URL** - Verify the privacy policy loads correctly before submitting to Google Play

✅ **Keep It Updated** - Update the "Last Updated" date when you make changes

---

## Next Steps

After setting up GitHub Pages:

1. ✅ Copy your privacy policy URL
2. ✅ Test it in a browser
3. ✅ Use it in Google Play Console
4. ✅ Submit your app for review

---

**Need Help?**
- [GitHub Pages Documentation](https://docs.github.com/en/pages)
- [GitHub Pages Quickstart](https://docs.github.com/en/pages/quickstart)

---

**Last Updated:** January 2026
