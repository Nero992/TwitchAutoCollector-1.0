
package com.twitchautocollector.app;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

import java.util.Locale;

public class TwitchAccessibilityService
        extends AccessibilityService {

    private static final String TAG = "TwitchCollector";

    private static final long SCAN_INTERVAL = 1500;
    private static final long CLICK_COOLDOWN = 10000;

    private long lastScan = 0;
    private long lastClick = 0;
    private int clickAttempts = 0;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "Twitch accessibility service started");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;

        CharSequence packageName = event.getPackageName();

        if (packageName == null) return;

String pkg = packageName.toString();

if (!pkg.equals("tv.twitch.android.app")
        && !pkg.equals("com.android.chrome")) {
    return;
}

        long now = System.currentTimeMillis();

        if (now - lastScan < SCAN_INTERVAL) return;
        lastScan = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            Log.d(TAG, "No accessible root");
            return;
        }

        try {
            if (now - lastClick < CLICK_COOLDOWN) {
                return;
            }

            boolean clicked = scanForChest(root, 0);

            if (clicked) {
                lastClick = System.currentTimeMillis();
                clickAttempts++;

                Log.i(TAG, "Chest click attempted. Total: "
                        + clickAttempts);
            } else {
                Log.d(TAG, "No accessible chest found");
            }
        } finally {
            root.recycle();
        }
    }

    private boolean scanForChest(
            AccessibilityNodeInfo node, int depth) {

        if (node == null || depth > 40) return false;

        String text = getNodeText(node);

        if (isChestButton(text)) {
            Log.i(TAG, "Possible chest: " + text);

            AccessibilityNodeInfo target = node;

            for (int i = 0; i < 4 && target != null; i++) {
                if (target.isClickable()
                        && target.isEnabled()
                        && target.isVisibleToUser()) {

                    boolean result = target.performAction(
                            AccessibilityNodeInfo.ACTION_CLICK);

                    Log.i(TAG, "Click result: " + result);
                    return result;
                }

                target = target.getParent();
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);

            if (child != null) {
                try {
                    if (scanForChest(child, depth + 1)) {
                        return true;
                    }
                } finally {
                    child.recycle();
                }
            }
        }

        return false;
    }

    private boolean isChestButton(String text) {
        if (text.isEmpty()) return false;

        return text.contains("claim bonus")
                || text.contains("claim points")
                || text.contains("bonus chest")
                || text.contains("領取獎勵")
                || text.contains("領取點數")
                || text.contains("領取紅利");
    }

    private String getNodeText(AccessibilityNodeInfo node) {
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
        Log.d(TAG, "Service interrupted");
    }
}
