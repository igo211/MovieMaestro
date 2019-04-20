package com.zero211.moviemaestro;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jayway.jsonpath.DocumentContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zero211.moviemaestro.DateFormatUtils.*;
import static com.zero211.moviemaestro.StringUtils.*;

import static com.zero211.utils.http.HttpUtils.*;

public class GetPersonDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String PERSON_DETAILS_URL_PATT_STR = "person/" + PERSON_ID_PLACEHOLDER + "?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&append_to_response=images%2Cmovie_credits%2Cexternal_ids%2Ctagged_images";

    private static final String NAME_PATH = "$.name";
    private static final String ALSO_KNOWN_AS_PATH = "$.also_known_as";

    private static final String BIRTHDAY_PATH = "$.birthday";
    private static final String BIRTHPLACE_PATH = "$.place_of_birth";

    private static final String DEATHDAY_PATH = "$.deathday";

    private static final String BIOGRAPHY_PATH = "$.biography";

    private static final String HOMEPAGE_PATH = "$.homepage";
    private static final String IMDB_ID_PATH = "$.external_ids.imdb_id";
    private static final String FACEBOOK_ID_PATH = "$.external_ids.facebook_id";
    private static final String INSTA_ID_PATH = "$.external_ids.instagram_id";
    private static final String TWITTER_ID_PATH = "$.external_ids.twitter_id";

    private static final String AS_MOVIE_CAST_PATH = "$.movie_credits.cast";
    private static final String AS_MOVIE_CREW_PATH = "$.movie_credits.crew";

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

        ArrayList<String> alsoKnownAsList = mergedDoc.read(ALSO_KNOWN_AS_PATH);

        String firstAKAForLocale = null;

        if ((alsoKnownAsList != null) && (alsoKnownAsList.size() > 0))
        {
            String firstAlsoKnownAs = alsoKnownAsList.get(0);
            if (StandardCharsets.US_ASCII.newEncoder().canEncode(firstAlsoKnownAs))
            {
                String name = mergedDoc.read(NAME_PATH);
                if (!(name.trim().equalsIgnoreCase(firstAlsoKnownAs.trim())))
                {
                    firstAKAForLocale = firstAlsoKnownAs;
                }
            }
        }


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

        List<Map<String,Object>> asMovieCast = mergedDoc.read(AS_MOVIE_CAST_PATH);
        List<Map<String,Object>> asMovieCrew = mergedDoc.read(AS_MOVIE_CREW_PATH);

        String homepage = mergedDoc.read(HOMEPAGE_PATH);
        String fb_id = mergedDoc.read(FACEBOOK_ID_PATH);
        String insta_id = mergedDoc.read(INSTA_ID_PATH);
        String twitter_id = mergedDoc.read(TWITTER_ID_PATH);
        String imdb_id = mergedDoc.read(IMDB_ID_PATH);

        TextView lblAlsoKnownAs = personDetailActivity.findViewById(R.id.lblAlsoKnownAs);
        TextView txtAlsoKnownAs = personDetailActivity.findViewById(R.id.txtAlsoKnownAs);

        TextView lblBirth = personDetailActivity.findViewById(R.id.lblBorn);
        TextView txtBirthdate = personDetailActivity.findViewById(R.id.txtBirthDate);
        TextView txtPlaceOfBirth = personDetailActivity.findViewById(R.id.txtBirthPlace);

        TextView lblAge = personDetailActivity.findViewById(R.id.lblAge);
        TextView txtAge = personDetailActivity.findViewById(R.id.txtAge);

        TextView lblDeath = personDetailActivity.findViewById(R.id.lblDeath);
        TextView txtDeathdate = personDetailActivity.findViewById(R.id.txtDeathDate);

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

        setTextIfNotNullAndNotEmpty(lblAlsoKnownAs, txtAlsoKnownAs, firstAKAForLocale);

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


        if ((asMovieCast != null) && (asMovieCast.size() > 0))
        {
            TextView lblAsCast = personDetailActivity.findViewById(R.id.lblAsCast);
            RecyclerView rvAsCast = personDetailActivity.findViewById(R.id.rvAsCast);
            rvAsCast.setHasFixedSize(true);
            LinearLayoutManager asCastLLM = new LinearLayoutManager(personDetailActivity);
            asCastLLM.setOrientation(RecyclerView.HORIZONTAL);
            rvAsCast.setLayoutManager(asCastLLM);
            MovieListAdapter asCastMovieListAdapter = new MovieListAdapter(MovieListAdapter.MOVIE_TYPE.AS_CAST, null, lblAsCast);
            rvAsCast.setAdapter(asCastMovieListAdapter);
            asCastMovieListAdapter.clearAndAddMovies(asMovieCast);
        }

        if ((asMovieCrew != null) && (asMovieCrew.size() > 0))
        {
            TextView lblAsCrew = personDetailActivity.findViewById(R.id.lblAsCrew);
            RecyclerView rvAsCrew = personDetailActivity.findViewById(R.id.rvAsCrew);
            rvAsCrew.setHasFixedSize(true);
            LinearLayoutManager asCrewLLM = new LinearLayoutManager(personDetailActivity);
            asCrewLLM.setOrientation(RecyclerView.HORIZONTAL);
            rvAsCrew.setLayoutManager(asCrewLLM);
            MovieListAdapter asCrewMovieAdapter = new MovieListAdapter(MovieListAdapter.MOVIE_TYPE.AS_CREW, null, lblAsCrew);
            rvAsCrew.setAdapter(asCrewMovieAdapter);
            asCrewMovieAdapter.clearAndAddMovies(asMovieCrew);
        }

        ProgressBar pgLoading = personDetailActivity.findViewById(R.id.pgLoading);
        pgLoading.setVisibility(View.GONE);
    }
}
