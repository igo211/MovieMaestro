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

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>
{
    private SwipeRefreshLayout swipeRefreshLayout;
    private String backdropImageWidthStr = "w1280";
    private int total_pages;
    private int total_results;

    private ArrayList<Map<String,Object>> moviesList = new ArrayList<Map<String, Object>>();

    public MoviesAdapter(Configuration deviceConfig, SwipeRefreshLayout swipeRefreshLayout)
    {
        this.swipeRefreshLayout = swipeRefreshLayout;
        // TODO: Optimize the image size specification based on the device screen density/resolution.
        //
        // For now, we ask for the largest size (the Fresco drawee will scale it no matter what, but the download size
        // can be optimized based on the expected minimum size needed based on the device screen size/density/resolution)..

//        if (displaySize.x <= 300)
//        {
//            backdropImageWidthStr = "w300";
//        }
//        else if (displaySize.x <=780)
//        {
//            backdropImageWidthStr = "w780";
//        }
//        else
//        {
//            backdropImageWidthStr = "w1280";
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
        swipeRefreshLayout.setRefreshing(false);
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

        return new MovieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder movieViewHolder, int i)
    {
        Map<String, Object> itemData = moviesList.get(i);

        String backdropImgRelPath = (String)(itemData.get("backdrop_path"));

        String fullPathImageURI;
        if (backdropImgRelPath != null)
        {
            fullPathImageURI = "https://image.tmdb.org/t/p/" + backdropImageWidthStr + backdropImgRelPath;

        }
        else
        {
            fullPathImageURI = "res:///" + R.drawable.no_image_available;
        }

        itemData.put("backdrop_img_full_path", fullPathImageURI);
        movieViewHolder.imgMovieBackdrop.setImageURI(fullPathImageURI);

        movieViewHolder.imgMovieBackdrop.setTag(itemData);

        String title = (String)(itemData.get("title"));
        movieViewHolder.txtMovieTitle.setText(title);

        String descr = (String)(itemData.get("overview"));
        movieViewHolder.txtMovieDescr.setText(descr);

        String releaseDate = (String)(itemData.get("release_date"));
        movieViewHolder.txtMovieReleaseDate.setText(releaseDate);
    }

    @Override
    public int getItemCount()
    {
        return moviesList.size();
    }


    public static class MovieViewHolder extends RecyclerView.ViewHolder
    {
        protected SimpleDraweeView imgMovieBackdrop;
        protected TextView txtMovieTitle;
        protected TextView txtMovieDescr;
        protected TextView txtMovieReleaseDate;

        public MovieViewHolder(View v)
        {
            super(v);
            imgMovieBackdrop = v.findViewById(R.id.imgMovieBackdrop);
            txtMovieTitle = v.findViewById(R.id.txtMovieTitle);
            txtMovieDescr = v.findViewById(R.id.txtMovieOverview);
            txtMovieReleaseDate = v.findViewById(R.id.txtMovieReleaseDate);
        }
    }
}
