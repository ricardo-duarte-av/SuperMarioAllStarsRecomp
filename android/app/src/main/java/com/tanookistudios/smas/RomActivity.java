package com.tanookistudios.smas;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * First screen. The game needs the player's own Super Mario All-Stars (USA) (25th Anniversary) ROM: this asks for it once
 * with Android's file picker (a .sfc/.smc, or a .zip holding one), checks it is exactly the right dump,
 * and copies it into the app's folder as smas.sfc with a rom.cfg naming it. Every later launch goes
 * straight to the game.
 */
public class RomActivity extends Activity {
    static final String EXPECTED_SHA256 = "411c7036750fbc79d60b84602414dbe713074de8204d3c4a5ad762679c103c61";
    private static final int PICK = 1;
    private static final int ROM_SIZE = 2 * 1024 * 1024;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (romInstalled()) { startGame(); return; }
        showPicker("Pick your own Super Mario All-Stars (USA) (Super Mario All-Stars - 25th Anniversary) game file (.sfc, or a .zip with one inside). The 1993 cartridge is a different file and won't work. "
                + "No game data comes with this app.");
    }

    private File dir() { return getExternalFilesDir(null); }

    private boolean romInstalled() {
        File d = dir();
        return d != null && new File(d, "smas.sfc").length() == ROM_SIZE && new File(d, "rom.cfg").exists();
    }

    private void startGame() {
        startActivity(new Intent(this, SmasActivity.class));
        finish();
    }

    private void showPicker(String text) {
        float dp = getResources().getDisplayMetrics().density;
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(Color.rgb(16, 14, 34));
        int pad = (int) (24 * dp);
        box.setPadding(pad, pad, pad, pad);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher);
        box.addView(icon, new LinearLayout.LayoutParams((int) (72 * dp), (int) (72 * dp)));

        TextView title = new TextView(this);
        title.setText("Super Mario All-Stars");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, (int) (12 * dp), 0, (int) (8 * dp));
        box.addView(title);

        message = new TextView(this);
        message.setText(text);
        message.setTextColor(Color.rgb(205, 205, 225));
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 0, 0, (int) (16 * dp));
        box.addView(message);

        Button choose = new Button(this);
        choose.setText("Choose file");
        choose.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.setType("*/*");
            startActivityForResult(pick, PICK);
        });
        box.addView(choose);
        setContentView(box);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        try {
            byte[] rom = readRom(data.getData());
            if (rom == null) { message.setText("That file doesn't hold a SNES game. Pick the .sfc file (or a .zip with it inside)."); return; }
            if (!EXPECTED_SHA256.equals(sha256(rom))) {
                message.setText("That's not the right game file. It needs Super Mario All-Stars (USA) (Super Mario All-Stars - 25th Anniversary), not the 1993 cartridge: the one with SHA-256\n"
                        + EXPECTED_SHA256);
                return;
            }
            File d = dir();
            try (FileOutputStream o = new FileOutputStream(new File(d, "smas.sfc"))) { o.write(rom); }
            try (FileOutputStream o = new FileOutputStream(new File(d, "rom.cfg"))) { o.write("smas.sfc\n".getBytes("UTF-8")); }
            startGame();
        } catch (IOException e) {
            message.setText("Couldn't read that file: " + e.getMessage());
        }
    }

    /** The ROM bytes from a .sfc/.smc or the first such file inside a .zip; a 512-byte copier header is dropped. */
    private byte[] readRom(Uri uri) throws IOException {
        byte[] raw = readAll(getContentResolver().openInputStream(uri), 16 * 1024 * 1024);
        if (raw.length > 4 && raw[0] == 'P' && raw[1] == 'K') {
            try (ZipInputStream z = new ZipInputStream(new java.io.ByteArrayInputStream(raw))) {
                for (ZipEntry e; (e = z.getNextEntry()) != null; ) {
                    String n = e.getName().toLowerCase();
                    if (!e.isDirectory() && (n.endsWith(".sfc") || n.endsWith(".smc"))) { raw = readAll(z, 16 * 1024 * 1024); break; }
                }
            }
        }
        if (raw.length == ROM_SIZE + 512) raw = Arrays.copyOfRange(raw, 512, raw.length);
        return raw.length == ROM_SIZE ? raw : null;
    }

    private static byte[] readAll(InputStream in, int max) throws IOException {
        if (in == null) throw new IOException("no data");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[65536];
        for (int n; (n = in.read(buf)) > 0; ) {
            out.write(buf, 0, n);
            if (out.size() > max) throw new IOException("file too large");
        }
        return out.toByteArray();
    }

    static String sha256(byte[] data) {
        try {
            StringBuilder s = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(data)) s.append(String.format("%02x", b));
            return s.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
