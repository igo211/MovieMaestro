package com.zero211.moviemaestro;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.zero211.moviemaestro.DateFormatUtils.*;

public class MovieDetailFragment extends Fragment
{
    public static final String ARG_MOVIE_ID = "movieID";
    public static final String ARG_MOVIE_TITLE = "movieTitle";
    public static final String ARG_MOVIE_RELEASE_DATE = "movieReleaseDate";
    public static final String ARG_MOVIE_BACKDROP_URL = "movieBackdropUrl";

    private Integer movieID;
    private String movieTitle;
    private String movieReleaseDateStr;
    private String movieBackdropUrl;

    private CollapsingToolbarLayout appBarLayout;
    private SimpleDraweeView imgMovieBackdrop;
    private TextView txtReleaseDate;


    public MovieDetailFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Activity activity = this.getActivity();

        if (getArguments().containsKey(ARG_MOVIE_ID))
        {
            movieID = getArguments().getInt(ARG_MOVIE_ID);
        }

        if (getArguments().containsKey(ARG_MOVIE_TITLE))
        {
            movieTitle = getArguments().getString(ARG_MOVIE_TITLE);

            appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.movie_detail_toolbar_layout);

        }



        if (getArguments().containsKey(ARG_MOVIE_BACKDROP_URL))
        {
            movieBackdropUrl = getArguments().getString(ARG_MOVIE_BACKDROP_URL);
            imgMovieBackdrop = (SimpleDraweeView) activity.findViewById(R.id.imgMoviePoster);

        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        if (getArguments().containsKey(ARG_MOVIE_RELEASE_DATE))
        {
            movieReleaseDateStr = getArguments().getString(ARG_MOVIE_RELEASE_DATE);
            txtReleaseDate = (TextView) fragmentView.findViewById(R.id.txtReleaseDate);

            if (txtReleaseDate != null)
            {
                String formattedMovieReleaseDateStr = getLongDateStrFromTMDBDateStr(movieReleaseDateStr);
                txtReleaseDate.setText(formattedMovieReleaseDateStr);
            }
        }

        return fragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        GetMovieDetailsAsyncTask getMovieDetailsAsyncTask = new GetMovieDetailsAsyncTask(this);
//        getMovieDetailsAsyncTask.execute();
        getMovieDetailsAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (appBarLayout != null)
        {
            appBarLayout.setTitle(movieTitle);
        }

        if (imgMovieBackdrop != null)
        {
            imgMovieBackdrop.setImageURI(movieBackdropUrl);
        }

    }

    public Integer getMovieID()
    {
        return movieID;
    }
}
