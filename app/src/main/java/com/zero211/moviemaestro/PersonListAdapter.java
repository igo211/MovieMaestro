package com.zero211.moviemaestro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.zero211.moviemaestro.PersonDetailActivity.ARG_PROFILE_IMG_FULL_PATH;


public class PersonListAdapter extends RecyclerView.Adapter<PersonListAdapter.PersonViewHolder>
{
    private static final String PROFILE_IMAGE_SIZE = "h632";

    public enum PERSON_TYPE
    {
        CAST,
        CREW
    }

    private PERSON_TYPE personType;

    private ArrayList<Map<String,Object>> peopleList = new ArrayList<Map<String, Object>>();

    public PersonListAdapter(PERSON_TYPE personType)
    {
        this.personType = personType;
    }

    public void clearAndAddPeople(List<Map<String,Object>> peopleToAdd)
    {
        peopleList.clear();
        peopleList.addAll(peopleToAdd);
        this.notifyDataSetChanged();
    }

    public void addPeople(List<Map<String,Object>> peopleToAdd)
    {
        int insertPos = peopleList.size() - 1;
        peopleList.addAll(peopleToAdd);
        this.notifyItemRangeInserted(insertPos, peopleToAdd.size());
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
        Map<String, Object> itemData = peopleList.get(i);

        String profileImgRelPath = (String)(itemData.get("profile_path"));


        String profileFullPathImageURI;
        if (profileImgRelPath != null)
        {
            profileFullPathImageURI = "https://image.tmdb.org/t/p/" + PROFILE_IMAGE_SIZE + profileImgRelPath;

        }
        else
        {
            int gender = (Integer)(itemData.get("gender"));
            switch (gender)
            {
                case 1:
                    profileFullPathImageURI = "res:///" + R.drawable.unknown_female;
                    break;
                case 2:
                    profileFullPathImageURI = "res:///" + R.drawable.unknown_male;
                    break;
                case 0:
                default:
                    profileFullPathImageURI = "res:///" + R.drawable.unknown_person;
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
        return peopleList.size();
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
