package com.zero211.moviemaestro;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity
{
    private static final String ELLIPSES = "…";
    private static final String RAW_PREFIX = "file:///android_res/raw/";

    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_about);
        this.setTitle(R.string.about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        frameLayout = findViewById(R.id.frameLayout);

        View aboutPage = new AboutPage(this)
//                .setImage(R.drawable.dummy_image)
                .setDescription("About MovieMaestro")
                .addItem(getVersionElement())
                .addGroup(getString(R.string.legal_header))
                .addItem(getCopyRightElement())
                .addItem(getTermsAndConditionsElement())
                .addItem(getPrivacyElement())
                .addItem(getThirdPartyLicensesElement())
                .addGroup(getString(R.string.ways_to_connect_header))
                .addEmail("info@zero211.com", getString(R.string.email_us))
                .addWebsite("http://www.zero211.com/", getString(R.string.visit_our_website))
//                .addPlayStore("com.zero211.moviemaestro")
//                .addFacebook("the.medy")
//                .addInstagram("medyo80")
//                .addTwitter("medyo80")
//                .addYoutube("UCdPQtdWIsg7_pi4mrRu46vA")
//                .addGitHub("medyo")
                .create();


        frameLayout.addView(aboutPage);
    }

    private Intent getHTMLContactActivityIntent(String title, String url)
    {
        Intent intent = new Intent(frameLayout.getContext(), HTMLContentActivity.class);
        intent.putExtra(HTMLContentActivity.TITLE, title);
        intent.putExtra(HTMLContentActivity.URL, url);
        return intent;
    }

    private Element getTermsAndConditionsElement()
    {
        String title = getString(R.string.terms_and_conditions);
        String url = RAW_PREFIX + "terms.html";

        Element element = new Element()
                .setTitle(title + ELLIPSES)
                .setIntent(getHTMLContactActivityIntent(title,url))
//                .setOnClickListener(view -> {
//                    Toast.makeText(AboutActivity.this, "Would have shown terms and conditions here", Toast.LENGTH_LONG).show();
//                })
                ;

        return element;
    }

    private Element getPrivacyElement()
    {
        String title = getString(R.string.privacy_policy_label);
        String url = RAW_PREFIX + "privacy.html";

        Element element = new Element()
                .setTitle(title + ELLIPSES)
                .setIntent(getHTMLContactActivityIntent(title,url))
//                .setOnClickListener(view -> {
//                    Toast.makeText(AboutActivity.this, "Would have shown privacy policy here", Toast.LENGTH_LONG).show();
//                })
                ;

        return element;
    }

    private Element getThirdPartyLicensesElement()
    {
        String title = getString(R.string.third_party_licenses);
        String url = RAW_PREFIX + "licenses.html";

        Element element = new Element()
                .setTitle(title + ELLIPSES)
                .setIntent(getHTMLContactActivityIntent(title,url))
//                .setOnClickListener(view -> {
//                    Toast.makeText(AboutActivity.this, "Would have shown third party licenses here", Toast.LENGTH_LONG).show();
//                })
                ;

        return element;
    }

    private String getVersionStr()
    {
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        SimpleDateFormat buildDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String buildDateStr = buildDateFormat.format(buildDate);

        return buildDateStr;
    }

    private Element getVersionElement()
    {
        String buildDateStr = getVersionStr();
        Element versionElement = new Element();
        versionElement.setTitle(getString(R.string.version_label) + " " + buildDateStr);
        return versionElement;
    }

    private Element getCopyRightElement()
    {
        Element copyRightElement = new Element();
        final String copyright = String.format(getString(R.string.copyright), Calendar.getInstance().get(Calendar.YEAR));
        copyRightElement.setTitle(copyright);
        //copyRightsElement.setIconDrawable(R.drawable.about_icon_copy_right);
        copyRightElement.setIconTint(R.color.about_item_icon_color);
        copyRightElement.setIconNightTint(android.R.color.white);
//        copyRightElement.setGravity(Gravity.CENTER);
//        copyRightElement.setOnClickListener(view ->
//            {
//                Toast.makeText(AboutActivity.this, copyright, Toast.LENGTH_SHORT).show();
//            }
//        );

        return copyRightElement;
    }


    @Override
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
