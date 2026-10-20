package com.fullscreenhtml;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int PICK_HTML = 1;

    private FrameLayout root;
    private WebView webView;
    private WebChromeClient chromeClient;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWindow();

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        webView = createWebView();
        root.addView(
            webView,
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        );
        setContentView(root);

        hideSystemBars();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else if (!openFromIntent(getIntent())) {
            pickFile();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (!openFromIntent(intent)) {
            pickFile();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    private boolean openFromIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        Uri uri = null;

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            uri = intent.getData();
        } else if (Intent.ACTION_SEND.equals(intent.getAction())) {
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        if (uri == null) {
            return false;
        }

        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (text != null) {
            webView.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
            return true;
        }

        webView.loadUrl(uri.toString());
        return true;
    }

    private void pickFile() {
        Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("*/*");
        pick.putExtra(
            Intent.EXTRA_MIME_TYPES,
            new String[] {
                "text/html",
                "application/xhtml+xml",
                "text/plain",
                "application/xml"
            }
        );

        try {
            startActivityForResult(pick, PICK_HTML);
        } catch (Exception e) {
            Toast.makeText(this, "No file picker available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PICK_HTML) {
            return;
        }

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
            } catch (Exception ignored) {
                // Not all providers support persistable permissions.
            }
            webView.loadUrl(uri.toString());
        } else if (webView.getUrl() == null) {
            finish();
        }
    }

    private void setupWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    private void hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Window window = getWindow();
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
            }
            return;
        }

        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView createWebView() {
        WebView webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(
            new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    return false;
                }
            }
        );

        chromeClient = new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
                MainActivity.this.onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                MainActivity.this.onHideCustomView();
            }
        };
        webView.setWebChromeClient(chromeClient);

        return webView;
    }

    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }

        customView = view;
        customViewCallback = callback;

        root.addView(
            view,
            new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        );
        webView.setVisibility(View.GONE);
        hideSystemBars();
    }

    public void onHideCustomView() {
        if (customView == null) {
            return;
        }

        root.removeView(customView);
        customView = null;
        customViewCallback = null;
        webView.setVisibility(View.VISIBLE);
        hideSystemBars();
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }
            return;
        }

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }
}

