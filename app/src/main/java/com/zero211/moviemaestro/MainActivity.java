package com.zero211.moviemaestro;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.blogc.android.views.ExpandableTextView;

import static com.zero211.moviemaestro.DateFormatUtils.TMDB_API_DATE_FORMAT;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener//, NavigationView.OnNavigationItemSelectedListener
{

    private SwipeRefreshLayout swipeRefreshLayout;

    //private NavigationView navigationView;

    private TMDBCardListAdapter upcomingMovieListAdapter;
    private TMDBCardListAdapter inTheatresMovieAdapter;

    private MenuItem mediaRouteMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Set up Facebook's Fresco lib for simplified image downloading, processing, and cache management...
        Set<RequestListener> requestListeners = new HashSet<>();
        requestListeners.add(new RequestLoggingListener());
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
                // other setters
                .setRequestListeners(requestListeners)
                .build();
        Fresco.initialize(this, imagePipelineConfig);
        FLog.setMinimumLoggingLevel(FLog.ERROR);

        //setContentView(R.layout.activity_main;
        setContentView(R.layout.activity_main_content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = this.getActionBar();

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        inTheatresMovieAdapter = new TMDBCardListAdapter(this, TMDBCardListAdapter.CARDTYPE.IN_THEATRES,  R.id.rvInTheatresMovieCardList, R.id.lblInTheatresNow, null);
        upcomingMovieListAdapter = new TMDBCardListAdapter(this, TMDBCardListAdapter.CARDTYPE.COMING_SOON,  R.id.rvUpcomingMovieCardList, R.id.lblUpcomingMovies, R.id.swiperefresh);

        swipeRefreshLayout.setRefreshing(true);
        this.onRefresh();
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

//    @Override
//    public void onBackPressed()
//    {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START))
//        {
//            drawer.closeDrawer(GravityCompat.START);
//        }
//        else
//        {
//            super.onBackPressed();
//        }
//    }

    @Override
    public void onRefresh()
    {
        Calendar cal;

        cal = Calendar.getInstance();
        // for Upcoming movies, start with tomorrow's date
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String upcomingStartDateStr = TMDB_API_DATE_FORMAT.format(cal.getTime());

        // for Upcoming movies, end with a date six months from tomorrow
        cal.add(Calendar.MONTH, 6);
        String upcomingEndDateStr = TMDB_API_DATE_FORMAT.format(cal.getTime());

        GetMoviesByReleaseDateRangeAsyncTask getUpcomingMoviesAsyncTask = new GetMoviesByReleaseDateRangeAsyncTask(this, 1, Integer.MAX_VALUE, upcomingMovieListAdapter, upcomingStartDateStr, upcomingEndDateStr);
//        getUpcomingMoviesAsyncTask.execute();
        getUpcomingMoviesAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


        GetInTheatresMoviesAsyncTask getInTheatresMoviesAsyncTask = new GetInTheatresMoviesAsyncTask(this, 1, Integer.MAX_VALUE, inTheatresMovieAdapter);
//        getInTheatresMoviesAsyncTask.execute();
        getInTheatresMoviesAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main_actionbar, menu);

        // Get the SearchView and set the searchable configuration

        MenuItem searchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView)(searchMenuItem.getActionView());
        // Assumes current activity is the searchable activity
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        searchView.setGravity(GravityCompat.START);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.nav_about:
                Context context = this.getApplicationContext();
                Intent aboutIntent = new Intent(context, AboutActivity.class);
                context.startActivity(aboutIntent);
                break;
            case R.id.nav_settings:
            default:
                Toast.makeText(this, "Unhandled menu item: " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        }

        return true;
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
//    {
//        switch(menuItem.getItemId())
//        {
//            case R.id.nav_about:
//                Context context = this.getApplicationContext();
//                Intent aboutIntent = new Intent(context, AboutActivity.class);
//                context.startActivity(aboutIntent);
//                break;
//            case R.id.nav_settings:
//            default:
//                Toast.makeText(this, "Unhandled menu item: " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
//        }
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//
//        return true;
//    }
}
