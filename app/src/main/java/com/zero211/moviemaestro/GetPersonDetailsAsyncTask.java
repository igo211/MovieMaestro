package com.zero211.moviemaestro;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jayway.jsonpath.DocumentContext;
import com.zero211.utils.http.HttpStringResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.zero211.moviemaestro.DateFormatUtils.getAge;
import static com.zero211.moviemaestro.DateFormatUtils.getDateFromTMDBDateStr;
import static com.zero211.moviemaestro.DateFormatUtils.getLongDateStrFromDate;
import static com.zero211.moviemaestro.StringUtils.getFBURIFromID;
import static com.zero211.moviemaestro.StringUtils.getIMDBURIFromID;
import static com.zero211.moviemaestro.StringUtils.getInstaURIFromID;
import static com.zero211.moviemaestro.StringUtils.getTwitterURIFromID;
import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetPersonDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String PERSON_DETAILS_URL_PATT_STR = "person/" + PERSON_ID_PLACEHOLDER + "?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&append_to_response=images%2Ctagged_images%2Cmovie_credits%2Cexternal_ids";

    private static final String NAME_PATH = "$.name";
    private static final String ALSO_KNOWN_AS_PATH = "$.also_known_as";

    private static final String BIRTHDAY_PATH = "$.birthday";
    private static final String BIRTHPLACE_PATH = "$.place_of_birth";

    private static final String DEATHDAY_PATH = "$.deathday";

    private static final String BIOGRAPHY_PATH = "$.biography";

    private static final String AS_MOVIE_CAST_PATH = "$.movie_credits.cast";
    private static final String AS_MOVIE_CREW_PATH = "$.movie_credits.crew";

    private PersonDetailActivity personDetailActivity;

    public GetPersonDetailsAsyncTask(PersonDetailActivity personDetailActivity)
    {
        super(personDetailActivity, PERSON_DETAILS_URL_PATT_STR.replace(PERSON_ID_PLACEHOLDER, String.valueOf(personDetailActivity.getPersonID())));
        this.personDetailActivity = personDetailActivity;
    }

    @Override
    protected void onPostExecute(HttpStringResponse mergedResponse)
    {
        DocumentContext mergedDoc = mergedResponse.getDocumentContext();

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
        String formattedBirthDateStr = getLongDateStrFromDate(birthDate);

        String birthplace = mergedDoc.read(BIRTHPLACE_PATH);

        String deathDateStr = mergedDoc.read(DEATHDAY_PATH);
        Date deathDate = getDateFromTMDBDateStr(deathDateStr);
        String formattedDeathDate = getLongDateStrFromDate(deathDate);

        String ageStr = null;
        if (birthDate != null)
        {

            int age = getAge(birthDate, deathDate);
            int notDeadAge = getAge(birthDate);

            if (deathDate != null)
            {
                ageStr = String.valueOf(age) + " (would have been " + String.valueOf(notDeadAge) + " if still alive)";
            }
            else
            {
                ageStr = String.valueOf(age);
            }
        }

        String biography = mergedDoc.read(BIOGRAPHY_PATH);

        List<Map<String,Object>> asMovieCast = mergedDoc.read(AS_MOVIE_CAST_PATH);
        List<Map<String,Object>> asMovieCrew = mergedDoc.read(AS_MOVIE_CREW_PATH);

        TextView lblAlsoKnownAs = personDetailActivity.findViewById(R.id.lblAlsoKnownAs);
        TextView txtAlsoKnownAs = personDetailActivity.findViewById(R.id.txtAlsoKnownAs);

        TextView lblBirth = personDetailActivity.findViewById(R.id.lblBorn);
        TextView txtBirthdate = personDetailActivity.findViewById(R.id.txtBirthDate);
        TextView txtPlaceOfBirth = personDetailActivity.findViewById(R.id.txtBirthPlace);

        TextView lblAge = personDetailActivity.findViewById(R.id.lblAge);
        TextView txtAge = personDetailActivity.findViewById(R.id.txtAge);

        TextView lblDeath = personDetailActivity.findViewById(R.id.lblDeath);
        TextView txtDeathdate = personDetailActivity.findViewById(R.id.txtDeathDate);

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
        setTextIfNotNullAndNotEmpty(lblBiography, txtBiography, biography);


        MovieListAdapter asCastMovieListAdapter = new MovieListAdapter(MovieListAdapter.MOVIE_TYPE.AS_CAST, personDetailActivity, R.id.rvAsCast, R.id.lblAsCast, null);
        Collections.sort(asMovieCast, new MovieUtils.MovieReleaseDateComparator(true));
        asCastMovieListAdapter.clearAndAddList(asMovieCast);

        MovieListAdapter asCrewMovieAdapter = new MovieListAdapter(MovieListAdapter.MOVIE_TYPE.AS_CREW, personDetailActivity, R.id.rvAsCrew, R.id.lblAsCrew, R.id.pgLoading);

        Collections.sort(asMovieCrew, new PersonUtils.CrewDeptAndJobComparator());
        List<Map<String,Object>> mergedAsMovieCrew = PersonUtils.CrewListMerge(asMovieCrew);
        Collections.sort(mergedAsMovieCrew, new MovieUtils.MovieReleaseDateComparator(true));
        asCrewMovieAdapter.clearAndAddList(mergedAsMovieCrew);

        ViewGroup rootView = (ViewGroup) ((ViewGroup) personDetailActivity.findViewById(android.R.id.content)).getChildAt(0);
        setSocialButtons(mergedDoc, rootView);

    }
}
