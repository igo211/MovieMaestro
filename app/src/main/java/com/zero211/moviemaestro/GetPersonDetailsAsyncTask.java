package com.zero211.moviemaestro;

import android.view.View;
import android.widget.TextView;

import com.jayway.jsonpath.DocumentContext;

import java.util.Date;
import java.util.List;

import static com.zero211.moviemaestro.DateFormatUtils.*;
import static com.zero211.moviemaestro.StringUtils.*;

import static com.zero211.utils.http.HttpUtils.*;

public class GetPersonDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String PERSON_DETAILS_URL_PATT_STR = "person/" + PERSON_ID_PLACEHOLDER + "?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&append_to_response=images%2Ccombined_credits%2Cexternal_ids%2Ctagged_images";

    private static final String BIRTHDAY_PATH = "$.birthday";
    private static final String BIRTHPLACE_PATH = "$.place_of_birth";

    private static final String DEATHDAY_PATH = "$.deathday";

    private static final String BIOGRAPHY_PATH = "$.biography";

    private static final String HOMEPAGE_PATH = "$.homepage";
    private static final String IMDB_ID_PATH = "$.external_ids.imdb_id";
    private static final String FACEBOOK_ID_PATH = "$.external_ids.facebook_id";
    private static final String INSTA_ID_PATH = "$.external_ids.instagram_id";
    private static final String TWITTER_ID_PATH = "$.external_ids.twitter_id";

    private PersonDetailActivity personDetailActivity;

    public GetPersonDetailsAsyncTask(PersonDetailActivity personDetailActivity)
    {
        super(personDetailActivity, PERSON_DETAILS_URL_PATT_STR.replace(PERSON_ID_PLACEHOLDER, String.valueOf(personDetailActivity.getPersonID())));
        this.personDetailActivity = personDetailActivity;
    }

    @Override
    protected void onPostExecute(DocumentContext mergedDoc)
    {

        String internal_err_msg = mergedDoc.read(INTERNAL_ERROR_PATH);
        List<String> TMDB_err_msgs = mergedDoc.read(ERRORS_PATH);
        Integer TMDB_status_code = mergedDoc.read(STATUS_CODE_PATH);
        String TMDB_status_msg = mergedDoc.read(STATUS_MESSAGE_PATH);

        // TODO: Handle the various error cases... push error handling code up to parent class?

        String birthDateStr = mergedDoc.read(BIRTHDAY_PATH);
        Date birthDate = getDateFromTMDBDateStr(birthDateStr);
        String formattedBirthDateStr = getShortDateStrFromDate(birthDate);

        String birthplace = mergedDoc.read(BIRTHPLACE_PATH);

        String deathDateStr = mergedDoc.read(DEATHDAY_PATH);
        Date deathDate = getDateFromTMDBDateStr(deathDateStr);
        String formattedDeathDate = getShortDateStrFromDate(deathDate);

        String ageStr = null;
        if (birthDate != null)
        {
            int age = getAge(birthDate, deathDate);
            ageStr = String.valueOf(age);
        }

        String biography = mergedDoc.read(BIOGRAPHY_PATH);

        String homepage = mergedDoc.read(HOMEPAGE_PATH);
        String fb_id = mergedDoc.read(FACEBOOK_ID_PATH);
        String insta_id = mergedDoc.read(INSTA_ID_PATH);
        String twitter_id = mergedDoc.read(TWITTER_ID_PATH);
        String imdb_id = mergedDoc.read(IMDB_ID_PATH);

        TextView lblBirth = personDetailActivity.findViewById(R.id.lblBorn);
        TextView txtBirthdate = personDetailActivity.findViewById(R.id.txtBirthDate);
        TextView txtPlaceOfBirth = personDetailActivity.findViewById(R.id.txtBirthPlace);

        TextView lblDeath = personDetailActivity.findViewById(R.id.lblDeath);
        TextView txtDeathdate = personDetailActivity.findViewById(R.id.txtDeathDate);

        TextView lblAge = personDetailActivity.findViewById(R.id.lblAge);
        TextView txtAge = personDetailActivity.findViewById(R.id.txtAge);

        TextView lblHomepage = personDetailActivity.findViewById(R.id.lblHomepage);
        TextView txtHomepage = personDetailActivity.findViewById(R.id.txtHomepage);

        TextView lblFB = personDetailActivity.findViewById(R.id.lblFB);
        TextView txtFB = personDetailActivity.findViewById(R.id.txtFB);

        TextView lblInsta = personDetailActivity.findViewById(R.id.lblInsta);
        TextView txtInsta = personDetailActivity.findViewById(R.id.txtInsta);

        TextView lblTwitter = personDetailActivity.findViewById(R.id.lblTwitter);
        TextView txtTwitter = personDetailActivity.findViewById(R.id.txtTwitter);

        TextView lblIMDB = personDetailActivity.findViewById(R.id.lblIMDB);
        TextView txtIMDB = personDetailActivity.findViewById(R.id.txtIMDB);

        TextView lblBiography = personDetailActivity.findViewById(R.id.lblBiography);
        TextView txtBiography = personDetailActivity.findViewById(R.id.txtBiography);

        setTextIfNotNullAndNotEmpty(txtBirthdate, formattedBirthDateStr);
        setTextIfNotNullAndNotEmpty(txtPlaceOfBirth, birthplace);
        setLabelVisibilityBasedOnStringValues(lblBirth, false, formattedBirthDateStr, birthplace);

        setTextIfNotNullAndNotEmpty(lblDeath, txtDeathdate, formattedDeathDate);

        if (lblDeath.getVisibility() == View.VISIBLE)
        {
            lblAge.setText(R.string.age_at_death_label);
        }

        setTextIfNotNullAndNotEmpty(lblAge, txtAge, ageStr);

        setTextIfNotNullAndNotEmpty(lblHomepage, txtHomepage, homepage);

        setTextIfNotNullAndNotEmpty(lblFB, txtFB, getFBURLFromID(fb_id));
        setTextIfNotNullAndNotEmpty(lblInsta, txtInsta, getInstaURLFromID(insta_id));
        setTextIfNotNullAndNotEmpty(lblTwitter, txtTwitter, getTwitterURLFromID(twitter_id));
        setTextIfNotNullAndNotEmpty(lblIMDB, txtIMDB, getIMDBURLFromID(imdb_id));

        setTextIfNotNullAndNotEmpty(lblBiography, txtBiography, biography);

    }
}
