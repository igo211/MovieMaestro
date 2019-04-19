package com.zero211.moviemaestro;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastContext;

import java.util.Map;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import at.blogc.android.views.ExpandableTextView;

public class MovieDetailActivity extends AppCompatActivity
{
    private CastContext mCastContext;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        // Setup the Cast context
        mCastContext = CastContext.getSharedInstance(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.movie_detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null)
        {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(MovieDetailFragment.ARG_MOVIE_ID,
                    getIntent().getIntExtra(MovieDetailFragment.ARG_MOVIE_ID, 0));
            arguments.putString(MovieDetailFragment.ARG_MOVIE_TITLE,
                    getIntent().getStringExtra(MovieDetailFragment.ARG_MOVIE_TITLE));
            arguments.putString(MovieDetailFragment.ARG_MOVIE_RELEASE_DATE,
                    getIntent().getStringExtra(MovieDetailFragment.ARG_MOVIE_RELEASE_DATE));
            arguments.putString(MovieDetailFragment.ARG_MOVIE_BACKDROP_URL,
                    getIntent().getStringExtra(MovieDetailFragment.ARG_MOVIE_BACKDROP_URL));
            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();


        }


    }


    public void personDetails(View view)
    {
        Map<String,Object> itemData = (Map<String, Object>) view.getTag();
        //Toast.makeText(this, "Would have navigated and shown details for '" + itemData.get("name") + "' with id: " + itemData.get("id") ,Toast.LENGTH_LONG).show();

        Context context = view.getContext();

        Intent intent = new Intent(context, PersonDetailActivity.class);
        intent.putExtra(PersonDetailActivity.ARG_PERSON_ID, (Integer)(itemData.get("id")));
        intent.putExtra(PersonDetailActivity.ARG_NAME, (String) itemData.get("name"));
        intent.putExtra(PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH, (String) itemData.get(PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH));
//        intent.putExtra(MovieDetailFragment.ARG_MOVIE_TITLE, (String) itemData.get("title"));
//        intent.putExtra(MovieDetailFragment.ARG_MOVIE_BACKDROP_URL, (String) itemData.get("backdrop_img_full_path"));

        context.startActivity(intent);
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
