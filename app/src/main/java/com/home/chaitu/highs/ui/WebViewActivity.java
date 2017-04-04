package com.home.chaitu.highs.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.home.chaitu.highs.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chaitu on 28-08-2016.
 */
/*TODO:
* 2) url open to card with curated article -Jsoup - boilerpipe -WIP */
public class WebViewActivity extends AppCompatActivity {
    String url;
    public static final String WEB_VIEW_URL = "WEB_VIEW_URL";
    private WebView webView;
    private ProgressBar webViewProgressBar;
    private Handler progressBarHandler = new Handler();
    String NON_YOUTUBE_URL = "nonyoutubeurl";
    public static final String YOUTUBE_URL_REGEX = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUrl(savedInstanceState);
        setContentView(R.layout.web_view);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Activity activity = this;
        webViewProgressBar = (ProgressBar) findViewById(R.id.webViewProgressBar);
        webViewProgressBar.setProgress(0);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, final int progress) {
                progressBarHandler.post(new Runnable() {
                    public void run() {
                        webViewProgressBar.setProgress(progress * 100);
                    }
                });
                activity.setProgress(progress * 100);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Oops! " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                webViewProgressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webViewProgressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
            }


        });
        if (getYoutubeId(url).equals(NON_YOUTUBE_URL)) {
            webView.loadUrl(url);
        } else {
            String youTubeId = getYoutubeId(url);
            Log.d("onStart: ", youTubeId);
            String playVideo = "<iframe class=\"youtube-player\" allowtransparency=\"true\" style=\" border: 0; width: 100%; height: 50%; padding:0px; margin:0px\" type=\"text/html\"frameborder=\"0\" src=\"http://www.youtube.com/embed/" + youTubeId + "\"></iframe>";
            webView.loadData(playVideo, "text/html", "utf-8");
        }
    }


    private void setUrl(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                this.url = null;
            } else {
                this.url = extras.getString(WEB_VIEW_URL);
            }
        } else {
            this.url = (String) savedInstanceState.getSerializable(WEB_VIEW_URL);
        }
    }

    private String getYoutubeId(String youTubeUrl) {
        String pattern = YOUTUBE_URL_REGEX;

        Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        while (matcher.find()) {
            return (matcher.group(1));
        }
        return NON_YOUTUBE_URL;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        webView.loadUrl("about:blank");
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }


}
