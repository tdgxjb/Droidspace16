# DroidSpace VM — বাস্তব ভার্চুয়াল মেশিন পরিকল্পনা

## লক্ষ্য
DroidSpace VM হবে Android ফোনে চালানো একটি ভার্চুয়াল মেশিন অ্যাপ, যেখানে ব্যবহারকারী আলাদা guest OS image বেছে নিয়ে সেটি চালাতে পারবেন। এটি শুধু WebView বা Android-স্টাইল UI হবে না।

## প্রযুক্তিগত ভিত্তি
- Emulator engine: QEMU, Android-এ ব্যবহারের জন্য Limbo-এর QEMU integration থেকে প্রযুক্তিগত ভিত্তি যাচাই করা।
- প্রথম বাস্তব guest target: Android-x86 ISO বা অন্য bootable guest image, ব্যবহারকারী নিজে বেছে নেবেন।
- App layer: VM profile, RAM/CPU/storage settings, image picker, start/stop, console/display.
- Android guest চালাতে x86 image-কে ARM ফোনে emulate করতে হতে পারে; তাই performance খুব ধীর হতে পারে এবং সব Android image boot করবে না।
- ARM64 guest image থাকলেও QEMU software emulation-এর পারফরম্যান্স হার্ডওয়্যার virtualization-এর সমান নয়।

## বাধ্যতামূলক সীমা
- VMOS-এর মতো proprietary optimized Android guest runtime পুনরুৎপাদন করা যাবে না যদি তার engine/API উন্মুক্ত না থাকে।
- Android OS image স্বয়ংক্রিয়ভাবে bundled বা ডাউনলোড করা হবে না; লাইসেন্স ও আকার যাচাই করে ব্যবহারকারী-নির্বাচিত image দিয়ে শুরু করা হবে।
- QEMU-এর native shared libraries (Android NDK) অ্যাপে সংযুক্ত না হওয়া পর্যন্ত এটি বাস্তব VM নয়।
- এই রিপোজিটরির বর্তমান WebView wrapper-কে পূর্ণ VM হিসেবে গণ্য করা যাবে না।

## পরবর্তী প্রকৌশল ধাপ
1. QEMU/Limbo source এবং GPL-2.0 license compatibility যাচাই।
2. Android NDK দিয়ে ARM64 host-এর জন্য QEMU native engine build করা।
3. engine JNI/service interface তৈরি করে VM lifecycle সংযুক্ত করা।
4. একটি অনুমোদিত ছোট guest image দিয়ে boot test করা।
5. Android guest image boot, input, networking, storage, এবং crash recovery পরীক্ষা করা।
6. সফল পরীক্ষার পরই APK-কে functional VM build বলা।

## বর্তমান অবস্থা
এই ফাইলটি লক্ষ্য ও প্রযুক্তিগত পরিকল্পনা নথিভুক্ত করে। QEMU engine এখনও অ্যাপে যুক্ত বা যাচাইকৃত নয়; তাই এটি এখনও কার্যকর Android VM নয়।
