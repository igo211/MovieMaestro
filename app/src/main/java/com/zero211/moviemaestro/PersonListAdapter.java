package com.zero211.moviemaestro;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import static com.zero211.moviemaestro.PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH;

// TODO: Refactor this class and MovieListAdapter to have an abstract parent class (AbstractPosterListAdapter) that contains the object list, the add* methods, the loadingIndicator and the viewsToMakeVisible for all list adapters

public class PersonListAdapter extends AbstractTMDBCardListAdapter<PersonListAdapter.PersonViewHolder>
{
    private static final String PROFILE_IMAGE_SIZE = "h632";

    public enum PERSON_TYPE
    {
        CAST,
        CREW,
        SEARCH_RESULT
    }

    private PERSON_TYPE personType;


    public PersonListAdapter(@NonNull PERSON_TYPE personType, @NonNull Activity activity, @NonNull Integer recyclerViewID, @Nullable Integer labelID, @Nullable Integer loadingIndicatorID)
    {
        super(activity, R.layout.person_card, recyclerViewID, labelID, loadingIndicatorID);

        this.personType = personType;
    }


    public void clearAndAddList(List<Map<String,Object>> newItemList)
    {
        ArrayList<Map<String,Object>> processedNewItemList = new ArrayList<>();

        switch (this.personType)
        {

        }

        super.clearAndAddList(newItemList);
    }

    public void addList(List<Map<String,Object>> listToAdd)
    {
        super.addList(listToAdd);
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View itemView = this.getItemView(viewGroup);
        return new PersonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder personViewholder, int i)
    {
        Map<String, Object> itemData = this.getItem(i);

        String profileImgRelPath = (String)(itemData.get("profile_path"));

        String profileFullPathImageURI;
        if (profileImgRelPath != null)
        {
            profileFullPathImageURI = TMDB_IMAGE_PATH_PREFIX + PROFILE_IMAGE_SIZE + profileImgRelPath;
        }
        else
        {
            if (this.personType == PERSON_TYPE.SEARCH_RESULT)
            {
                profileFullPathImageURI = FRESCO_RESOURCES_IMAGE_PATH_PREFIX + R.drawable.unknown_person;

            }
            else
            {
                int gender = (Integer) (itemData.get("gender"));
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
//                    case "Writing":
//                        if (job.equals("Writer"))
//                        {
//                            secondLineStr = job;
//                        }
//                        else
//                        {
//                            secondLineStr = "Writer (" + job + ")";
//                        }
//
//                        break;
                    default:
                        secondLineStr = job;
                }
                break;
            case SEARCH_RESULT:
            default:
                secondLineStr = "";
        }

        personViewholder.txtSecondLine.setText(secondLineStr);
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
