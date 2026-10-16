# DroidSpace 16 — Android APK wrapper

এটি DroidSpace 16-এর বর্তমান Lovable web preview-কে Android WebView অ্যাপের মধ্যে খোলার একটি প্রাথমিক native Android project।

## গুরুত্বপূর্ণ সীমাবদ্ধতা
- এটি নিজে থেকে সম্পূর্ণ Android OS চালায় না।
- এটি APK ফাইলগুলো ইনস্টল/চালানোর virtual machine নয়।
- বর্তমান UI ইন্টারনেটের মাধ্যমে Lovable preview থেকে লোড হয়; তাই preview URL কাজ করা এবং ইন্টারনেট থাকা দরকার।
- এটি একটি debug APK তৈরি করার source project; APK বাইনারি build workflow সফল হওয়ার আগে তৈরি হয়েছে বলে ধরে নেওয়া যাবে না।
- GitHub Actions workflow চালিয়ে APK তৈরি করার চেষ্টা করা হবে।

## APK ডাউনলোড করার ধাপ
1. GitHub repository-এর **Actions** tab খুলুন।
2. **Build DroidSpace 16 APK** workflow-এর সর্বশেষ run খুলুন।
3. সফল হলে **Artifacts** থেকে `DroidSpace16-debug-apk` ডাউনলোড করুন।
4. ZIP extract করে `app-debug.apk` ফাইলটি Android ফোনে install করুন। ফোন চাইলে Chrome/Files থেকে app install করার অনুমতি দিন।

## তথ্য
Package: `com.droidspace16.app`
Minimum Android: Android 6.0 (API 23)
Target SDK: 28 (প্রাথমিক পরীক্ষামূলক build)

পূর্ণ Android guest OS ও APK execution আলাদা virtualization প্রকল্প; এই wrapper তা দেয় না।


## Build status
GitHub Actions workflow: `.github/workflows/build-apk.yml`.
