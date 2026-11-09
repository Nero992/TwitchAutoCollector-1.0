package com.twitchautocollector.app;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView webView;
    private TextView status;
    private final Handler handler = new Handler();
    private int attempts = 0;

    private final String collectorScript =
        "(function() {" +
        "const selectors = [" +
        "'button[aria-label*=\"Claim\"]'," +
        "'button[aria-label*=\"領取\"]'," +
        "'[data-test-selector=\"claimable-bonus__icon\"]'" +
        "];" +
        "for (const selector of selectors) {" +
        " const el = document.querySelector(selector);" +
        " if (el) {" +
        "  const btn = el.closest('button') || el;" +
        "  btn.click();" +
        "  return 'clicked';" +
        " }" +
        "}" +
        "return 'none';" +
        "})();";

    private final Runnable scanner = new Runnable() {
        @Override
        public void run() {
            if (webView != null) {
                webView.evaluateJavascript(
                    collectorScript,
                    result -> {
                        if ("\"clicked\"".equals(result)) {
                            attempts++;
                            status.setText(
                                "領取點擊次數：" + attempts
                            );
                        }
                    }
                );
                handler.postDelayed(this, 5000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        status = new TextView(this);
        status.setText("Twitch Auto Collector 已啟動");
        status.setPadding(16, 16, 16, 16);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        
settings.setUseWideViewPort(true);
settings.setLoadWithOverviewMode(true);


String originalUA = settings.getUserAgentString();

String desktopUA = originalUA
    .replace("; wv", "")
    .replace("Mobile ", "");

settings.setUserAgentString(desktopUA);


settings.setUserAgentString(desktopUA);


        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        layout.addView(status);
        layout.addView(webView,
            new LinearLayout.LayoutParams(
                -1, 0, 1
            )
        );

        setContentView(layout);
        webView.loadUrl("https://www.twitch.tv");

        handler.postDelayed(scanner, 5000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(scanner);
        webView.destroy();
        super.onDestroy();
    }
}
