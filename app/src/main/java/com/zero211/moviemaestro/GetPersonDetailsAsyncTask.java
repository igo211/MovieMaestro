package com.zero211.moviemaestro;

import android.widget.TextView;

import com.jayway.jsonpath.DocumentContext;

import java.util.List;

import static com.zero211.moviemaestro.DateFormatUtils.*;
import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetPersonDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String PERSON_DETAILS_URL_PATT_STR = "https://api.themoviedb.org/3/person/" + PERSON_ID_PLACEHOLDER + "?api_key=" + API_KEY + "&language=" + LOCALE_STR + "&append_to_response=images%2Ccombined_credits%2Cexternal_ids%2Ctagged_images";

    private static final String BIRTHDAY_PATH = "$.birthday";
    private static final String BIRTHPLACE_PATH = "$.place_of_birth";

    private static final String DEATHDAY_PATH = "$.deathday";

    private static final String BIOGRAPHY_PATH = "$.biography";

    private static final String HOMEPAGE_PATH = "$.homepage";
    private static final String IMDB_ID_PATH = "$.external_ids.imdb_id";
    private static final String FACEBOOK_ID_PATH = "$.external_ids.facebook_id";
    private static final String INSTA_ID_PATH = "$.external_ids.instagram_id";
    private static final String TWITTER_ID_PATH = "$.external_ids.twitter_id";

    private int personID;
    private String name;
    private String profile_img_full_path;

    private String urlStr;
    private PersonDetailActivity personDetailActivity;

    public GetPersonDetailsAsyncTask(PersonDetailActivity personDetailActivity)
    {
        this.personDetailActivity = personDetailActivity;

        this.personID = personDetailActivity.getPersonID();
        this.name = personDetailActivity.getName();
        this.profile_img_full_path = personDetailActivity.getProfile_img_full_path();

        String personIDStr = String.valueOf(personID);
        urlStr = PERSON_DETAILS_URL_PATT_STR.replace(PERSON_ID_PLACEHOLDER, personIDStr);
    }

    @Override
    protected DocumentContext doInBackground(String... params)
    {
        // No execute-time params, since all are required in the constructor.

        DocumentContext result = super.doInBackground(urlStr,"1","1");

        return result;
    }

    @Override
    protected void onPostExecute(DocumentContext mergedDoc)
    {

        String internal_err_msg = mergedDoc.read(INTERNAL_ERROR_PATH);
        List<String> TMDB_err_msgs = mergedDoc.read(ERRORS_PATH);
        Integer TMDB_status_code = mergedDoc.read(STATUS_CODE_PATH);
        String TMDB_status_msg = mergedDoc.read(STATUS_MESSAGE_PATH);

        // TODO: Handle the various error cases... push error handling code up to parent class?


        TextView lblBirth = personDetailActivity.findViewById(R.id.lblBorn);
        TextView txtBirthdate = personDetailActivity.findViewById(R.id.txtBirthDate);
        TextView txtPlaceOfBirth = personDetailActivity.findViewById(R.id.txtBirthPlace);

        TextView lblDeath = personDetailActivity.findViewById(R.id.lblDeath);
        TextView txtDeathdate = personDetailActivity.findViewById(R.id.txtDeathDate);

        TextView txtBiography = personDetailActivity.findViewById(R.id.txtBiography);

//        TextView txtHomePage = personDetailActivity.findViewById(R.id.txtBudget);


        String formattedBirthDate = getShortDateFromTMDBDateStr(mergedDoc.read(BIRTHDAY_PATH));
        String birthplace = mergedDoc.read(BIRTHPLACE_PATH);
        String formattedDeathDate = getShortDateFromTMDBDateStr(mergedDoc.read(DEATHDAY_PATH));
        String biography = mergedDoc.read(BIOGRAPHY_PATH);

        setTextIfNotNullorEmpty(txtBirthdate, formattedBirthDate);
        setTextIfNotNullorEmpty(txtPlaceOfBirth, birthplace);
        setLabelVisibilityBasedOnValues(lblBirth, formattedBirthDate, birthplace);

        setTextIfNotNullorEmpty(lblDeath, txtDeathdate, formattedDeathDate);
        setTextIfNotNullorEmpty(txtBiography, biography);

    }
}
