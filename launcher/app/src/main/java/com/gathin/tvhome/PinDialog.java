package com.gathin.tvhome;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Local addition: a D-pad friendly PIN pad for the System apps folder (system_apps_pin in config.xml).
 * Digits come from the on-screen pad or the remote's number keys; the PIN is checked as soon as it is long enough.
 * This keeps family members out of the folder; it is not a security boundary (the PIN ships inside the APK).
 */
final class PinDialog {
    private PinDialog() { }

    static void show(Activity activity, String pin, Runnable unlocked) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        float density = activity.getResources().getDisplayMetrics().density;
        StringBuilder typed = new StringBuilder();

        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        int pad = (int) (24 * density);
        panel.setPadding(pad, pad, pad, pad);
        panel.setBackgroundResource(R.drawable.glass_panel);

        TextView title = new TextView(activity);
        title.setText(R.string.pin_title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        panel.addView(title);

        TextView dots = new TextView(activity);
        dots.setTextColor(Color.WHITE);
        dots.setTextSize(30);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, (int) (8 * density), 0, (int) (12 * density));
        dots.setText(mask(0, pin.length()));
        panel.addView(dots);

        Runnable check = () -> {
            dots.setText(mask(typed.length(), pin.length()));
            if (typed.length() < pin.length()) return;
            if (typed.toString().equals(pin)) {
                dialog.dismiss();
                unlocked.run();
            } else {
                typed.setLength(0);
                dots.setText(activity.getString(R.string.pin_wrong));
            }
        };

        GridLayout keys = new GridLayout(activity);
        keys.setColumnCount(3);
        String[] labels = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "✕"};
        TextView first = null;
        for (String label : labels) {
            TextView key = new TextView(activity, null, 0, R.style.TopButton);
            key.setText(label);
            key.setGravity(Gravity.CENTER);
            key.setFocusable(true);
            key.setOnClickListener(v -> {
                if ("✕".equals(label)) {
                    dialog.dismiss();
                } else if ("⌫".equals(label)) {
                    if (typed.length() > 0) typed.setLength(typed.length() - 1);
                    dots.setText(mask(typed.length(), pin.length()));
                } else if (typed.length() < pin.length()) {
                    typed.append(label);
                    check.run();
                }
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = (int) (72 * density);
            lp.height = (int) (52 * density);
            int m = (int) (6 * density);
            lp.setMargins(m, m, m, m);
            keys.addView(key, lp);
            if (first == null) first = key;
        }
        panel.addView(keys);

        dialog.setContentView(panel);
        dialog.setOnKeyListener((d, code, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) {
                if (typed.length() < pin.length()) {
                    typed.append((char) ('0' + code - KeyEvent.KEYCODE_0));
                    check.run();
                }
                return true;
            }
            if (code == KeyEvent.KEYCODE_DEL && typed.length() > 0) {
                typed.setLength(typed.length() - 1);
                dots.setText(mask(typed.length(), pin.length()));
                return true;
            }
            return false;
        });
        Window window = dialog.getWindow();
        if (window != null) window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        if (first != null) first.requestFocus();
    }

    private static String mask(int filled, int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) s.append(i < filled ? "● " : "○ ");
        return s.toString().trim();
    }
}
