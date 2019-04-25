package com.zero211.moviemaestro;

import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

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

//        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
//        {
//
//        }

        String query = intent.getStringExtra(SearchManager.QUERY);

        MovieListAdapter movieListAdapter = new MovieListAdapter(MovieListAdapter.MOVIE_TYPE.SEARCH_RESULT, this, R.id.rvMovieSearchResultsCardList, R.id.lblMovieSearchResults, null);
        PersonListAdapter personListAdapter = new PersonListAdapter(PersonListAdapter.PERSON_TYPE.SEARCH_RESULT, this, R.id.rvPeopleSearchResultsCardList, R.id.lblPeopleSearchResults, R.id.pgLoading);

        GetMultiSearchResultsAsyncTask getMultiSearchResultsAsyncTask = new GetMultiSearchResultsAsyncTask(this, 1, Integer.MAX_VALUE, query, movieListAdapter, personListAdapter);

        ProgressBar pgLoading = findViewById(R.id.pgLoading);
        pgLoading.setVisibility(View.VISIBLE);
        getMultiSearchResultsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
