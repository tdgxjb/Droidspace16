# DroidSpace VM — বাস্তব QEMU ইঞ্জিন সংযুক্তির কাজের নথি

## বর্তমান লক্ষ্য
DroidSpace-এর ভেতরেই QEMU-চালিত গেস্ট VM চালানো। আলাদা QubeVM অ্যাপ খুলে দেওয়া কেবল অস্থায়ী bridge; এটিকে embedded VM বলা যাবে না।

## যাচাইকৃত upstream তথ্য
- Upstream: https://github.com/rhn-1k/QubeVM
- লাইসেন্স: GPL-3.0 (QubeVM repository metadata এবং COPYING অনুযায়ী)।
- QubeVM-এ QEMU native Android shared libraries এবং Java UI আলাদা module-এ আছে:
  - `qube-android-lib`
  - `qube-android-arm`
- upstream developer notes-এ Android NDK r30, JDK 17, Meson/Ninja, QEMU source ও patched dependencies দরকার বলা হয়েছে।
- ARM64 host build-এর জন্য upstream configuration-এ `BUILD_HOST=arm64-v8a`; guest architecture build আলাদা বিষয়।
- QubeVM-এর stock Android-এ `/dev/kvm` সাধারণত নেই; তাই software TCG emulation-ই প্রত্যাশিত। x86 Android guest ARM ফোনে ধীর হতে পারে।

## কাজের ধাপ
1. **Source/license integration:** upstream source-এর নির্দিষ্ট release/tag pin করা; GPL-3.0 অনুযায়ী copyright, license text, source offer ও notices বজায় রাখা।
2. **Native build pipeline:** GitHub Actions-এ NDK r30/JDK 17 toolchain ও upstream dependency/patch build reproducibly চালানো। প্রথমে শুধু `arm64-v8a` host target।
3. **Android module integration:** upstream QubeVM Java library ও ARM guest front-end module-কে DroidSpace-এর Gradle project-এ আনতে হবে; manifest/resources/package collisions ঠিক করতে হবে। শুধু APK download বা package launch embedded integration নয়।
4. **VM profile bridge:** QubeVM-এর machine/database/service API ব্যবহার করে guest VM create/select/start/stop, disk/ISO picker ও persistent config-কে DroidSpace UI-তে যুক্ত করা।
5. **Guest boot validation:** ব্যবহারকারী-নির্বাচিত বৈধ boot image দিয়ে Android-x86/অন্য guest boot পরীক্ষা; console, input, storage, networking ও crash recovery যাচাই।
6. **Release gate:** CI build success যথেষ্ট নয়। বাস্তব ফোনে guest boot সফলভাবে পরীক্ষা না হওয়া পর্যন্ত release-কে experimental বলতে হবে।

## এখনো করা হয়নি
- QEMU native `.so` লাইব্রেরি DroidSpace APK-তে সংযুক্ত করা হয়নি।
- QubeVM-এর Java modules DroidSpace-এ import করা হয়নি।
- DroidSpace থেকে সরাসরি guest VM create/start/stop করা যায় না।
- Android guest boot test সফল হয়েছে এমন প্রমাণ নেই।

## প্রথম বাস্তব লক্ষ্য
একটি ARM64 debug build-এ upstream QubeVM engine ও UI-কে DroidSpace package-এর মধ্যে আনতে পারা। Build error/manifest conflict সমাধান করার পরই VM profile integration শুরু হবে।
