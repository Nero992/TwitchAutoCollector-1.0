
package com.twitchautocollector.app;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.util.Log;

import java.util.Locale;

public class TwitchAccessibilityService
        extends AccessibilityService {

    private static final String TAG = "TwitchCollector";
    private long lastScan = 0;

    @Override
    public void onAccessibilityEvent(
            AccessibilityEvent event) {

        if (event == null) return;

        long now = System.currentTimeMillis();
        if (now - lastScan < 3000) return;
        lastScan = now;

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            Log.d(TAG, "No accessible window");
            return;
        }

        try {
            if (!isTwitchPage(root, 0)) {
                Log.d(TAG, "Twitch page not confirmed");
                return;
            }

            boolean found = findChest(root, 0);

            if (found) {
                Log.i(TAG, "Possible chest detected");
            } else {
                Log.d(TAG, "No chest found");
            }
        } finally {
            root.recycle();
        }
    }

    private boolean isTwitchPage(
            AccessibilityNodeInfo node, int depth) {

        if (node == null || depth > 30) return false;

        String value = getNodeText(node);

        if (value.contains("twitch.tv")) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (isTwitchPage(node.getChild(i), depth + 1)) {
                return true;
            }
        }

        return false;
    }

    private boolean findChest(
            AccessibilityNodeInfo node, int depth) {

        if (node == null || depth > 30) return false;

        String value = getNodeText(node);

        if (value.contains("claim bonus")
                || value.contains("claim points")
                || value.contains("領取獎勵")
                || value.contains("領取點數")
                || value.contains("bonus chest")) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (findChest(node.getChild(i), depth + 1)) {
                return true;
            }
        }

        return false;
    }

    private String getNodeText(AccessibilityNodeInfo node) {
        StringBuilder sb = new StringBuilder();

        if (node.getText() != null) {
            sb.append(node.getText()).append(" ");
        }

        if (node.getContentDescription() != null) {
            sb.append(node.getContentDescription());
        }

        return sb.toString().toLowerCase(Locale.ROOT);
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }
}
