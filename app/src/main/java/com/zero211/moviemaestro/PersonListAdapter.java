package com.zero211.moviemaestro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static com.zero211.moviemaestro.PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH;

// TODO: Refactor this class and MovieListAdapter to have an abstract parent class (AbstractPosterListAdapter) that contains the object list, the add* methods, the loadingIndicator and the viewsToMakeVisible for all list adapters

public class PersonListAdapter extends RecyclerView.Adapter<PersonListAdapter.PersonViewHolder>
{
    private static final String TMDB_IMAGE_PATH_PREFIX = "https://image.tmdb.org/t/p/";
    private static final String FRESCO_RESOURCES_IMAGE_PATH_PREFIX = "res:///";

    private static final String PROFILE_IMAGE_SIZE = "h632";

    public enum PERSON_TYPE
    {
        CAST,
        CREW
    }


    private PERSON_TYPE personType;

    private ArrayList<Map<String,Object>> itemList = new ArrayList<Map<String, Object>>();
    private int total_pages;
    private int total_results;
    private Object loadingIndicator;
    private View[] viewsToMakeVisibleWhenDone;

    public PersonListAdapter(PERSON_TYPE personType, Object loadingIndicator, View... viewsToMakeVisibleWhenDone)
    {
        this.personType = personType;
        this.loadingIndicator = loadingIndicator;
        this.viewsToMakeVisibleWhenDone = viewsToMakeVisibleWhenDone;
    }

    public void clearAndAddList(List<Map<String,Object>> newItemList)
    {
        this.itemList.clear();
        this.itemList.addAll(newItemList);
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

    public void addList(List<Map<String,Object>> listToAdd)
    {
        int insertPos = itemList.size() - 1;
        itemList.addAll(listToAdd);
        this.notifyItemRangeInserted(insertPos, listToAdd.size());
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View itemView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.person_card, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.width = (int) (viewGroup.getWidth() * 0.4);
        itemView.setLayoutParams(layoutParams);

        return new PersonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder personViewholder, int i)
    {
        Map<String, Object> itemData = itemList.get(i);

        String profileImgRelPath = (String)(itemData.get("profile_path"));


        String profileFullPathImageURI;
        if (profileImgRelPath != null)
        {
            profileFullPathImageURI = TMDB_IMAGE_PATH_PREFIX + PROFILE_IMAGE_SIZE + profileImgRelPath;

        }
        else
        {
            int gender = (Integer)(itemData.get("gender"));
            switch (gender)
            {
                case 1:
                    profileFullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_female;
                    break;
                case 2:
                    profileFullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_male;
                    break;
                case 0:
                default:
                    profileFullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_person;
            }

        }

        itemData.put(ARG_PROFILE_IMG_FULL_PATH, profileFullPathImageURI);


        personViewholder.imgPersonProfile.setImageURI(profileFullPathImageURI);

        personViewholder.imgPersonProfile.setTag(itemData);

        String personName = (String)(itemData.get("name"));
        personViewholder.txtPersonName.setText(personName);

        String secondLineStr = "Unknown";
        switch (this.personType)
        {
            case CAST:
                secondLineStr = (String) (itemData.get("character"));
                break;
            case CREW:
                String job = (String) (itemData.get("job"));
                String dept = (String) (itemData.get("department"));
                switch (dept)
                {
                    case "Writing":
                        if (job.equals("Writer"))
                        {
                            secondLineStr = job;
                        }
                        else
                        {
                            secondLineStr = "Writer (" + job + ")";
                        }

                        break;
                    default:
                        secondLineStr = job;
                }
                break;
        }

        personViewholder.txtSecondLine.setText(secondLineStr);
    }

    @Override
    public int getItemCount()
    {
        return itemList.size();
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder
    {
        protected SimpleDraweeView imgPersonProfile;
        protected TextView txtPersonName;
        protected TextView txtSecondLine;

        public PersonViewHolder(View v)
        {
            super(v);
            imgPersonProfile = v.findViewById(R.id.imgPersonPoster);
            txtPersonName = v.findViewById(R.id.txtPersonName);
            txtSecondLine = v.findViewById(R.id.txtSecondLine);
        }
    }
}
