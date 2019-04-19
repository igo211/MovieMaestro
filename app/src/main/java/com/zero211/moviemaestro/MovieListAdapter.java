package com.zero211.moviemaestro;

import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zero211.moviemaestro.DateFormatUtils.*;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder>
{
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean showReleaseDate;

    private static final String POSTER_IMAGE_SIZE = "w780";
    private static final String BACKDROP_IMAGE_SIZE = "w1280";

    private int total_pages;
    private int total_results;


    private ArrayList<Map<String,Object>> moviesList = new ArrayList<Map<String, Object>>();

    public MovieListAdapter(Configuration deviceConfig, SwipeRefreshLayout swipeRefreshLayout, boolean showReleaseDate)
    {
        this.swipeRefreshLayout = swipeRefreshLayout;
        this.showReleaseDate = showReleaseDate;

        // TODO: Optimize the image size specification based on the device screen density/resolution.
        //
        // For now, we ask for the largest size (the Fresco drawee will scale it no matter what, but the download size
        // can be optimized based on the expected minimum size needed based on the device screen size/density/resolution)..

//        if (displaySize.x <= 300)
//        {
//            POSTER_IMAGE_SIZE = "w300";
//        }
//        else if (displaySize.x <=780)
//        {
//            POSTER_IMAGE_SIZE = "w780";
//        }
//        else
//        {
//            POSTER_IMAGE_SIZE = "w1280";
//        }
    }

    public void setTotal_pages(int total_pages)
    {
        this.total_pages = total_pages;
    }

    public void setTotal_results(int total_results)
    {
        this.total_results = total_results;
    }

    public void clearAndAddMovies(List<Map<String,Object>> moviesToAdd)
    {
        moviesList.clear();
        moviesList.addAll(moviesToAdd);
        this.notifyDataSetChanged();
        if (swipeRefreshLayout != null)
        {
            swipeRefreshLayout.setRefreshing(false);

            TextView lblInTheatres = swipeRefreshLayout.findViewById(R.id.lblInTheatresNow);
            lblInTheatres.setVisibility(View.VISIBLE);

            TextView lblComingSoon = swipeRefreshLayout.findViewById(R.id.lblUpcomingMovies);
            lblComingSoon.setVisibility(View.VISIBLE);
        }
    }

    public void addMovies(List<Map<String,Object>> moviesToAdd)
    {
        int insertPos = moviesList.size() - 1;
        moviesList.addAll(moviesToAdd);
        this.notifyItemRangeInserted(insertPos, moviesToAdd.size());
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.movie_card, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = (int) (viewGroup.getWidth() * 0.4);
        itemView.setLayoutParams(layoutParams);

        return new MovieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder movieViewHolder, int i)
    {
        Map<String, Object> itemData = moviesList.get(i);

        String backDropImgRelPath = (String)(itemData.get("backdrop_path"));

        String backdropFullPathImageURI;
        if (backDropImgRelPath != null)
        {
            backdropFullPathImageURI = "https://image.tmdb.org/t/p/" + BACKDROP_IMAGE_SIZE + backDropImgRelPath;

        }
        else
        {
            backdropFullPathImageURI = "res:///" + R.drawable.no_image_available;
        }

        itemData.put("backdrop_img_full_path", backdropFullPathImageURI);

        String posterImgRelPath = (String)(itemData.get("poster_path"));

        String posterFullPathImageURI;
        if (posterImgRelPath != null)
        {
            posterFullPathImageURI = "https://image.tmdb.org/t/p/" + POSTER_IMAGE_SIZE + posterImgRelPath;

        }
        else
        {
            posterFullPathImageURI = "res:///" + R.drawable.no_image_available;
        }

        itemData.put("poster_img_full_path", posterFullPathImageURI);


        movieViewHolder.imgMoviePoster.setImageURI(posterFullPathImageURI);

        movieViewHolder.imgMoviePoster.setTag(itemData);

        String title = (String)(itemData.get("title"));
        movieViewHolder.txtMovieTitle.setText(title);

        if (this.showReleaseDate)
        {
            String releaseDateStr = (String) (itemData.get("release_date"));
            String formattedReleaseDateStr = getShortThisYearDateStrFromTMDBDateStr(releaseDateStr);
            movieViewHolder.txtMovieReleaseDate.setText(formattedReleaseDateStr);
            movieViewHolder.txtMovieReleaseDate.setVisibility(View.VISIBLE);
        }
        else
        {
            movieViewHolder.txtMovieReleaseDate.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount()
    {
        return moviesList.size();
    }


    public static class MovieViewHolder extends RecyclerView.ViewHolder
    {
        protected SimpleDraweeView imgMoviePoster;
        protected TextView txtMovieTitle;
        protected TextView txtMovieReleaseDate;

        public MovieViewHolder(View v)
        {
            super(v);
            imgMoviePoster = v.findViewById(R.id.imgMoviePoster);
            txtMovieTitle = v.findViewById(R.id.txtMovieTitle);
            txtMovieReleaseDate = v.findViewById(R.id.txtMovieReleaseDate);
        }
    }
}
