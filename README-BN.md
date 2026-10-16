# DroidSpace VM — Android VM launcher

DroidSpace VM হলো একটি native Android launcher, যা আলাদা QubeVM (QEMU-based) অ্যাপের সঙ্গে কাজ করার জন্য তৈরি। এটি নিজে QEMU engine বা guest OS image bundle করে না।

## বর্তমান অ্যাপের ফিচার
- ফোনের supported CPU ABI দেখায়।
- Android 11+ এবং arm64-v8a compatibility যাচাই করে।
- অফিসিয়াল QubeVM ARM64 APK Android Download Manager দিয়ে ডাউনলোড শুরু করে এবং notification progress দেখায়।
- প্রয়োজন হলে Android-এর “Install unknown apps” সেটিংসে নিয়ে যায়।
- QubeVM ইনস্টল থাকলে Start Virtual Machine দিয়ে সেটি খোলে।
- Guest OS setup guide এবং আলাদা design preview রয়েছে।
- Download receiver lifecycle cleanup যুক্ত করা হয়েছে।

## যেগুলো এখনও সম্পন্ন/যাচাইকৃত নয়
- QEMU engine DroidSpace APK-এর ভেতরে embed করা হয়নি; QubeVM আলাদা অ্যাপ।
- Guest OS image bundled নয়। ব্যবহারকারীকে QubeVM-এ সমর্থিত image বেছে নিতে হবে।
- VM profile তৈরি/সংরক্ষণ, CPU/RAM/disk settings DroidSpace-এর নিজস্ব UI থেকে নিয়ন্ত্রণ, guest Android boot, guest-এর ভিতরে APK install, networking এবং stop/resume এখনও এই প্রকল্পে end-to-end যাচাই করা হয়নি।
- ARMv7/32-bit host-এর জন্য অফিসিয়াল QubeVM engine নেই। DroidSpace launcher-এর ARMv7 APK থাকলেই engine ARMv7-তে চলবে না।
- এই ফোনে সত্যিকারের guest OS boot সফল হয়েছে—এমন দাবি করা হচ্ছে না; বাস্তব ডিভাইসে পরীক্ষা দরকার।

## সর্বশেষ APK ডাউনলোড
1. [সর্বশেষ GitHub Actions build খুলুন](https://github.com/tdgxjb/Droidspace16/actions/runs/38895268567)।
2. **Artifacts** অংশে `DroidSpace16-debug-apks` নির্বাচন করুন।
3. ZIP extract করে আপনার ফোনের উপযুক্ত APK ইনস্টল করুন।
4. ইনস্টল করার আগে আগের পরীক্ষামূলক build-এর app data দরকার না হলে সেটি মুছে নতুন APK ইনস্টল করতে পারেন।

## Engine
- [QubeVM official release](https://github.com/rhn-1k/QubeVM/releases/tag/7.1.3)
- [QubeVM project guide](https://github.com/rhn-1k/QubeVM)

## Build
GitHub Actions workflow: `.github/workflows/build-apk.yml`.
Package: `com.droidspace16.app`; minimum Android API 23.
