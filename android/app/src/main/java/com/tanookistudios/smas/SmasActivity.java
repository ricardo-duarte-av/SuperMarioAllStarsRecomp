package com.tanookistudios.smas;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.libsdl.app.SDLActivity;

/** The game's activity. SDL is linked statically into libmain.so, so that is the only library to load. */
public class SmasActivity extends SDLActivity {
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
