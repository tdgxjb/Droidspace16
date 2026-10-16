package com.droidspace16.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.provider.OpenableColumns;

/**
 * Local VM profile preferences. These settings are saved on the host device.
 * They are not applied to QubeVM until a supported engine bridge is integrated.
 */
public class VmProfileActivity extends Activity {
    private static final int PICK_GUEST_APK = 4201;
    private static final int[] RAM_MB = {512, 768, 1024, 1536, 2048};
    private static final int[] CPU_CORES = {1, 2, 3, 4};
    private SharedPreferences prefs;
    private LinearLayout root;
    private TextView profileSummary;
    private TextView apkSummary;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("droidspace_vm", MODE_PRIVATE);
        getWindow().setStatusBarColor(Color.rgb(9, 14, 28));
        getWindow().setNavigationBarColor(Color.rgb(9, 14, 28));
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(26), dp(22), dp(24));
        root.setBackgroundColor(Color.rgb(9, 14, 28));

        TextView title = text("VM Profiles & Tools", 26, Color.rgb(115, 220, 190), true);
        root.addView(title);
        TextView intro = text("Save preferred VM settings and choose an APK file for later use.", 14, Color.LTGRAY, false);
        intro.setPadding(0, dp(8), 0, dp(20));
        root.addView(intro);

        TextView profileTitle = text("DEFAULT VM PROFILE", 15, Color.WHITE, true);
        root.addView(profileTitle);
        profileSummary = text("", 14, Color.rgb(205, 214, 230), false);
        profileSummary.setPadding(0, dp(8), 0, dp(10));
        root.addView(profileSummary);

        addButton("Guest RAM", v -> chooseOption("guest_ram_mb", "Choose guest RAM", RAM_MB,
                value -> value + " MB"));
        addButton("Guest CPU cores", v -> chooseOption("guest_cpu_cores", "Choose guest CPU cores", CPU_CORES,
                value -> value + (value == 1 ? " core" : " cores")));
        addButton("Profile name", v -> editProfileName());
        addButton("Reset profile defaults", v -> new AlertDialog.Builder(this)
                .setTitle("Reset VM profile?")
                .setMessage("This resets the saved RAM, CPU and profile name on this device only.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Reset", (d, w) -> {
                    prefs.edit().remove("guest_ram_mb").remove("guest_cpu_cores")
                            .remove("vm_profile_name").apply();
                    refreshSummary();
                    Toast.makeText(this, "Profile defaults restored.", Toast.LENGTH_SHORT).show();
                }).show());

        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(48, 58, 78));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(-1, dp(1));
        dividerParams.topMargin = dp(20);
        dividerParams.bottomMargin = dp(20);
        root.addView(divider, dividerParams);

        root.addView(text("GUEST APK FILE", 15, Color.WHITE, true));
        apkSummary = text("", 13, Color.rgb(205, 214, 230), false);
        apkSummary.setPadding(0, dp(8), 0, dp(8));
        root.addView(apkSummary);
        addButton("Choose APK file", v -> chooseApk());
        addButton("Clear selected APK", v -> {
            prefs.edit().remove("guest_apk_uri").apply();
            refreshApkSummary();
            Toast.makeText(this, "Selected APK reference cleared.", Toast.LENGTH_SHORT).show();
        });

        TextView note = text(
                "Important: these are saved local preferences only. The current QubeVM launcher integration does not expose a verified API for applying RAM/CPU settings or installing this APK inside the guest. Open QubeVM to configure and install files there manually. Choosing an APK here does not install it.",
                12, Color.rgb(175, 185, 205), false);
        note.setPadding(0, dp(18), 0, dp(10));
        root.addView(note);

        addButton("Back to DroidSpace", v -> finish());
        refreshSummary();
        refreshApkSummary();
        scroll.addView(root);
        setContentView(scroll);
    }

    private interface ValueLabel { String label(int value); }

    private void chooseOption(String key, String title, int[] options, ValueLabel labels) {
        String[] choices = new String[options.length];
        int selected = 0;
        int current = prefs.getInt(key, options[0]);
        for (int i = 0; i < options.length; i++) {
            choices[i] = labels.label(options[i]);
            if (options[i] == current) selected = i;
        }
        new AlertDialog.Builder(this).setTitle(title).setSingleChoiceItems(choices, selected, (dialog, which) -> {
            prefs.edit().putInt(key, options[which]).apply();
            dialog.dismiss();
            refreshSummary();
        }).setNegativeButton("Cancel", null).show();
    }

    private void editProfileName() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setSingleLine(true);
        input.setText(prefs.getString("vm_profile_name", "My VM"));
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(this).setTitle("Profile name").setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "My VM";
                    if (name.length() > 40) name = name.substring(0, 40);
                    prefs.edit().putString("vm_profile_name", name).apply();
                    refreshSummary();
                }).show();
    }

    private void chooseApk() {
        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("application/vnd.android.package-archive");
        picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try { startActivityForResult(picker, PICK_GUEST_APK); }
        catch (Exception e) { Toast.makeText(this, "Could not open file picker.", Toast.LENGTH_LONG).show(); }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_GUEST_APK || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) { }
        prefs.edit().putString("guest_apk_uri", uri.toString()).apply();
        refreshApkSummary();
        Toast.makeText(this, "APK reference saved. It has not been installed in the guest.", Toast.LENGTH_LONG).show();
    }

    private void refreshSummary() {
        String name = prefs.getString("vm_profile_name", "My VM");
        int ram = prefs.getInt("guest_ram_mb", 1024);
        int cpu = prefs.getInt("guest_cpu_cores", 1);
        profileSummary.setText(name + "\nRAM preference: " + ram + " MB\nCPU preference: " + cpu
                + (cpu == 1 ? " core" : " cores") + "\nSaved on this device");
    }

    private void refreshApkSummary() {
        String saved = prefs.getString("guest_apk_uri", null);
        if (saved == null) {
            apkSummary.setText("No APK selected.");
            return;
        }
        Uri uri = Uri.parse(saved);
        String name = uri.getLastPathSegment();
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) name = cursor.getString(index);
            }
        } catch (Exception ignored) { }
        apkSummary.setText("Selected: " + (name == null ? "Unknown file" : name) + "\nReference saved; guest installation is not yet connected.");
    }

    private void addButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(5);
        root.addView(button, params);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
