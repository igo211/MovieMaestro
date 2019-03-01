package com.zero211.moviemaestro;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import static com.zero211.moviemaestro.AbstractTMDBJSONResultFromURLTask.TMDB_DATE_FORMAT;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener
{

    private SwipeRefreshLayout swipeRefreshLayout;

    private MoviesAdapter upcomingMoviesAdapter;
    private MoviesAdapter inTheatresMovieAdapter;

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
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        Configuration deviceConfig = getResources().getConfiguration();

        RecyclerView upcomingMovieCardList = findViewById(R.id.rvUpcomingMovieCardList);
        upcomingMovieCardList.setHasFixedSize(true);
        LinearLayoutManager upcomingLLM = new LinearLayoutManager(this);
        upcomingLLM.setOrientation(RecyclerView.HORIZONTAL);
        upcomingMovieCardList.setLayoutManager(upcomingLLM);
        upcomingMoviesAdapter = new MoviesAdapter(deviceConfig, null, true);
        upcomingMovieCardList.setAdapter(upcomingMoviesAdapter);

        RecyclerView inTheatresMovieCardList = findViewById(R.id.rvInTheatresMovieCardList);
        inTheatresMovieCardList.setHasFixedSize(true);
        LinearLayoutManager inTheatresLLM = new LinearLayoutManager(this);
        inTheatresLLM.setOrientation(RecyclerView.HORIZONTAL);
        inTheatresMovieCardList.setLayoutManager(inTheatresLLM);
        inTheatresMovieAdapter = new MoviesAdapter(deviceConfig, swipeRefreshLayout, false);
        inTheatresMovieCardList.setAdapter(inTheatresMovieAdapter);

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

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public void onRefresh()
    {
        Calendar cal;

        cal = Calendar.getInstance();
        // for Upcoming movies, start with tomorrow's date
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String upcomingStartDateStr = TMDB_DATE_FORMAT.format(cal.getTime());

        // for Upcoming movies, end with a date six months from tomorrow
        cal.add(Calendar.MONTH, 6);
        String upcomingEndDateStr = TMDB_DATE_FORMAT.format(cal.getTime());

        GetMoviesByReleaseDateRangeAsyncTask getUpcomingMoviesAsyncTask = new GetMoviesByReleaseDateRangeAsyncTask(upcomingMoviesAdapter, upcomingStartDateStr, upcomingEndDateStr);
        getUpcomingMoviesAsyncTask.execute();

        GetInTheatresMoviesAsyncTask getInTheatresMoviesAsyncTask = new GetInTheatresMoviesAsyncTask(inTheatresMovieAdapter);
        getInTheatresMoviesAsyncTask.execute();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        switch(menuItem.getItemId())
        {
            default:
                Toast.makeText(this, "Unhandled menu item: " + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
