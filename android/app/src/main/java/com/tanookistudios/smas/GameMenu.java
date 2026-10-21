package com.tanookistudios.smas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import org.libsdl.app.SDLActivity;

/**
 * The in-game menu on Android (the desktop Esc menu draws with desktop OpenGL in a hidden window,
 * which Android cannot have). The option rows come from the game itself (smas_android.c), so cheats
 * and sound switches match the desktop menu and save to the same smas.ini. Opening it pauses the game.
 */
public class GameMenu extends FrameLayout {
    static native String[] nativeRows();
    static native int nativeGet(String key);
    static native void nativeSet(String key, int value);
    static native String nativeAboutText();

    private final SmasActivity activity;
    private final float dp;

    public GameMenu(SmasActivity activity) {
        super(activity);
        this.activity = activity;
        dp = getResources().getDisplayMetrics().density;
        setBackgroundColor(Color.argb(170, 0, 0, 0));
        setVisibility(GONE);
        setClickable(true);                         // the game underneath gets no touches while it is open
    }

    public boolean isOpen() { return getVisibility() == VISIBLE; }

    public void open() {
        if (isOpen()) return;
        build();
        setVisibility(VISIBLE);
        hotkey(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_P);   // Pause (config.ini [KeyMap] Pause = Shift+p)
    }

    public void close() {
        if (!isOpen()) return;
        setVisibility(GONE);
        hotkey(KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_P);   // resume
    }

    /** Presses modifier+key the way a keyboard would, so the host's shortcuts ([KeyMap]) apply. */
    static void hotkey(int modifier, int key) {
        if (modifier != 0) SDLActivity.onNativeKeyDown(modifier);
        SDLActivity.onNativeKeyDown(key);
        SDLActivity.onNativeKeyUp(key);
        if (modifier != 0) SDLActivity.onNativeKeyUp(modifier);
    }

    private void build() {
        removeAllViews();
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * dp);
        card.setPadding(pad, pad, pad, pad);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.rgb(20, 18, 40));
        bg.setCornerRadius(16 * dp);
        bg.setStroke((int) (1 * dp), Color.rgb(120, 90, 220));
        card.setBackground(bg);

        card.addView(title("Super Mario All-Stars", 22));
        LinearLayout row = new LinearLayout(getContext());
        row.addView(button("Resume", v -> close()));
        row.addView(button("Save / load", v -> {
            close();
            hotkey(0, KeyEvent.KEYCODE_F11);          // the save-state browser ([KeyMap] SaveStateMenu = F11)
        }));
        row.addView(button("Reset", v -> confirm("Reset the game? Anything not saved is lost.", () -> {
            close();
            hotkey(KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_R);   // [KeyMap] Reset = Ctrl+r
        })));
        row.addView(button("Quit", v -> confirm("Close the game?", activity::finish)));
        card.addView(row);

        // Cheats first, then the sound switches; a description shared by a run of rows is shown once.
        String[] rows = nativeRows();
        for (String wanted : new String[] { "Cheats", "Audio" }) {
            String lastNote = null;
            boolean headed = false;
            for (String line : rows) {
                String[] f = line.split("\t", -1);
                if (f.length < 4 || !f[1].equals(wanted)) continue;
                if (!headed) { card.addView(heading(wanted.equals("Audio") ? "Sound" : wanted)); headed = true; }
                if (!f[3].isEmpty() && !f[3].equals(lastNote) && wanted.equals("Audio")) card.addView(note(f[3]));
                final String key = f[0];
                card.addView(toggle(f[2], nativeGet(key) != 0, on -> nativeSet(key, on ? 1 : 0)),
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                if (!f[3].isEmpty() && !wanted.equals("Audio")) card.addView(note(f[3]));
                lastNote = f[3];
            }
        }

        card.addView(heading("About"));
        for (String para : nativeAboutText().split("\n\n")) card.addView(note(para, 15, Color.rgb(225, 225, 240)));
        ImageView sig = new ImageView(getContext());
        sig.setImageResource(R.drawable.signature);
        sig.setAdjustViewBounds(true);
        card.addView(sig, new LinearLayout.LayoutParams((int) (200 * dp), LinearLayout.LayoutParams.WRAP_CONTENT));
        card.addView(button("Leave a Tip", v ->
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://tanookistudios.com/SupportMe")))));

        ScrollView scroll = new ScrollView(getContext());
        scroll.addView(card);
        int w = Math.min(getResources().getDisplayMetrics().widthPixels - (int) (32 * dp), (int) (620 * dp));
        LayoutParams lp = new LayoutParams(w, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        lp.topMargin = lp.bottomMargin = (int) (16 * dp);
        addView(scroll, lp);
    }

    private Switch toggle(String label, boolean on, java.util.function.Consumer<Boolean> changed) {
        Switch s = new Switch(getContext());
        s.setText(label);
        s.setTextColor(Color.WHITE);
        s.setTextSize(16);
        s.setChecked(on);
        int[][] states = { { android.R.attr.state_checked }, {} };
        s.setThumbTintList(new android.content.res.ColorStateList(states, new int[] { Color.rgb(190, 160, 255), Color.rgb(200, 200, 210) }));
        s.setTrackTintList(new android.content.res.ColorStateList(states, new int[] { Color.rgb(120, 90, 220), Color.rgb(90, 90, 110) }));
        s.setPadding(0, (int) (6 * dp), 0, (int) (2 * dp));
        s.setOnCheckedChangeListener((b, v) -> changed.accept(v));
        return s;
    }

    private TextView title(String text, int size) {
        TextView t = new TextView(getContext());
        t.setText(text);
        t.setTextColor(Color.WHITE);
        t.setTextSize(size);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, 0, 0, (int) (8 * dp));
        return t;
    }

    private TextView heading(String text) {
        TextView t = title(text.toUpperCase(), 13);
        t.setTextColor(Color.rgb(170, 140, 255));
        t.setLetterSpacing(0.15f);
        t.setPadding(0, (int) (18 * dp), 0, (int) (6 * dp));
        return t;
    }

    private TextView note(String text) { return note(text, 13, Color.rgb(160, 160, 185)); }

    private TextView note(String text, int size, int color) {
        TextView t = new TextView(getContext());
        t.setText(text);
        t.setTextColor(color);
        t.setTextSize(size);
        t.setPadding(0, (int) (2 * dp), 0, (int) (10 * dp));
        return t;
    }

    private Button button(String text, View.OnClickListener click) {
        Button b = new Button(getContext());
        b.setText(text);
        b.setAllCaps(false);
        b.setOnClickListener(click);
        return b;
    }

    private void confirm(String question, Runnable yes) {
        new AlertDialog.Builder(getContext())
                .setMessage(question)
                .setPositiveButton("Yes", (d, w) -> yes.run())
                .setNegativeButton("No", null)
                .show();
    }
}
