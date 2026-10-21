package com.tanookistudios.smas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.input.InputManager;
import android.view.HapticFeedbackConstants;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import org.libsdl.app.SDLActivity;

/**
 * On-screen SNES controller drawn over the game. Every control presses the same key the desktop
 * keyboard uses (README "Controls"), through SDL's own key path, so the game needs no changes:
 * D-pad = arrows, B = Z, A = X, Y = A, X = S, L = C, R = V, Start = Enter, Select = Right Shift.
 * The menu button opens GameMenu.
 *
 * Multitouch: each finger presses whatever is under it and follows it as it slides. Between B and Y
 * (and A and X) a finger presses both, for running jumps. The controls hide while a game controller
 * is connected and come back when the screen is touched.
 */
public class TouchControlsView extends View implements InputManager.InputDeviceListener {
    private static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3, B = 4, A = 5, Y = 6, X = 7,
            L = 8, R = 9, START = 10, SELECT = 11, MENU = 12, COUNT = 13;
    private static final int[] KEYS = {
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_Z, KeyEvent.KEYCODE_X, KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_S,
        KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_V, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_SHIFT_RIGHT,
        KeyEvent.KEYCODE_ESCAPE,
    };
    private static final String[] LABELS = { "", "", "", "", "B", "A", "Y", "X", "L", "R", "START", "SELECT", "☰" };

    private final float[] cx = new float[COUNT], cy = new float[COUNT], rad = new float[COUNT];
    private float padX, padY, padR;                 // D-pad centre and radius
    private final boolean[] down = new boolean[COUNT];
    private final int[] heldBy = new int[COUNT];    // how many fingers hold each control
    private final int[][] fingers = new int[10][];  // per pointer id: the controls it holds
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG), edge = new Paint(Paint.ANTI_ALIAS_FLAG),
            text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean hiddenForController;

    public TouchControlsView(Context context) {
        super(context);
        edge.setStyle(Paint.Style.STROKE);
        edge.setColor(Color.argb(150, 255, 255, 255));
        text.setColor(Color.argb(200, 255, 255, 255));
        text.setTextAlign(Paint.Align.CENTER);
        text.setFakeBoldText(true);
        InputManager im = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        if (im != null) im.registerInputDeviceListener(this, null);
        hiddenForController = controllerConnected();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        float u = Math.min(w, h) / 100f;            // one "unit" = 1% of the short side
        edge.setStrokeWidth(u * 0.4f);
        padR = u * 17; padX = u * 4 + padR; padY = h - u * 6 - padR;
        float fr = u * 7.5f, fx = w - u * 4 - u * 24, fy = h - u * 6 - u * 24;   // face-button diamond centre
        put(Y, fx - u * 15, fy, fr); put(A, fx + u * 15, fy, fr);
        put(X, fx, fy - u * 15, fr); put(B, fx, fy + u * 15, fr);
        // Side columns, top to bottom: L / menu / Select on the left, R / Start on the right.
        float left = u * 4 + u * 10, right = w - u * 4 - u * 10;
        put(L, left, u * 12, u * 7); put(R, right, u * 12, u * 7);
        put(MENU, left, u * 31, u * 6);
        put(SELECT, left, u * 46, u * 5.5f); put(START, right, u * 31, u * 5.5f);
        text.setTextSize(u * 5);
    }

    private void put(int c, float x, float y, float r) { cx[c] = x; cy[c] = y; rad[c] = r; }

    /** The controls a finger at (x, y) presses. */
    private int[] hit(float x, float y) {
        int[] out = new int[4];
        int n = 0;
        float dx = x - padX, dy = y - padY;
        if (dx * dx + dy * dy <= padR * padR * 1.5f && Math.hypot(dx, dy) > padR * 0.18f) {
            double ang = Math.toDegrees(Math.atan2(dy, dx));        // 0 = right, 90 = down
            if (ang > 22.5 && ang < 157.5) out[n++] = DOWN;
            if (ang < -22.5 && ang > -157.5) out[n++] = UP;
            if (Math.abs(ang) < 67.5) out[n++] = RIGHT;
            if (Math.abs(ang) > 112.5) out[n++] = LEFT;
            return java.util.Arrays.copyOf(out, n);
        }
        for (int c = B; c < COUNT && n < 2; c++) {
            float r = rad[c] * (c <= X ? 1.25f : 1.15f);              // face buttons overlap a little
            float ex = x - cx[c], ey = y - cy[c];
            if (ex * ex + ey * ey <= r * r) out[n++] = c;
        }
        return java.util.Arrays.copyOf(out, n);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (hiddenForController) { hiddenForController = false; invalidate(); }
        int action = e.getActionMasked();
        for (int i = 0; i < e.getPointerCount(); i++) {
            int id = e.getPointerId(i);
            if (id >= fingers.length) continue;
            boolean lifted = (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                    || (action == MotionEvent.ACTION_POINTER_UP && i == e.getActionIndex());
            assign(id, lifted ? new int[0] : hit(e.getX(i), e.getY(i)));
        }
        return true;
    }

    /** Moves finger `id` onto `now`: releases what it left, presses what it reached. */
    private void assign(int id, int[] now) {
        int[] before = fingers[id] == null ? new int[0] : fingers[id];
        for (int c : before) if (!contains(now, c) && --heldBy[c] == 0) set(c, false);
        for (int c : now) if (!contains(before, c) && heldBy[c]++ == 0) set(c, true);
        fingers[id] = now;
    }

    private static boolean contains(int[] a, int v) { for (int x : a) if (x == v) return true; return false; }

    private void set(int c, boolean on) {
        if (c == MENU) {                            // the menu button opens the in-game menu (GameMenu)
            down[c] = false;
            if (on && getContext() instanceof SmasActivity && ((SmasActivity) getContext()).menu != null)
                ((SmasActivity) getContext()).menu.open();
            invalidate();
            return;
        }
        down[c] = on;
        if (on) { SDLActivity.onNativeKeyDown(KEYS[c]); performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY); }
        else SDLActivity.onNativeKeyUp(KEYS[c]);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (hiddenForController) return;
        // D-pad: a plus shape; the pressed arms light up.
        float arm = padR * 0.36f;
        RectF h = new RectF(padX - padR, padY - arm, padX + padR, padY + arm);
        RectF v = new RectF(padX - arm, padY - padR, padX + arm, padY + padR);
        fill.setColor(Color.argb(90, 20, 20, 30));
        canvas.drawRoundRect(h, arm * 0.4f, arm * 0.4f, fill); canvas.drawRoundRect(v, arm * 0.4f, arm * 0.4f, fill);
        canvas.drawRoundRect(h, arm * 0.4f, arm * 0.4f, edge); canvas.drawRoundRect(v, arm * 0.4f, arm * 0.4f, edge);
        fill.setColor(Color.argb(150, 255, 255, 255));
        if (down[UP]) canvas.drawRect(padX - arm, padY - padR, padX + arm, padY - arm, fill);
        if (down[DOWN]) canvas.drawRect(padX - arm, padY + arm, padX + arm, padY + padR, fill);
        if (down[LEFT]) canvas.drawRect(padX - padR, padY - arm, padX - arm, padY + arm, fill);
        if (down[RIGHT]) canvas.drawRect(padX + arm, padY - arm, padX + padR, padY + arm, fill);
        // Round buttons, pill-shaped Start/Select.
        for (int c = B; c < COUNT; c++) {
            fill.setColor(down[c] ? Color.argb(170, 255, 255, 255) : Color.argb(90, 20, 20, 30));
            if (c == START || c == SELECT) {
                RectF p = new RectF(cx[c] - rad[c] * 1.7f, cy[c] - rad[c] * 0.6f, cx[c] + rad[c] * 1.7f, cy[c] + rad[c] * 0.6f);
                canvas.drawRoundRect(p, rad[c] * 0.6f, rad[c] * 0.6f, fill);
                canvas.drawRoundRect(p, rad[c] * 0.6f, rad[c] * 0.6f, edge);
                float ts = text.getTextSize(); text.setTextSize(ts * 0.55f);
                canvas.drawText(LABELS[c], cx[c], cy[c] + text.getTextSize() * 0.55f * 0.35f, text);
                text.setTextSize(ts);
            } else {
                canvas.drawCircle(cx[c], cy[c], rad[c], fill);
                canvas.drawCircle(cx[c], cy[c], rad[c], edge);
                canvas.drawText(LABELS[c], cx[c], cy[c] + text.getTextSize() * 0.35f, text);
            }
        }
    }

    // --- hide while a game controller is connected ---------------------------------------------
    private static boolean controllerConnected() {
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice d = InputDevice.getDevice(id);
            if (d != null && !d.isVirtual() && ((d.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (d.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) return true;
        }
        return false;
    }
    private void refreshController() {
        boolean c = controllerConnected();
        if (c != hiddenForController) { hiddenForController = c; invalidate(); }
    }
    @Override public void onInputDeviceAdded(int id) { refreshController(); }
    @Override public void onInputDeviceRemoved(int id) { refreshController(); }
    @Override public void onInputDeviceChanged(int id) { refreshController(); }
}
