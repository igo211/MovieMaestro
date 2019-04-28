package com.zero211.moviemaestro;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Map;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import at.blogc.android.views.ExpandableTextView;

public class PersonDetailActivity extends AppCompatActivity
{
    public static final String ARG_PERSON_ID = "personID";
    public static final String ARG_NAME = "name";
    public static final String ARG_PROFILE_IMG_FULL_PATH = "profile_img_full_path";

    private int personID;
    private String name;
    private String profile_img_full_path;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_detail);
        ProgressBar pgLoading = findViewById(R.id.pgLoading);

        this.personID = getIntent().getIntExtra(ARG_PERSON_ID, 0);
        this.name = getIntent().getStringExtra(ARG_NAME);
        this.profile_img_full_path = getIntent().getStringExtra(ARG_PROFILE_IMG_FULL_PATH);

        AppBarLayout appBarLayout = findViewById(R.id.app_bar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(this.name);
        }


        toolbar.setTitle(this.name);

        SimpleDraweeView profile_pic = findViewById(R.id.imgPersonPoster);
        profile_pic.setImageURI(this.profile_img_full_path);

        pgLoading.setVisibility(View.VISIBLE);
        GetPersonDetailsAsyncTask getPersonDetailsAsyncTask = new GetPersonDetailsAsyncTask(this);
        getPersonDetailsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public int getPersonID()
    {
        return personID;
    }

    public String getName()
    {
        return name;
    }

    public String getProfile_img_full_path()
    {
        return profile_img_full_path;
    }

    public void launchURI(View view)
    {
        Button btn = (Button)view;
        String uriStr = (String)(btn.getTag());
        Uri uri = Uri.parse(uriStr);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(launchBrowser);
    }

    public void movieDetails(View view)
    {
        Map<String,Object> itemData = (Map<String, Object>) view.getTag();
        //Toast.makeText(this, "Would have navigated and shown details for '" + itemData.get("title") + "' with movie id: " + itemData.get("id") ,Toast.LENGTH_LONG).show();

        Context context = view.getContext();

        Intent intent = new Intent(context, MovieDetailActivity.class);
        intent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, (Integer)(itemData.get("id")));
        intent.putExtra(MovieDetailFragment.ARG_MOVIE_TITLE, (String) itemData.get("title"));
        intent.putExtra(MovieDetailFragment.ARG_MOVIE_BACKDROP_URL, (String) itemData.get("backdrop_img_full_path"));
        intent.putExtra(MovieDetailFragment.ARG_MOVIE_RELEASE_DATE, (String) itemData.get("release_date"));

        context.startActivity(intent);
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

    public void overviewToggle(View v)
    {
        ExpandableTextView tv;

        if (v instanceof ExpandableTextView)
        {
            tv = (ExpandableTextView)v;
            tv.toggle();
        }
        else
        {
            Integer labledID = v.getLabelFor();
            if (labledID != null)
            {
                ViewGroup parentView = (ViewGroup) v.getParent();

                View labeledView = parentView.findViewById(labledID);
                if (labeledView instanceof ExpandableTextView)
                {
                    tv = (ExpandableTextView)labeledView;
                    tv.toggle();
                }
            }
        }
    }
}
