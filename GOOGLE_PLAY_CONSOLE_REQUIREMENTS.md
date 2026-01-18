# Google Play Console - Publication Requirements for Fynical

## 📱 App Information

- **App Name:** Fynical
- **Package Name:** com.kholhang.fynical
- **Version Code:** 1
- **Version Name:** 1.0
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36 (Android 16)
- **Category:** Finance

---

## ✅ Required Items Checklist

### 1. Store Listing Information

#### App Name
- **Short Name:** Fynical (30 characters max) ✅
- **Full Name:** Fynical - Financial Calculator (50 characters max)

#### Short Description (80 characters max)
```
Comprehensive financial calculator for loans, investments, tax, and more.
```

#### Full Description (4000 characters max)
```
Fynical is your all-in-one financial calculator app designed to simplify complex financial calculations. Whether you're planning a loan, managing investments, calculating taxes, or converting currencies, Fynical has you covered.

🎯 KEY FEATURES:

📊 COMPREHENSIVE CALCULATORS
• EMI Calculator - Calculate loan EMIs with detailed amortization schedules
• Fixed Deposit (FD) Calculator - Plan your FD investments
• Recurring Deposit (RD) Calculator - Calculate RD maturity amounts
• SIP Calculator - Systematic Investment Plan calculations
• PPF Calculator - Public Provident Fund planning
• Simple & Compound Interest Calculators
• Lumpsum Investment Calculator
• Inflation Calculator

💰 LOAN MANAGEMENT TOOLS
• Advanced EMI Calculator with prepayment options
• Loan Eligibility Calculator
• Loan Comparison Tool
• Prepayment Calculator
• ROI Change Impact Calculator
• Moratorium Calculator
• Loan Profile Manager

📈 TAX & PRICING TOOLS
• GST Calculator (CGST, SGST, IGST)
• VAT Calculator
• Income Tax Calculator
• Discount Calculator

🌍 UTILITIES
• Currency Converter with live exchange rates
• Amount to Words Converter
• Denomination Manager for cash management
• Scientific Calculator
• Simple Calculator

✨ ADDITIONAL FEATURES
• Export results as PDF or PNG
• Share calculations easily
• Dark mode support
• Clean, modern Material Design 3 UI
• All formulas verified as per 2026 banking standards
• Atal Pension Yojana (APY) calculator with latest 2026 rules

📱 PERFECT FOR:
• Bankers and financial advisors
• Loan applicants and borrowers
• Investors planning their portfolio
• Business owners calculating taxes
• Students learning finance
• Anyone managing personal finances

🔒 PRIVACY & SECURITY
• No data collection
• All calculations done locally on your device
• No internet required for most features (except currency converter)
• No ads or tracking

Download Fynical today and take control of your financial planning!
```

#### App Category
- **Primary Category:** Finance
- **Secondary Category:** Tools (optional)

#### Tags/Keywords (comma-separated)
```
financial calculator, EMI calculator, loan calculator, investment calculator, tax calculator, GST calculator, FD calculator, RD calculator, SIP calculator, PPF calculator, currency converter, banking calculator, finance app
```

---

### 2. Graphics & Media Assets

#### App Icon
- ✅ Already created: Adaptive icon (ic_launcher.xml)
- **Required Sizes:**
  - 512 x 512 pixels (PNG, 32-bit, no transparency)
  - 1024 x 1024 pixels (for feature graphic)

#### Feature Graphic
- **Size:** 1024 x 500 pixels
- **Format:** PNG or JPG
- **Content:** App name, tagline, key features
- **Note:** Create this graphic with app branding

#### Screenshots (Required)
**Phone Screenshots (at least 2, up to 8):**
- Minimum: 2 screenshots
- Recommended: 4-8 screenshots
- **Sizes:**
  - 16:9 or 9:16 aspect ratio
  - Minimum: 320px (shortest side)
  - Maximum: 3840px (longest side)
  - Format: PNG or JPG (24-bit)

**Suggested Screenshots:**
1. Home screen showing all calculator categories
2. EMI Calculator with results
3. Denomination Manager interface
4. Investment calculators (FD/RD/SIP)
5. Tax calculators (GST/Income Tax)
6. Currency Converter
7. Dark mode view
8. Export/Share feature

**Tablet Screenshots (Optional but recommended):**
- Same requirements as phone
- 7" or 10" tablet screenshots

#### Promotional Video (Optional)
- YouTube URL (if available)
- Maximum 2 minutes
- Showcase key features

---

### 3. Privacy Policy

**Status:** ⚠️ **REQUIRED** - Must be hosted on a publicly accessible URL

**Privacy Policy URL:** [Your URL here]
Example: `https://yourwebsite.com/privacy-policy` or `https://github.com/yourusername/fynical/blob/main/PRIVACY_POLICY.md`

**Privacy Policy Template:** See `PRIVACY_POLICY.md` file

**Key Points to Include:**
- Data collection (if any)
- Data usage
- Data sharing
- User rights
- Contact information

---

### 4. Content Rating

#### Content Rating Questionnaire Answers

**Category: Finance**
- **Does your app contain user-generated content?** No
- **Does your app allow users to communicate with each other?** No
- **Does your app allow users to share personal information?** No
- **Does your app contain ads?** No
- **Does your app allow in-app purchases?** No (or Yes, if you plan to add)

**Expected Rating:** Everyone (PEGI 3, ESRB Everyone)

---

### 5. App Access

#### Permissions Declaration

**Required Permissions:**
1. **INTERNET** - For currency converter API calls
2. **ACCESS_NETWORK_STATE** - To check network connectivity
3. **WRITE_EXTERNAL_STORAGE** (maxSdkVersion 28) - For saving PDF exports on older Android versions
4. **READ_EXTERNAL_STORAGE** (maxSdkVersion 28) - For reading files on older Android versions

**Permission Justification:**
- Internet: Required for live currency exchange rates
- Network State: To inform users when offline
- Storage: For exporting calculation results as PDF (legacy Android support)

**Sensitive Permissions:** None

---

### 6. Pricing & Distribution

#### Pricing
- **Free or Paid?** Free
- **In-app purchases?** No (or Yes, if planned)

#### Countries/Regions
- Select all countries where you want to distribute
- Recommended: Start with India, then expand

#### Content Guidelines Compliance
- ✅ No prohibited content
- ✅ No misleading information
- ✅ Accurate financial calculations
- ✅ Clear disclaimers (if needed)

---

### 7. Store Listing Localization

#### Default Language: English (United States)

**Additional Languages (Optional but recommended):**
- English (India)
- Hindi (if you plan to localize)

---

### 8. App Signing

#### App Signing Key
- **Key Type:** Upload key (for Google Play App Signing)
- **Key Alias:** (Your key alias)
- **Key Store:** (Your keystore file)

**Important:** 
- Keep your upload key secure
- Google will manage the app signing key
- Use App Bundle (AAB) format for upload

---

### 9. Release Management

#### Production Release
- **Release Type:** Production
- **Release Name:** 1.0 (Initial Release)
- **Release Notes:**
```
Initial release of Fynical - Financial Calculator

Features:
• Comprehensive financial calculators (EMI, FD, RD, SIP, PPF)
• Loan management tools
• Tax calculators (GST, VAT, Income Tax)
• Currency converter with live rates
• Denomination manager
• Export results as PDF/PNG
• Dark mode support
• Modern Material Design 3 UI
```

#### Testing (Optional but recommended)
- **Internal Testing:** Test with internal team
- **Closed Testing:** Beta test with selected users
- **Open Testing:** Public beta (optional)

---

### 10. Data Safety

#### Data Collection
- **Does your app collect user data?** No
- **Does your app share user data?** No
- **Does your app use encryption?** No (not required for this app)

#### Data Safety Form Answers:
- ✅ No data collection
- ✅ No data sharing
- ✅ No data selling
- ✅ No personal information collection
- ✅ No location data
- ✅ No user content collection

---

### 11. Target Audience

#### Target Audience
- **Age Group:** Everyone
- **Primary Audience:** Adults (18+)
- **Content Rating:** Everyone

---

### 12. Contact Details

#### Developer Contact Information
- **Email:** [Your support email]
- **Website:** [Your website URL] (optional)
- **Phone:** [Your phone number] (optional)

---

## 📋 Pre-Submission Checklist

Before submitting to Google Play Console:

- [ ] App tested on multiple devices and Android versions
- [ ] All features working correctly
- [ ] No crashes or ANRs (Application Not Responding)
- [ ] Privacy policy URL is live and accessible
- [ ] App icon (512x512) created
- [ ] Feature graphic (1024x500) created
- [ ] At least 2 phone screenshots prepared
- [ ] App description written and proofread
- [ ] Content rating questionnaire completed
- [ ] Data safety form completed
- [ ] App signing key created and secured
- [ ] Release notes prepared
- [ ] App bundle (AAB) built and signed
- [ ] Tested on Google Play Console internal testing track
- [ ] All permissions justified
- [ ] No prohibited content
- [ ] Terms of service (if applicable)

---

## 🚀 Submission Steps

1. **Create Google Play Console Account**
   - Go to https://play.google.com/console
   - Pay one-time $25 registration fee

2. **Create New App**
   - Click "Create app"
   - Fill in app details
   - Select default language

3. **Complete Store Listing**
   - Upload app icon
   - Upload feature graphic
   - Add screenshots
   - Write descriptions
   - Set category

4. **Set Up App Content**
   - Complete content rating questionnaire
   - Add privacy policy URL
   - Complete data safety form

5. **Prepare Release**
   - Build signed App Bundle (AAB)
   - Upload to Production track
   - Add release notes

6. **Review & Submit**
   - Review all information
   - Submit for review
   - Wait for approval (usually 1-3 days)

---

## 📝 Notes

- **Review Time:** Google Play review typically takes 1-3 business days
- **App Bundle:** Use AAB format, not APK for production releases
- **Version Updates:** Increment versionCode for each release
- **Testing:** Always test on internal/closed testing tracks first
- **Updates:** Keep app updated with latest Android SDK requirements

---

## 🔗 Useful Links

- [Google Play Console](https://play.google.com/console)
- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [App Bundle Guide](https://developer.android.com/guide/app-bundle)
- [Content Rating](https://support.google.com/googleplay/android-developer/answer/9888179)
- [Data Safety](https://support.google.com/googleplay/android-developer/answer/10787469)

---

**Last Updated:** January 2026
**App Version:** 1.0
