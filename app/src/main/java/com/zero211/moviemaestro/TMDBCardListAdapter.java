package com.zero211.moviemaestro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.zero211.moviemaestro.DateFormatUtils.*;

import static com.zero211.moviemaestro.PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH;

public class TMDBCardListAdapter extends Adapter
{
    protected static final String TMDB_IMAGE_PATH_PREFIX = "https://image.tmdb.org/t/p/";
    protected static final String FRESCO_RESOURCES_IMAGE_PATH_PREFIX = "res:///";

    protected static final String PROFILE_IMAGE_SIZE = "h632";
    protected static final String POSTER_IMAGE_SIZE = "w780";
    protected static final String BACKDROP_IMAGE_SIZE = "w1280";

    public static final String ID_KEY = "id";
    public static final String CARDTYPE_KEY = "cardType";
    public static final String TITLE_KEY = "title";
    public static final String BACKDROP_PATH_KEY = "backdrop_path";
    public static final String POSTER_PATH_KEY = "poster_path";
    public static final String FILE_PATH_KEY = "file_path";
    public static final String ASPECT_RATIO_KEY = "aspect_ratio";
    public static final String RELEASE_DATE_KEY = "release_date";
    public static final String JOB_KEY = "job";
    public static final String DEPARTMENT_KEY = "department";
    public static final String CHARACTER_KEY = "character";
    public static final String PROFILE_PATH_KEY = "profile_path";
    public static final String NAME_KEY = "name";
    public static final String KEY_KEY = "key";
    public static final String SITE_KEY = "site";
    public static final String TYPE_KEY = "type";
    public static final String GENDER_KEY = "gender";
    public static final String BACKDROP_IMG_FULL_PATH_KEY = "backdrop_img_full_path";
    public static final String POSTER_IMG_FULL_PATH_KEY = "poster_img_full_path";
    public static final String IMG_FULL_PATH_KEY = "img_full_path";
    public static final String MEDIA_PATH_KEY = "media";

    private ArrayList<Map<String,Object>> itemList = new ArrayList<Map<String, Object>>();

    private Activity activity;
    private int total_pages;
    private int total_results;

    private CARDTYPE defaultCardType;
    private Object loadingIndicator;
    private TextView label;
    private String labelBaseStr;
    private RecyclerView recyclerView;

    public enum CARDTYPE
    {
        IN_THEATRES(R.layout.movie_card, 2.5f, MovieViewHolder.class),
        COMING_SOON(R.layout.movie_card, 2.5f, MovieViewHolder.class),
        AS_CAST(R.layout.movie_card, 2.5f, MovieViewHolder.class),
        AS_CREW(R.layout.movie_card, 2.5f, MovieViewHolder.class),
        MOVIE_SEARCH_RESULT(R.layout.movie_card, 2.5f, MovieViewHolder.class),
        CAST(R.layout.person_card, 2.5f, PersonViewHolder.class),
        CREW(R.layout.person_card, 2.5f, PersonViewHolder.class),
        PERSON_SEARCH_RESULT(R.layout.person_card, 2.5f, PersonViewHolder.class),
        DATED_PERSON_PROFILE(R.layout.person_card, 2.5f, PersonViewHolder.class),
        DATED_MOVIE_BACKDROP(R.layout.movie_card, 1.5f, MovieViewHolder.class),
        DATED_MOVIE_POSTER(R.layout.movie_card, 2.5f, MovieViewHolder.class),
        MOVIE_TRAILER_LAUNCHER(R.layout.trailer_card, 1.5f, TrailerViewHolder.class);
        ;

        private static final CARDTYPE[] CARDTYPESARRAY = CARDTYPE.values();

        public static CARDTYPE fromOrdinal(int ordinal)
        {
            return CARDTYPESARRAY[ordinal];
        }

        private int cardLayoutID;
        private float cardsPerViewPort;
        private Class viewHolderClass;

        <VH extends ViewHolder> CARDTYPE(int cardLayoutID, float cardsPerViewPort,  Class<VH> viewHolderClass)
        {
            this.cardLayoutID = cardLayoutID;
            this.cardsPerViewPort = cardsPerViewPort;
            this.viewHolderClass = viewHolderClass;
        }


    }

    public TMDBCardListAdapter(@NonNull Activity activity, @NonNull CARDTYPE defaultCardType, @NonNull Integer recyclerViewID, @Nullable Integer labelID, @Nullable Integer loadingIndicatorID)
    {
        this.activity = activity;

        if (labelID != null)
        {
            this.label = activity.findViewById(labelID);
            this.labelBaseStr = this.label.getText().toString();
        }

        if (loadingIndicatorID != null)
        {
            this.loadingIndicator = activity.findViewById(loadingIndicatorID);
        }

        this.defaultCardType = defaultCardType;

        this.recyclerView = activity.findViewById(recyclerViewID);
        /* TODO: Decide: layout attribs or unified method calls?
            not sure if everything between here and the setAdapter call is really needed (i.e. make it layout attribs), or if the equiv layout attribs are unnneeded.
         */
        this.recyclerView.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        this.recyclerView.setLayoutManager(linearLayoutManager);

        this.recyclerView.setAdapter(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        CARDTYPE cardtype = CARDTYPE.fromOrdinal(viewType);

        View itemView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(cardtype.cardLayoutID, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = (int) (viewGroup.getWidth() * ( 1.0f / cardtype.cardsPerViewPort));
        itemView.setLayoutParams(layoutParams);

        ViewHolder viewHolder = null;
        try
        {
            viewHolder = (ViewHolder)(cardtype.viewHolderClass.getConstructor(View.class).newInstance(itemView));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int itemIndex)
    {
        Map<String, Object> itemData = this.getItem(itemIndex);

        int viewType = this.getItemViewType(itemIndex);
        CARDTYPE cardtype = CARDTYPE.fromOrdinal(viewType);

        switch (cardtype)
        {
            case IN_THEATRES:
            case COMING_SOON:
            case AS_CAST:
            case AS_CREW:
            case MOVIE_SEARCH_RESULT:
            case DATED_MOVIE_BACKDROP:
            case DATED_MOVIE_POSTER:
                bindMovieViewHolder(viewHolder, cardtype, itemData);
                return;
            case CAST:
            case CREW:
            case PERSON_SEARCH_RESULT:
            case DATED_PERSON_PROFILE:
                bindPersonViewHolder(viewHolder, cardtype, itemData);
                return;
            case MOVIE_TRAILER_LAUNCHER:
                bindTrailerViewHolder(viewHolder, cardtype, itemData);
                return;
        }
    }


    private void bindTrailerViewHolder(@NonNull ViewHolder viewHolder, @NonNull CARDTYPE cardType, @NonNull Map<String, Object> itemData)
    {
        TrailerViewHolder trailerViewHolder = (TrailerViewHolder) viewHolder;
        trailerViewHolder.img.setTag(itemData);

        String name = (String)(itemData.get(NAME_KEY));
        String key = (String)(itemData.get(KEY_KEY));
        String site = (String)(itemData.get(SITE_KEY));
        String type = (String)(itemData.get(TYPE_KEY));

        // TODO: Convert to ITagOrderedYouTubeURLImageAndButtonSetter.ITag enums
        int[] orderediTags = {
                46,
                37,
                45,
                22,
                18,
                43
        };

        ITagOrderedYouTubeURLImageAndButtonSetter iTagOrderedYouTubeURLImageAndButtonSetter = new ITagOrderedYouTubeURLImageAndButtonSetter(activity, key, name, orderediTags , trailerViewHolder.playButtonImg, trailerViewHolder.img);
        iTagOrderedYouTubeURLImageAndButtonSetter.setPlayButtonAndStillImage();

        trailerViewHolder.txtMovieTitle.setText(name);
    }

    public void personDetails(View v)
    {
        Map<String, Object> itemData = (Map<String, Object>)(v.getTag());
        Context context = v.getContext();

        Integer id = (Integer)(itemData.get(ID_KEY));
        String name = (String)(itemData.get(NAME_KEY));
        String profile_img_full_path = (String)(itemData.get(PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH));

        if ((id != null) && (!StringUtils.isNullOrEmpty(name)) && (!StringUtils.isNullOrEmpty(profile_img_full_path)))
        {
            Intent intent = new Intent(context, PersonDetailActivity.class);
            intent.putExtra(PersonDetailActivity.ARG_PERSON_ID, id);
            intent.putExtra(PersonDetailActivity.ARG_NAME, name);
            intent.putExtra(PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH, profile_img_full_path);
            context.startActivity(intent);
        }

    }



    private void bindPersonViewHolder(@NonNull ViewHolder viewHolder, @NonNull CARDTYPE cardType, @NonNull Map<String, Object> itemData)
    {
        PersonViewHolder personViewHolder = (PersonViewHolder)viewHolder;

        personViewHolder.img.setTag(itemData);

        String addnlProfileImgRelPath = (String)(itemData.get(FILE_PATH_KEY));
        Number aspectRatioNum = (Number)(itemData.get(ASPECT_RATIO_KEY));

        String profileImgRelPath = (String)(itemData.get(PROFILE_PATH_KEY));
        String personName = (String)(itemData.get(NAME_KEY));
        Integer gender = (Integer) (itemData.get(GENDER_KEY));
        String job = (String) (itemData.get(JOB_KEY));
        String dept = (String) (itemData.get(DEPARTMENT_KEY));
        String character = (String) (itemData.get(CHARACTER_KEY));

        if (aspectRatioNum == null)
        {
            aspectRatioNum = 0.6666666666666667;
        }

        String aspectRatioNumStr = "H," + String.valueOf(aspectRatioNum) + ":1";

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(personViewHolder.layout);
        constraintSet.setDimensionRatio(R.id.img, aspectRatioNumStr);
        constraintSet.applyTo(personViewHolder.layout);

        String imgRelPath = null;
        String secondLineStr = null;

        switch (cardType)
        {
            case DATED_PERSON_PROFILE:
                imgRelPath = addnlProfileImgRelPath;
                break;
            case CAST:
                imgRelPath = profileImgRelPath;
                secondLineStr = character;
                personViewHolder.img.setOnClickListener(this::personDetails);
                break;
            case CREW:
                imgRelPath = profileImgRelPath;
                secondLineStr = job;
                personViewHolder.img.setOnClickListener(this::personDetails);
                break;
            case PERSON_SEARCH_RESULT:
                imgRelPath = profileImgRelPath;
                personViewHolder.img.setOnClickListener(this::personDetails);
                break;
            default:
                String err = this.activity.getString(R.string.unknown_cardlist_type, cardType.name());
                Toast.makeText(this.activity, err, Toast.LENGTH_SHORT).show();
        }

        String imgFullPathURI;

        if (imgRelPath != null)
        {
            imgFullPathURI = TMDB_IMAGE_PATH_PREFIX + PROFILE_IMAGE_SIZE + imgRelPath;
        }
        else
        {
            if (gender != null)
            {
                switch (gender)
                {
                    case 1:
                        imgFullPathURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_female;
                        break;
                    case 2:
                        imgFullPathURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_male;
                        break;
                    case 0:
                    default:
                        imgFullPathURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_person;
                }
            }
            else
            {
                imgFullPathURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_person;

            }
        }
        itemData.put(ARG_PROFILE_IMG_FULL_PATH, imgFullPathURI);

//        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imgFullPathURI))
//                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.DISK_CACHE)
//                .build();
//
//
//        DraweeController controller = Fresco.newDraweeControllerBuilder()
//                .setImageRequest(imageRequest)
//                .build();
//
//        switch (cardType)
//        {
//            case DATED_PERSON_PROFILE:
//                ((AbstractDraweeController) controller).addControllerListener(new ExifDateExtractorControllerListener(imageRequest, personViewHolder.txtSecondLine));
//                break;
//            case CAST:
//            case CREW:
//            case PERSON_SEARCH_RESULT:
//            default:
//        }
//
//        personViewHolder.img.setController(controller);

        personViewHolder.img.setImageURI(imgFullPathURI);

        UIUtils.setTextIfNotNullAndNotEmpty(personViewHolder.txtPersonName, true, personName);
        UIUtils.setTextIfNotNullAndNotEmpty(personViewHolder.txtSecondLine, true, secondLineStr);
    }


    public void movieDetails(View v)
    {
        Map<String,Object> itemData = (Map<String, Object>) v.getTag();
        //Toast.makeText(this, "Would have navigated and shown details for '" + itemData.get("title") + "' with movie id: " + itemData.get("id") ,Toast.LENGTH_LONG).show();

        Context context = v.getContext();

        Integer id = (Integer)(itemData.get("id"));
        String backdrop_img_full_path = (String)(itemData.get("backdrop_img_full_path"));
        String title = (String)(itemData.get("title"));
        String releaseDate = (String)(itemData.get("release_date"));

        if ((id != null) && (!StringUtils.isNullOrEmpty(backdrop_img_full_path)) && (!StringUtils.isNullOrEmpty(title)))
        {
            Intent intent = new Intent(context, MovieDetailActivity.class);
            intent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, id);
            intent.putExtra(MovieDetailFragment.ARG_MOVIE_BACKDROP_URL, backdrop_img_full_path);
            intent.putExtra(MovieDetailFragment.ARG_MOVIE_TITLE, title);
            intent.putExtra(MovieDetailFragment.ARG_MOVIE_RELEASE_DATE, releaseDate);

            context.startActivity(intent);
        }

    }

    private void bindMovieViewHolder(@NonNull ViewHolder viewHolder, @NonNull CARDTYPE cardType, @NonNull Map<String, Object> itemData)
    {
        MovieViewHolder movieViewHolder = (MovieViewHolder)viewHolder;
        movieViewHolder.img.setTag(itemData);

        Integer id = (Integer)(itemData.get(ID_KEY));
        String title = (String)(itemData.get(TITLE_KEY));
        String backDropImgRelPath = (String)(itemData.get(BACKDROP_PATH_KEY));
        String posterImgRelPath = (String)(itemData.get(POSTER_PATH_KEY));
        String imgRelPath = (String)(itemData.get(FILE_PATH_KEY));
        Number aspectRatioNum = (Number)(itemData.get(ASPECT_RATIO_KEY));
        String releaseDateStr = (String) (itemData.get(RELEASE_DATE_KEY));
        String job = (String) (itemData.get(JOB_KEY));
        String dept = (String) (itemData.get(DEPARTMENT_KEY));
        String character = (String) (itemData.get(CHARACTER_KEY));
        Map<String,Object> media = (Map<String, Object>)(itemData.get(MEDIA_PATH_KEY));

        Integer mediaID = null;
        String mediaTitle = null;
        String mediaReleaseDateStr = null;
        String mediaBackdropImageRelPathURI = null;

        if (media != null)
        {
            mediaID = (Integer)(media.get(ID_KEY));
            mediaTitle = (String)(media.get(TITLE_KEY));
            mediaReleaseDateStr = (String)(media.get(RELEASE_DATE_KEY));
            mediaBackdropImageRelPathURI = (String)(media.get(BACKDROP_PATH_KEY));
        }

        String backdropImageFullPathURI;
        if (backDropImgRelPath != null)
        {
            backdropImageFullPathURI = TMDB_IMAGE_PATH_PREFIX + BACKDROP_IMAGE_SIZE + backDropImgRelPath;
        }
        else
        {
            backdropImageFullPathURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.no_movie_backdrop;
        }
        itemData.put(BACKDROP_IMG_FULL_PATH_KEY, backdropImageFullPathURI);

        String mediaBackdropImageFullPathURI;
        if (mediaBackdropImageRelPathURI != null)
        {
            mediaBackdropImageFullPathURI = TMDB_IMAGE_PATH_PREFIX + BACKDROP_IMAGE_SIZE + mediaBackdropImageRelPathURI;
        }
        else
        {
            mediaBackdropImageFullPathURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.no_movie_backdrop;
        }
        itemData.put(BACKDROP_IMG_FULL_PATH_KEY, mediaBackdropImageFullPathURI);

        String posterFullPathImageURI;
        if (posterImgRelPath != null)
        {
            posterFullPathImageURI = TMDB_IMAGE_PATH_PREFIX + POSTER_IMAGE_SIZE + posterImgRelPath;

        }
        else
        {
            posterFullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.no_movie_poster;
        }
        itemData.put(POSTER_IMG_FULL_PATH_KEY, posterFullPathImageURI);

        String fullPathImageURI;
        if (imgRelPath != null)
        {
            fullPathImageURI = TMDB_IMAGE_PATH_PREFIX + BACKDROP_IMAGE_SIZE + imgRelPath;

        }
        else
        {
            fullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.no_movie_backdrop;
        }
        itemData.put(IMG_FULL_PATH_KEY, fullPathImageURI);


        if (aspectRatioNum != null)
        {
            String aspectRatioNumStr = "H," + String.valueOf(aspectRatioNum) + ":1";

            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(movieViewHolder.layout);
            constraintSet.setDimensionRatio(R.id.img, aspectRatioNumStr);
            constraintSet.applyTo(movieViewHolder.layout);
        }

        Integer actualID = null;
        String actualImgPath = null;
        String actualTitle = null;
        String actualReleaseDateStr = null;
        String actualCharacterOrJob = null;
        String actualBackdropImageFullPathURI = null;

        switch (cardType)
        {
            case IN_THEATRES:
                actualID = id;
                actualImgPath = posterFullPathImageURI;
                actualTitle = title;
                actualBackdropImageFullPathURI = backdropImageFullPathURI;
                break;
            case COMING_SOON:
                actualID = id;
                actualImgPath = posterFullPathImageURI;
                actualTitle = title;
                actualReleaseDateStr = getShortThisYearDateStrFromTMDBDateStr(releaseDateStr);
                actualBackdropImageFullPathURI = backdropImageFullPathURI;
                break;
            case AS_CAST:
                actualID = id;
                actualImgPath = posterFullPathImageURI;
                actualTitle = title;
                actualReleaseDateStr = getJustYearDateStrFromTMDBDateStr(releaseDateStr);
                actualCharacterOrJob = character;
                actualBackdropImageFullPathURI = backdropImageFullPathURI;
                break;
            case AS_CREW:
                actualID = id;
                actualImgPath = posterFullPathImageURI;
                actualTitle = title;
                actualReleaseDateStr = getJustYearDateStrFromTMDBDateStr(releaseDateStr);
                actualCharacterOrJob = job;
                actualBackdropImageFullPathURI = backdropImageFullPathURI;
                break;
            case MOVIE_SEARCH_RESULT:
                actualID = id;
                actualImgPath = posterFullPathImageURI;
                actualTitle = title;
                actualReleaseDateStr = getJustYearDateStrFromTMDBDateStr(releaseDateStr);
                actualBackdropImageFullPathURI = backdropImageFullPathURI;
                break;
            case DATED_MOVIE_POSTER:
                actualID = mediaID;
                actualImgPath = fullPathImageURI;
                actualTitle = mediaTitle;
                actualReleaseDateStr = getJustYearDateStrFromTMDBDateStr(mediaReleaseDateStr);
                actualBackdropImageFullPathURI = mediaBackdropImageFullPathURI;
                break;
            case DATED_MOVIE_BACKDROP:
                actualID = mediaID;
                actualImgPath = fullPathImageURI;
                actualTitle = mediaTitle;
                actualReleaseDateStr = getJustYearDateStrFromTMDBDateStr(mediaReleaseDateStr);
                actualBackdropImageFullPathURI = mediaBackdropImageFullPathURI;
                break;
            default:
                String err = this.activity.getString(R.string.unknown_cardlist_type, cardType.name());
                Toast.makeText(this.activity, err, Toast.LENGTH_SHORT).show();
        }

        final Integer finalActualID = actualID;
        final String finalActualTitle = actualTitle;
        final String finalActualReleaseDateStr = actualReleaseDateStr;
        final String finalActualBackdropImageFullPath = actualBackdropImageFullPathURI;

        if ((finalActualID != null) && (finalActualTitle != null) && (finalActualBackdropImageFullPath != null))
        {
            movieViewHolder.img.setOnClickListener(v->{

                //Toast.makeText(this, "Would have navigated and shown details for '" + itemData.get("title") + "' with movie id: " + itemData.get("id") ,Toast.LENGTH_LONG).show();

                Context context = this.activity;

                Intent intent = new Intent(context, MovieDetailActivity.class);
                intent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, finalActualID);
                intent.putExtra(MovieDetailFragment.ARG_MOVIE_BACKDROP_URL, finalActualBackdropImageFullPath);
                intent.putExtra(MovieDetailFragment.ARG_MOVIE_TITLE, finalActualTitle);
                intent.putExtra(MovieDetailFragment.ARG_MOVIE_RELEASE_DATE, finalActualReleaseDateStr);

                context.startActivity(intent);
            });
        }


        movieViewHolder.img.setImageURI(actualImgPath);

        UIUtils.setTextIfNotNullAndNotEmpty(movieViewHolder.txtMovieTitle, true, actualTitle);
        UIUtils.setTextIfNotNullAndNotEmpty(movieViewHolder.txtMovieReleaseDate, true, actualReleaseDateStr);
        UIUtils.setTextIfNotNullAndNotEmpty(movieViewHolder.txtCharacterOrJob, true, actualCharacterOrJob);

    }

    public void setTotal_pages(int total_pages)
    {
        this.total_pages = total_pages;
    }

    public void setTotal_results(int total_results)
    {
        this.total_results = total_results;
    }

    public List<Map<String,Object>> getItemList()
    {
        return this.itemList;
    }

    @Override
    public int getItemCount()
    {
        return this.itemList.size();
    }

    public Map<String,Object> getItem(int i)
    {
        return this.itemList.get(i);
    }

    public void clearAndAddList(List<Map<String,Object>> newItemList)
    {
        this.itemList.clear();

        if ((newItemList != null) && (newItemList.size() > 0))
        {
            this.itemList.addAll(newItemList);

            if (this.label != null)
            {
                this.label.setVisibility(View.VISIBLE);
                this.label.setText(this.labelBaseStr + " (" + this.itemList.size() + ")");
            }

            this.recyclerView.setVisibility(View.VISIBLE);

        }
        else
        {
            if (this.label != null)
            {
                this.label.setVisibility(View.GONE);
            }
            this.recyclerView.setVisibility(View.GONE);
        }


        if (this.loadingIndicator != null)
        {
            if (this.loadingIndicator instanceof SwipeRefreshLayout)
            {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) this.loadingIndicator;
                swipeRefreshLayout.setRefreshing(false);
            }
            else if (this.loadingIndicator instanceof ProgressBar)
            {
                ProgressBar progressBar = (ProgressBar) this.loadingIndicator;
                progressBar.setVisibility(View.GONE);
            }
        }

        this.notifyDataSetChanged();
    }

    public void addList(List<Map<String,Object>> listToAdd)
    {
        if ((listToAdd != null) && (listToAdd.size() > 0))
        {
            int newItemsSize = listToAdd.size();
            int insertPos = itemList.size() - 1;
            itemList.addAll(listToAdd);
            this.label.setText(this.labelBaseStr + " (" + this.itemList.size() + ")");

            this.notifyItemRangeInserted(insertPos, newItemsSize);
        }
    }

    @Override
    public int getItemViewType(int itemIndex)
    {
        Map<String, Object> itemData = this.getItem(itemIndex);
        CARDTYPE cardtype = (CARDTYPE)(itemData.get(CARDTYPE_KEY));
        if (cardtype == null)
        {
            cardtype = this.defaultCardType;
        }

        return cardtype.ordinal();
    }

    public static class TrailerViewHolder extends RecyclerView.ViewHolder
    {
        protected ConstraintLayout layout;
        protected SimpleDraweeView img;
        protected ImageView playButtonImg;
        protected TextView txtMovieTitle;
        protected TextView txtMovieReleaseDate;

        public TrailerViewHolder(View v)
        {
            super(v);
            layout = v.findViewById(R.id.movieCardConstraintLayout);
            img = v.findViewById(R.id.img);
            playButtonImg = v.findViewById(R.id.playbuttonimg);
            txtMovieTitle = v.findViewById(R.id.txtMovieTitle);
            txtMovieReleaseDate = v.findViewById(R.id.txtMovieReleaseDate);
        }
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder
    {
        protected SimpleDraweeView img;
        protected ConstraintLayout layout;
        protected TextView txtMovieTitle;
        protected TextView txtMovieReleaseDate;
        protected TextView txtCharacterOrJob;

        public MovieViewHolder(View v)
        {
            super(v);
            img = v.findViewById(R.id.img);
            layout = v.findViewById(R.id.movieCardConstraintLayout);
            txtMovieTitle = v.findViewById(R.id.txtMovieTitle);
            txtMovieReleaseDate = v.findViewById(R.id.txtMovieReleaseDate);
            txtCharacterOrJob = v.findViewById(R.id.txtCharacterOrJob);
        }
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder
    {
        protected SimpleDraweeView img;
        protected ConstraintLayout layout;
        protected TextView txtPersonName;
        protected TextView txtSecondLine;

        public PersonViewHolder(View v)
        {
            super(v);
            img = v.findViewById(R.id.img);
            layout = v.findViewById(R.id.personCardContraintLayout);
            txtPersonName = v.findViewById(R.id.txtPersonName);
            txtSecondLine = v.findViewById(R.id.txtSecondLine);
        }
    }

}
