package com.zero211.moviemaestro;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HTMLContentActivity extends AppCompatActivity
{
    public static final String TITLE = "title";
    public static final String URL = "url";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html_content);

        String title = getIntent().getStringExtra(TITLE);
        String url = getIntent().getStringExtra(URL);

        this.setTitle(title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        WebView webView = findViewById(R.id.wvHTMLContent);

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url2) {
                view.loadUrl(url2);
                return true;
            }});


        webView.loadUrl(url);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //

            //navigateUpTo(new Intent(this, MainActivity.class));

            super.onBackPressed();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
