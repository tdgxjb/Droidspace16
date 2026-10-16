package com.droidspace16.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.Intent;
import android.net.Uri;
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

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(9, 14, 28));
        getWindow().setNavigationBarColor(Color.rgb(9, 14, 28));
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

        TextView note = new TextView(this);
        note.setText("Compatibility note: QubeVM's current release requires Android 11 or newer. Performance depends on the guest OS image and phone hardware. The engine is a separate app at this stage.");
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

    private void openEngineDownload() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(QUBEVm_APK));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open download link.", Toast.LENGTH_LONG).show();
        }
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
