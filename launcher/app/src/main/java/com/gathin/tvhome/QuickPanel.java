package com.gathin.tvhome;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Local addition: an English stand-in for the Hisense laser TV's Chinese quick panel (com.hisense.quickmenu).
 * Every tile opens the firmware's own screen for that job, found on a Vidda laser TV (LASER_MICRO_3DTOF,
 * Android 14, firmware 07.02.70.202512281739). "Turn off light source" is missing: the firmware exposes no
 * intent for it.
 */
final class QuickPanel {
    private static final String SETTINGS_PKG = "com.android.tv.settings";
    private static final String LASER_KEYS = "com.hisense.systemsettings.LaserCustomKeyService";

    private QuickPanel() { }

    static void show(Activity activity, Runnable myApps, Runnable inputs) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        float density = activity.getResources().getDisplayMetrics().density;

        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (24 * density);
        panel.setPadding(pad, (int) (18 * density), pad, pad);
        panel.setBackgroundResource(R.drawable.glass_panel);

        TextView title = new TextView(activity);
        title.setText(R.string.quick);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setPadding((int) (6 * density), 0, 0, (int) (12 * density));
        panel.addView(title);

        GridLayout grid = new GridLayout(activity);
        grid.setColumnCount(5);
        Object[][] tiles = {
                {R.string.qp_my_apps, R.drawable.ic_apps_pill, (Runnable) myApps},
                {R.string.qp_inputs, R.drawable.ic_input, (Runnable) inputs},
                {R.string.qp_network, R.drawable.ic_ethernet, service(activity, SETTINGS_PKG, LASER_KEYS,
                        "com.hisense.action.laser_custom_key.network_setting")},
                {R.string.qp_bluetooth, R.drawable.ic_q_bluetooth, action(activity, "com.hisense.bluetoothsettings_devices")},
                {R.string.qp_mirroring, R.drawable.ic_q_cast, app(activity, "com.hisense.multiscreen.screenhelper")},
                {R.string.qp_projector, R.drawable.ic_settings, action(activity, "android.settings.SETTINGS")},
                {R.string.qp_keystone, R.drawable.ic_q_keystone, action(activity, "com.hisense.screen.keystone")},
                {R.string.qp_focus, R.drawable.ic_q_focus, action(activity, "com.hisense.screen.ElectricFocusing")},
                {R.string.qp_picture, R.drawable.ic_picture, service(activity, SETTINGS_PKG, LASER_KEYS,
                        "com.hisense.action.laser_custom_key.image_mode")},
                {R.string.qp_sound, R.drawable.ic_q_sound, service(activity, SETTINGS_PKG, LASER_KEYS,
                        "com.hisense.action.laser_custom_key.sound_mode")},
        };
        TextView first = null;
        for (Object[] tile : tiles) {
            TextView view = new TextView(activity, null, 0, R.style.TopButton);
            view.setText((Integer) tile[0]);
            view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, (Integer) tile[1], 0, 0);
            view.setCompoundDrawableTintList(activity.getResources().getColorStateList(R.drawable.pill_text, activity.getTheme()));
            view.setGravity(Gravity.CENTER);
            view.setMaxLines(2);
            view.setTextSize(14);
            view.setPadding((int) (8 * density), (int) (14 * density), (int) (8 * density), (int) (10 * density));
            Runnable run = (Runnable) tile[2];
            view.setOnClickListener(v -> {
                dialog.dismiss();
                try {
                    run.run();
                } catch (RuntimeException e) {
                    Toast.makeText(activity, R.string.not_available, Toast.LENGTH_SHORT).show();
                }
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = (int) (150 * density);
            lp.height = (int) (104 * density);
            int m = (int) (6 * density);
            lp.setMargins(m, m, m, m);
            grid.addView(view, lp);
            if (first == null) first = view;
        }
        panel.addView(grid);

        dialog.setContentView(panel);
        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        if (first != null) first.requestFocus();
    }

    private static Runnable action(Activity activity, String action) {
        return () -> activity.startActivity(new Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private static Runnable app(Activity activity, String pkg) {
        return () -> {
            Intent i = activity.getPackageManager().getLaunchIntentForPackage(pkg);
            if (i == null) i = activity.getPackageManager().getLeanbackLaunchIntentForPackage(pkg);
            if (i == null) throw new IllegalStateException("not installed: " + pkg);
            activity.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        };
    }

    /** The firmware's own pop-ups are services; an idle app's service only starts as a foreground one. */
    static Runnable service(Activity activity, String pkg, String cls, String action) {
        return () -> {
            Intent i = new Intent(action).setComponent(new ComponentName(pkg, cls));
            try {
                if (activity.startService(i) != null) return;
            } catch (IllegalStateException background) {
                // fall through
            }
            activity.startForegroundService(i);
        };
    }
}
