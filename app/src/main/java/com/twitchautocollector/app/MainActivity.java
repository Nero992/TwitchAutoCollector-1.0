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
        
settings.setJavaScriptEnabled(true);
settings.setDomStorageEnabled(true);
settings.setUseWideViewPort(true);
settings.setLoadWithOverviewMode(true);
        


        
webView.setWebViewClient(new WebViewClient() {
    @Override
    public void onPageStarted(
        WebView view, String url,
        android.graphics.Bitmap favicon) {
        status.setText("正在載入：" + url);
    }

    @Override
    public void onPageFinished(
        WebView view, String url) {
        status.setText("網頁載入完成：" + url);
    }

    @Override
    public void onReceivedError(
        WebView view,
        android.webkit.WebResourceRequest request,
        android.webkit.WebResourceError error) {

        if (request.isForMainFrame()) {
            status.setText(
                "載入錯誤：" +
                error.getDescription()
            );
        }
    }

    @Override
    public void onReceivedHttpError(
        WebView view,
        android.webkit.WebResourceRequest request,
        android.webkit.WebResourceResponse response) {

        if (request.isForMainFrame()) {
            status.setText(
                "HTTP 錯誤：" +
                response.getStatusCode()
            );
        }
    }
});

        
webView.setWebChromeClient(new WebChromeClient() {
    @Override
    public boolean onConsoleMessage(
        android.webkit.ConsoleMessage message) {

        if (message.messageLevel() ==
                android.webkit.ConsoleMessage.MessageLevel.ERROR) {

            android.util.Log.e(
                "TwitchWebView",
                message.sourceId() + ":" +
                message.lineNumber() + " " +
                message.message()
            );

            status.setText(
                "JS 錯誤：" + message.message()
            );
        }

        return true;
    }
});


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
