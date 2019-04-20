package com.zero211.moviemaestro;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zero211.moviemaestro.DateFormatUtils.*;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder>
{
    private static final String POSTER_IMAGE_SIZE = "w780";
    private static final String BACKDROP_IMAGE_SIZE = "w1280";

    public enum MOVIE_TYPE
    {
        IN_THEATRES,
        COMING_SOON,
        AS_CAST,
        AS_CREW
    }

    private MOVIE_TYPE movieType;
    private ArrayList<Map<String,Object>> moviesList = new ArrayList<Map<String, Object>>();
    private int total_pages;
    private int total_results;
    private Object loadingIndicator;
    private View[] viewsToMakeVisibleWhenDone;

    public MovieListAdapter(MOVIE_TYPE movieType, Object loadingIndicator, View... viewsToMakeVisibleWhenDone)
    {
        this.movieType = movieType;
        this.loadingIndicator = loadingIndicator;
        this.viewsToMakeVisibleWhenDone = viewsToMakeVisibleWhenDone;
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

        if (loadingIndicator != null)
        {
            if (loadingIndicator instanceof SwipeRefreshLayout)
            {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) loadingIndicator;
                swipeRefreshLayout.setRefreshing(false);
            }
            else if (loadingIndicator instanceof ProgressBar)
            {
                ProgressBar progressBar = (ProgressBar)loadingIndicator;
                progressBar.setVisibility(View.GONE);
            }
        }

        if (viewsToMakeVisibleWhenDone != null)
        {
            for (View viewToMakeVisibleWhenDone : viewsToMakeVisibleWhenDone)
            {
                viewToMakeVisibleWhenDone.setVisibility(View.VISIBLE);
            }
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



        String releaseDateStr = (String) (itemData.get("release_date"));
        String shortThisYearReleaseDateStr = getShortThisYearDateStrFromTMDBDateStr(releaseDateStr);
        String justYearReleaseDateStr = getJustYearDateStrFromTMDBDateStr(releaseDateStr);


        switch (this.movieType)
        {
            case AS_CAST:
                String character = (String)(itemData.get("character"));
                movieViewHolder.txtCharacterOrJob.setText(character);
                movieViewHolder.txtCharacterOrJob.setVisibility(View.VISIBLE);

                movieViewHolder.txtMovieReleaseDate.setText(justYearReleaseDateStr);
                movieViewHolder.txtMovieReleaseDate.setVisibility(View.VISIBLE);
                break;
            case AS_CREW:
                String job = (String)(itemData.get("job"));
                movieViewHolder.txtCharacterOrJob.setText(job);
                movieViewHolder.txtCharacterOrJob.setVisibility(View.VISIBLE);

                movieViewHolder.txtMovieReleaseDate.setText(justYearReleaseDateStr);
                movieViewHolder.txtMovieReleaseDate.setVisibility(View.VISIBLE);
                break;
            case COMING_SOON:
                movieViewHolder.txtMovieReleaseDate.setText(shortThisYearReleaseDateStr);
                movieViewHolder.txtMovieReleaseDate.setVisibility(View.VISIBLE);
                movieViewHolder.txtMovieReleaseDate.setVisibility(View.GONE);
                break;
            case IN_THEATRES:
            default:
                movieViewHolder.txtMovieReleaseDate.setVisibility(View.GONE);
                movieViewHolder.txtCharacterOrJob.setVisibility(View.GONE);
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
        protected TextView txtCharacterOrJob;

        public MovieViewHolder(View v)
        {
            super(v);
            imgMoviePoster = v.findViewById(R.id.imgMoviePoster);
            txtMovieTitle = v.findViewById(R.id.txtMovieTitle);
            txtMovieReleaseDate = v.findViewById(R.id.txtMovieReleaseDate);
            txtCharacterOrJob = v.findViewById(R.id.txtCharacterOrJob);
        }
    }
}
