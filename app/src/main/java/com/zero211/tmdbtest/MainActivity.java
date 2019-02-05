package com.zero211.tmdbtest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import androidx.annotation.NonNull;

import com.facebook.drawee.view.SimpleDraweeView;
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import at.blogc.android.views.ExpandableTextView;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener
{

    private SwipeRefreshLayout swipeRefreshLayout;
    private MoviesAdapter moviesAdapter;

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

        RecyclerView movieCardList = findViewById(R.id.movieCardList);
        movieCardList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);
        movieCardList.setLayoutManager(llm);

        Configuration deviceConfig = getResources().getConfiguration();
        moviesAdapter = new MoviesAdapter(deviceConfig, swipeRefreshLayout);
        movieCardList.setAdapter(moviesAdapter);

        swipeRefreshLayout.setRefreshing(true);
        GetMoviesByReleaseDateRangeAsyncTask getMoviesByReleaseDateRangeAsyncTask = new GetMoviesByReleaseDateRangeAsyncTask(moviesAdapter);
        getMoviesByReleaseDateRangeAsyncTask.execute();
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
        GetMoviesByReleaseDateRangeAsyncTask getMoviesByReleaseDateRangeAsyncTask = new GetMoviesByReleaseDateRangeAsyncTask(moviesAdapter);
        getMoviesByReleaseDateRangeAsyncTask.execute();
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
