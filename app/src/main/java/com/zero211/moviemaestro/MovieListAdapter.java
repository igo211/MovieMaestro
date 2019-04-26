package com.zero211.moviemaestro;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import static com.zero211.moviemaestro.DateFormatUtils.getJustYearDateStrFromTMDBDateStr;
import static com.zero211.moviemaestro.DateFormatUtils.getLongDateStrFromTMDBDateStr;
import static com.zero211.moviemaestro.DateFormatUtils.getShortThisYearDateStrFromTMDBDateStr;

// TODO: Refactor this class and PersonListAdapter to have an abstract parent class (AbstractPosterListAdapter) that contains the object list, the add* methods, the loadingIndicator and the viewsToMakeVisible for all list adapters
public class MovieListAdapter extends AbstractTMDBCardListAdapter<MovieListAdapter.MovieViewHolder>
{
    private static final String POSTER_IMAGE_SIZE = "w780";
    private static final String BACKDROP_IMAGE_SIZE = "w1280";

    public enum MOVIE_TYPE
    {
        IN_THEATRES,
        COMING_SOON,
        AS_CAST,
        AS_CREW,
        SEARCH_RESULT
    }

    private MOVIE_TYPE movieType;

    public MovieListAdapter(@NonNull MOVIE_TYPE movieType, @NonNull Activity activity, @NonNull Integer recyclerViewID, @Nullable Integer labelID, @Nullable Integer loadingIndicatorID)
    {
        super(activity, R.layout.movie_card, recyclerViewID, labelID, loadingIndicatorID);
        this.movieType = movieType;
    }


    public void clearAndAddList(List<Map<String,Object>> newItemList)
    {
        super.clearAndAddList(newItemList);
    }

    public void addList(List<Map<String,Object>> listToAdd)
    {
        super.addList(listToAdd);
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = this.getItemView(viewGroup);
        return new MovieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder movieViewHolder, int i)
    {
        Map<String, Object> itemData = this.getItem(i);

        String backDropImgRelPath = (String)(itemData.get("backdrop_path"));

        String backdropFullPathImageURI;
        if (backDropImgRelPath != null)
        {
            backdropFullPathImageURI = TMDB_IMAGE_PATH_PREFIX + BACKDROP_IMAGE_SIZE + backDropImgRelPath;

        }
        else
        {
            backdropFullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.no_image_available;
        }

        itemData.put("backdrop_img_full_path", backdropFullPathImageURI);

        String posterImgRelPath = (String)(itemData.get("poster_path"));

        String posterFullPathImageURI;
        if (posterImgRelPath != null)
        {
            posterFullPathImageURI = TMDB_IMAGE_PATH_PREFIX + POSTER_IMAGE_SIZE + posterImgRelPath;

        }
        else
        {
            posterFullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.no_image_available;
        }

        itemData.put("poster_img_full_path", posterFullPathImageURI);


        movieViewHolder.imgMoviePoster.setImageURI(posterFullPathImageURI);

        movieViewHolder.imgMoviePoster.setTag(itemData);

        String title = (String)(itemData.get("title"));
        movieViewHolder.txtMovieTitle.setText(title);



        String releaseDateStr = (String) (itemData.get("release_date"));
        String shortThisYearReleaseDateStr = getShortThisYearDateStrFromTMDBDateStr(releaseDateStr);
        String longReleaseDateStr = getLongDateStrFromTMDBDateStr(releaseDateStr);
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
                movieViewHolder.txtCharacterOrJob.setVisibility(View.GONE);

                movieViewHolder.txtMovieReleaseDate.setText(shortThisYearReleaseDateStr);
                movieViewHolder.txtMovieReleaseDate.setVisibility(View.VISIBLE);
                break;
            case SEARCH_RESULT:
                movieViewHolder.txtCharacterOrJob.setVisibility(View.GONE);

                if (StringUtils.isNullOrEmpty(releaseDateStr))
                {
                    movieViewHolder.txtMovieReleaseDate.setVisibility(View.GONE);
                }
                else
                {
                    movieViewHolder.txtMovieReleaseDate.setText(justYearReleaseDateStr);
                    movieViewHolder.txtMovieReleaseDate.setVisibility(View.VISIBLE);
                }
                break;
            case IN_THEATRES:
            default:
                movieViewHolder.txtMovieReleaseDate.setVisibility(View.GONE);
                movieViewHolder.txtCharacterOrJob.setVisibility(View.GONE);
        }
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
