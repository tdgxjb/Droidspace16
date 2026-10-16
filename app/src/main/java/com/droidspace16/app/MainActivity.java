package com.droidspace16.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Environment;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * DroidSpace VM launcher.
 * The QEMU engine currently runs in the separately installed QubeVM app.
 * This launcher never claims that the WebView simulator is a real VM.
 */
public class MainActivity extends Activity {
    private static final String QUBEVm_PACKAGE = "com.qubevm.arm";
    private static final String QUBEVm_APK =
        "https://github.com/rhn-1k/QubeVM/releases/download/7.1.3/qube-arm-q11.1.2-android-arm64-v8a-release.apk";
    private static final String SIMULATOR_URL =
        "https://id-preview--517422a2-fc10-4df2-9be0-6b1d7dfa2b34.lovable.app";
    private LinearLayout root;
    private long engineDownloadId = -1L;
    private Uri pendingInstallUri;
    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
                if (id != engineDownloadId) return;
                DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                try (Cursor cursor = manager.query(new DownloadManager.Query().setFilterById(id))) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Uri apkUri = manager.getUriForDownloadedFile(id);
                            if (apkUri != null) beginInstall(apkUri);
                        } else {
                            Toast.makeText(MainActivity.this, "VM engine download failed. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Could not verify the downloaded engine APK.", Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(9, 14, 28));
        getWindow().setNavigationBarColor(Color.rgb(9, 14, 28));
        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        buildHome();
    }

    private void buildHome() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(30), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(9, 14, 28));

        TextView logo = new TextView(this);
        logo.setText("◈  DroidSpace VM");
        logo.setTextColor(Color.rgb(115, 220, 190));
        logo.setTextSize(29);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(logo);

        TextView subtitle = new TextView(this);
        subtitle.setText("Virtual machine launcher");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(15);
        subtitle.setPadding(0, dp(6), 0, dp(28));
        root.addView(subtitle);

        TextView cardTitle = new TextView(this);
        cardTitle.setText("REAL VM ENGINE");
        cardTitle.setTextColor(Color.WHITE);
        cardTitle.setTextSize(17);
        cardTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(cardTitle);

        TextView detail = new TextView(this);
        detail.setText("DroidSpace connects to QubeVM, a QEMU-based Android virtual machine engine. Install the engine once, then use Start VM here.");
        detail.setTextColor(Color.rgb(205, 214, 230));
        detail.setTextSize(14);
        detail.setLineSpacing(dp(3), 1.0f);
        detail.setPadding(0, dp(10), 0, dp(20));
        root.addView(detail);

        TextView deviceInfo = new TextView(this);
        deviceInfo.setText("Phone architecture: " + getHostArchitecture());
        deviceInfo.setTextColor(Color.rgb(115, 220, 190));
        deviceInfo.setTextSize(13);
        deviceInfo.setPadding(0, 0, 0, dp(12));
        root.addView(deviceInfo);

        Button start = new Button(this);
        start.setText("START VIRTUAL MACHINE");
        start.setAllCaps(false);
        start.setTextSize(16);
        start.setOnClickListener(v -> startVm());
        root.addView(start, matchWrap());

        Button install = new Button(this);
        install.setText("Install / Get VM Engine");
        install.setAllCaps(false);
        install.setOnClickListener(v -> openEngineDownload());
        LinearLayout.LayoutParams buttonParams = matchWrap();
        buttonParams.topMargin = dp(8);
        root.addView(install, buttonParams);

        Button setup = new Button(this);
        setup.setText("How to Set Up a Guest OS");
        setup.setAllCaps(false);
        setup.setOnClickListener(v -> showGuestSetupGuide());
        LinearLayout.LayoutParams setupParams = matchWrap();
        setupParams.topMargin = dp(8);
        root.addView(setup, setupParams);

        TextView note = new TextView(this);
        note.setText("Compatibility note: the official QubeVM ARM build requires Android 11 or newer and a 64-bit ARM (arm64-v8a) phone. It does not provide an ARMv7/32-bit host build. Performance depends on the guest OS image and phone hardware. The engine is still a separate app.");
        note.setTextColor(Color.rgb(175, 185, 205));
        note.setTextSize(12);
        note.setPadding(0, dp(14), 0, dp(26));
        root.addView(note);

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(48, 58, 78));
        root.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        TextView simTitle = new TextView(this);
        simTitle.setText("DESIGN PREVIEW");
        simTitle.setTextColor(Color.WHITE);
        simTitle.setTextSize(16);
        simTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        simTitle.setPadding(0, dp(22), 0, dp(8));
        root.addView(simTitle);

        TextView simDetail = new TextView(this);
        simDetail.setText("Open the Android-style visual simulator. This is a UI preview, not the guest operating system.");
        simDetail.setTextColor(Color.LTGRAY);
        simDetail.setTextSize(13);
        root.addView(simDetail);

        Button simulator = new Button(this);
        simulator.setText("Open Design Preview");
        simulator.setAllCaps(false);
        simulator.setOnClickListener(v -> openSimulator());
        LinearLayout.LayoutParams simParams = matchWrap();
        simParams.topMargin = dp(10);
        root.addView(simulator, simParams);

        TextView footer = new TextView(this);
        footer.setText("DroidSpace VM • Early development build");
        footer.setTextColor(Color.GRAY);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(26), 0, dp(6));
        root.addView(footer);

        scroll.addView(root);
        setContentView(scroll);
    }

    private String getHostArchitecture() {
        String[] abis = android.os.Build.SUPPORTED_ABIS;
        if (abis == null || abis.length == 0) return "Unknown";
        StringBuilder result = new StringBuilder();
        for (String abi : abis) {
            if (result.length() > 0) result.append(", ");
            result.append(abi);
        }
        return result.toString();
    }

    private void startVm() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(QUBEVm_PACKAGE);
        if (launch != null) {
            try { startActivity(launch); }
            catch (Exception e) {
                Toast.makeText(this, "Could not open VM engine.", Toast.LENGTH_LONG).show();
            }
        } else {
            new android.app.AlertDialog.Builder(this)
                .setTitle("VM engine is not installed")
                .setMessage("Install QubeVM first. It is a separate QEMU-based VM app; after installation, return here and tap Start Virtual Machine.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Get engine", (dialog, which) -> openEngineDownload())
                .show();
        }
    }

    private void beginInstall(Uri apkUri) {
        pendingInstallUri = apkUri;
        if (android.os.Build.VERSION.SDK_INT >= 26
                && !getPackageManager().canRequestPackageInstalls()) {
            Toast.makeText(this, "Allow DroidSpace to install this downloaded APK, then return here.", Toast.LENGTH_LONG).show();
            Intent settings = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + getPackageName()));
            try { startActivity(settings); }
            catch (Exception e) {
                Toast.makeText(this, "Open Settings and allow installing unknown apps for DroidSpace.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        launchInstaller(apkUri);
    }

    @Override protected void onResume() {
        super.onResume();
        if (pendingInstallUri != null
                && (android.os.Build.VERSION.SDK_INT < 26
                || getPackageManager().canRequestPackageInstalls())) {
            Uri uri = pendingInstallUri;
            pendingInstallUri = null;
            launchInstaller(uri);
        }
    }

    private void launchInstaller(Uri apkUri) {
        try {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(install);
        } catch (Exception e) {
            Toast.makeText(this, "Download complete. Open Downloads and install QubeVM-ARM64.apk.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean supportsOfficialEngine() {
        if (android.os.Build.VERSION.SDK_INT < 30) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Android version not supported")
                .setMessage("The official QubeVM engine requires Android 11 or newer.")
                .setPositiveButton("OK", null)
                .show();
            return false;
        }
        for (String abi : android.os.Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi)) return true;
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("64-bit ARM is required")
            .setMessage("The official QubeVM engine available here is for arm64-v8a. This phone does not report that ABI, so installing this engine may fail. I will not download an incompatible APK.")
            .setPositiveButton("OK", null)
            .show();
        return false;
    }

    private void openEngineDownload() {
        if (!supportsOfficialEngine()) return;
        if (getPackageManager().getLaunchIntentForPackage(QUBEVm_PACKAGE) != null) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("VM engine already installed")
                .setMessage("QubeVM is installed. Tap Start Virtual Machine to open it.")
                .setPositiveButton("Start VM", (dialog, which) -> startVm())
                .setNegativeButton("Cancel", null)
                .show();
            return;
        }
        try {
            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(QUBEVm_APK));
            request.setTitle("DroidSpace VM Engine");
            request.setDescription("Downloading the official QubeVM ARM64 engine APK");
            request.setMimeType("application/vnd.android.package-archive");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(false);
            request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,
                    "QubeVM-ARM64.apk");
            engineDownloadId = manager.enqueue(request);
            Toast.makeText(this, "Engine download started. Check the notification shade for progress.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Download could not start")
                .setMessage("Please check your internet connection and try again. You can also download the official ARM64 APK from the QubeVM release page.")
                .setPositiveButton("Open release page", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/rhn-1k/QubeVM/releases/tag/7.1.3")));
                    } catch (Exception ignored) {
                        Toast.makeText(this, "Could not open the release page.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
    }

    private void showGuestSetupGuide() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Set up your virtual machine")
            .setMessage(
                "1. Install the QubeVM engine.\n\n" +
                "2. Tap START VIRTUAL MACHINE to open QubeVM.\n\n" +
                "3. In QubeVM, create a new virtual machine and choose a guest OS image that matches the virtual hardware. You must obtain the OS image separately; DroidSpace does not bundle Android or Windows images.\n\n" +
                "4. Set a modest RAM allocation first, save the VM, then start it. First boot can take several minutes.\n\n" +
                "Important: emulation is demanding and may be slow on phones without hardware acceleration. Keep enough free storage and stop if the phone overheats.")
            .setPositiveButton("Open QubeVM guide", (dialog, which) -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/rhn-1k/QubeVM")));
                } catch (Exception ignored) {
                    Toast.makeText(this, "Could not open the QubeVM guide.", Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void openSimulator() {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(SIMULATOR_URL))); }
        catch (Exception e) {
            Toast.makeText(this, "Could not open preview.", Toast.LENGTH_LONG).show();
        }
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
