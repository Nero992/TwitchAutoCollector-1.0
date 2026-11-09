
package com.twitchautocollector.app;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

public class TwitchAccessibilityService
        extends AccessibilityService {

    private static final String TAG = "TwitchCollector";
    private static final String CHROME = "com.android.chrome";

    private static final long SCAN_MS = 2000;
    private static final long COOLDOWN_MS = 15000;

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private boolean running = false;
    private long lastClick = 0;
    private int attempts = 0;

    private final Runnable scanner = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            try {
                scanChrome();
            } catch (Exception e) {
                Log.e(TAG, "Scan error", e);
            }

            handler.postDelayed(this, SCAN_MS);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        running = true;
        handler.removeCallbacks(scanner);
        handler.post(scanner);

        Log.i(TAG, "Chrome collector started");
    }

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event) {
        // Periodic scanner handles detection.
    }

    private void scanChrome() {
        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) return;

        try {
            CharSequence pkg = root.getPackageName();

            if (pkg == null ||
                    !CHROME.contentEquals(pkg)) {
                return;
            }

            long now = SystemClock.uptimeMillis();

            if (now - lastClick < COOLDOWN_MS) {
                return;
            }

            if (!containsTwitch(root, 0)) {
                Log.d(TAG, "Twitch URL not found");
                return;
            }

            boolean clicked = findAndClick(root, 0);

            if (clicked) {
                lastClick = SystemClock.uptimeMillis();
                attempts++;

                Log.i(TAG,
                        "Click attempted: " + attempts);
            } else {
                Log.d(TAG,
                        "No accessible claim button");
            }
        } finally {
            root.recycle();
        }
    }

    private boolean containsTwitch(
            AccessibilityNodeInfo node, int depth) {

        if (node == null || depth > 50) {
            return false;
        }

        if (getText(node).contains("twitch.tv")) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);

            if (child != null) {
                try {
                    if (containsTwitch(child, depth + 1)) {
                        return true;
                    }
                } finally {
                    child.recycle();
                }
            }
        }

        return false;
    }

    private boolean findAndClick(
            AccessibilityNodeInfo node, int depth) {

        if (node == null || depth > 50) {
            return false;
        }

        String value = getText(node);

        if (isClaimButton(value)) {
            Log.i(TAG, "Candidate: " + value);

            AccessibilityNodeInfo target = node;

            for (int i = 0; i < 4 && target != null; i++) {

                if (target.isClickable()
                        && target.isEnabled()
                        && target.isVisibleToUser()) {

                    boolean clicked =
                            target.performAction(
                                    AccessibilityNodeInfo.ACTION_CLICK);

                    if (clicked) return true;
                }

                target = target.getParent();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);

            if (child != null) {
                try {
                    if (findAndClick(child, depth + 1)) {
                        return true;
                    }
                } finally {
                    child.recycle();
                }
            }
        }

        return false;
    }

    private boolean isClaimButton(String text) {
        return text.contains("claim bonus")
                || text.contains("claim points")
                || text.contains("bonus chest")
                || text.contains("claim reward")
                || text.contains("領取獎勵")
                || text.contains("領取點數")
                || text.contains("領取紅利");
    }

    private String getText(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();

        if (node.getText() != null) {
            sb.append(node.getText()).append(" ");
        }

        if (node.getContentDescription() != null) {
            sb.append(node.getContentDescription()).append(" ");
        }

        if (node.getViewIdResourceName() != null) {
            sb.append(node.getViewIdResourceName());
        }

        return sb.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "Service interrupted");
    }

    @Override
    public void onDestroy() {
        running = false;
        handler.removeCallbacks(scanner);
        super.onDestroy();
    }
}
