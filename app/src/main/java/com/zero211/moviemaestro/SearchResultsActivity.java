package com.zero211.moviemaestro;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Map;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SearchResultsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search_results);

        this.setTitle(R.string.search_results);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);

            MovieListAdapter movieListAdapter = new MovieListAdapter(MovieListAdapter.MOVIE_TYPE.SEARCH_RESULT, this, R.id.rvMovieSearchResultsCardList, R.id.lblMovieSearchResults, null);
            PersonListAdapter personListAdapter = new PersonListAdapter(PersonListAdapter.PERSON_TYPE.SEARCH_RESULT, this, R.id.rvPeopleSearchResultsCardList, R.id.lblPeopleSearchResults, R.id.pgLoading);

            GetMultiSearchResultsAsyncTask getMultiSearchResultsAsyncTask = new GetMultiSearchResultsAsyncTask(this, 1, Integer.MAX_VALUE, query, movieListAdapter, personListAdapter);

            ProgressBar pgLoading = findViewById(R.id.pgLoading);
            pgLoading.setVisibility(View.VISIBLE);
            getMultiSearchResultsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

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

    public void personDetails(View view)
    {
        Map<String,Object> itemData = (Map<String, Object>) view.getTag();
        //Toast.makeText(this, "Would have navigated and shown details for '" + itemData.get("name") + "' with id: " + itemData.get("id") ,Toast.LENGTH_LONG).show();

        Context context = view.getContext();

        Intent intent = new Intent(context, PersonDetailActivity.class);
        intent.putExtra(PersonDetailActivity.ARG_PERSON_ID, (Integer)(itemData.get("id")));
        intent.putExtra(PersonDetailActivity.ARG_NAME, (String) itemData.get("name"));
        intent.putExtra(PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH, (String) itemData.get(PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH));

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
}
