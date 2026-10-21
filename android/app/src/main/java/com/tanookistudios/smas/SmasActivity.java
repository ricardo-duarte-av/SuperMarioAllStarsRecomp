package com.tanookistudios.smas;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.libsdl.app.SDLActivity;

/** The game's activity. SDL is linked statically into libmain.so, so that is the only library to load. */
public class SmasActivity extends SDLActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The on-screen controller sits over the game's picture, and the menu over both.
        if (mLayout != null) {
            ViewGroup.LayoutParams full = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            mLayout.addView(new TouchControlsView(this), full);
            menu = new GameMenu(this);
            mLayout.addView(menu, full);
        }
    }

    GameMenu menu;

    /** Back (gesture or button) and a controller's Home button open and close the menu. */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int k = event.getKeyCode();
        if (menu != null && (k == KeyEvent.KEYCODE_BACK || k == KeyEvent.KEYCODE_BUTTON_MODE)) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (menu.isOpen()) menu.close(); else menu.open();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    /** Full screen: hide the status and navigation bars while playing (a swipe from the edge shows them). */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) return;
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    protected String[] getLibraries() {
        return new String[] { "main" };
    }

    @Override
    protected String[] getArguments() {
        List<String> out = new ArrayList<>();
        // On Android SDL opens a RELATIVE path from inside the APK, so the host could write config.ini to the
        // app's folder but never read it back. Name it by its full path.
        File dir = getExternalFilesDir(null);
        if (dir != null) {
            out.add("--config");
            out.add(new File(dir, "config.ini").getAbsolutePath());
        }
        // Test hook: `adb shell "am start ... --es args '--script /path/route.txt'"` adds command-line words
        // (the same ones the desktop build takes). A normal launch has none.
        String args = getIntent() != null ? getIntent().getStringExtra("args") : null;
        if (args != null && !args.trim().isEmpty()) out.addAll(Arrays.asList(args.trim().split("\\s+")));
        // SDL 3.4's nativeRunMain can drop the last argument (its buffer math is exact), so end with a
        // switch that changes nothing on Android, where there is no launcher.
        out.add("--no-launcher");
        return out.toArray(new String[0]);
    }
}
