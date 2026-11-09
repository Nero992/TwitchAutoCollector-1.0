
package com.twitchautocollector.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.Gravity;

public class MainActivity extends Activity {

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 48, 32, 32);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Twitch Auto Collector v2.0");
        title.setTextSize(23);
        layout.addView(title);

        status = new TextView(this);
        status.setText(
            "Chrome 寶箱偵測版\n\n" +
            "請先啟用無障礙服務。"
        );
        status.setTextSize(17);
        status.setPadding(0, 30, 0, 30);
        layout.addView(status);

        Button settingsButton = new Button(this);
        settingsButton.setText("開啟無障礙設定");
        layout.addView(settingsButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        Button chromeButton = new Button(this);
        chromeButton.setText("開啟 Chrome Twitch");
        layout.addView(chromeButton,
            new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        chromeButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.twitch.tv/")
            );
            intent.setPackage("com.android.chrome");

            try {
                startActivity(intent);
            } catch (Exception e) {
                status.setText(
                    "無法開啟 Chrome：" + e.getMessage());
            }
        });

        setContentView(layout);
    }
}
