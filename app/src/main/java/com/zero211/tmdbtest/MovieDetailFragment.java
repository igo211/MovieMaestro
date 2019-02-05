package com.zero211.tmdbtest;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.fragment.app.Fragment;
import at.blogc.android.views.ExpandableTextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MovieDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MovieDetailFragment extends Fragment
{
    public static final String ARG_MOVIE_ID = "movieID";
    public static final String ARG_MOVIE_TITLE = "movieTitle";
    public static final String ARG_MOVIE_BACKDROP_URL = "movieBackdropUrl";

    private Integer movieID;
    private String movieTitle;
    private String movieBackdropUrl;

    private CollapsingToolbarLayout appBarLayout;
    private SimpleDraweeView imgMovieBackdrop;


    public MovieDetailFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param movieID The TMDB movie id.
     * @param movieTitle The movie title.
     * @return A new instance of fragment MovieDetailFragment.
     */
    public static MovieDetailFragment newInstance(Integer movieID, String movieTitle, String movieBackdropUrl)
    {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieID);
        args.putString(ARG_MOVIE_TITLE, movieTitle);
        args.putString(ARG_MOVIE_BACKDROP_URL, movieBackdropUrl);
        fragment.setArguments(args);
        return fragment;
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
            if (appBarLayout != null)
            {
                appBarLayout.setTitle(movieTitle);
            }
        }

        if (getArguments().containsKey(ARG_MOVIE_BACKDROP_URL))
        {
            movieBackdropUrl = getArguments().getString(ARG_MOVIE_BACKDROP_URL);
            imgMovieBackdrop = (SimpleDraweeView) activity.findViewById(R.id.imgMovieBackdrop);
            if (imgMovieBackdrop != null)
            {
                imgMovieBackdrop.setImageURI(movieBackdropUrl);
            }
        }

        GetMovieDetailsAsyncTask getMovieDetailsAsyncTask = new GetMovieDetailsAsyncTask(this);
        getMovieDetailsAsyncTask.execute();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    public Integer getMovieID()
    {
        return movieID;
    }
}
