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

import static com.zero211.moviemaestro.DateFormatUtils.*;
import static com.zero211.moviemaestro.UIUtils.*;
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

    private static final String IMAGES_PROFILES_PATH = "$.images.profiles[?(@.aspect_ratio == 0.66666666666667)]";
    private static final String IMAGES_TAGGED_MOVIE_PATH = "$.tagged_images.results[?((@.media_type == 'movie') && (@.aspect_ratio == 1.7777777777778))]";


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
                ageStr = String.valueOf(age) + " (" + personDetailActivity.getString(R.string.would_have_been_age, notDeadAge) + ")";
            }
            else
            {
                ageStr = String.valueOf(age);
            }
        }

        String biography = mergedDoc.read(BIOGRAPHY_PATH);

        List<Map<String,Object>> asMovieCast = mergedDoc.read(AS_MOVIE_CAST_PATH);
        List<Map<String,Object>> asMovieCrew = mergedDoc.read(AS_MOVIE_CREW_PATH);

        List<Map<String,Object>> profileImages = mergedDoc.read(IMAGES_PROFILES_PATH);
        List<Map<String,Object>> taggedMovieImages = mergedDoc.read(IMAGES_TAGGED_MOVIE_PATH);

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

        UIUtils.setTextIfNotNullAndNotEmpty(lblAlsoKnownAs, txtAlsoKnownAs, firstAKAForLocale);

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


        TMDBCardListAdapter asCastMovieListAdapter = new TMDBCardListAdapter(personDetailActivity, TMDBCardListAdapter.CARDTYPE.AS_CAST,  R.id.rvAsCast, R.id.lblAsCast, null);
        Collections.sort(asMovieCast, new MovieUtils.MovieReleaseDateComparator(true));
        asCastMovieListAdapter.clearAndAddList(asMovieCast);

        TMDBCardListAdapter asCrewMovieAdapter = new TMDBCardListAdapter(personDetailActivity, TMDBCardListAdapter.CARDTYPE.AS_CREW,  R.id.rvAsCrew, R.id.lblAsCrew, null);
        Collections.sort(asMovieCrew, new PersonUtils.CrewDeptAndJobComparator());
        List<Map<String,Object>> mergedAsMovieCrew = PersonUtils.CrewListMerge(asMovieCrew);
        Collections.sort(mergedAsMovieCrew, new MovieUtils.MovieReleaseDateComparator(true));
        asCrewMovieAdapter.clearAndAddList(mergedAsMovieCrew);

        TMDBCardListAdapter profilesListAdapter = new TMDBCardListAdapter(personDetailActivity, TMDBCardListAdapter.CARDTYPE.DATED_PERSON_PROFILE,  R.id.rvProfiles, R.id.lblProfiles, null);
        profilesListAdapter.clearAndAddList(profileImages);

        TMDBCardListAdapter movieStillsListAdapter = new TMDBCardListAdapter(personDetailActivity, TMDBCardListAdapter.CARDTYPE.DATED_MOVIE_BACKDROP,  R.id.rvMovieStills, R.id.lblMovieStills, R.id.pgLoading);
        movieStillsListAdapter.clearAndAddList(taggedMovieImages);

        ViewGroup rootView = (ViewGroup) ((ViewGroup) personDetailActivity.findViewById(android.R.id.content)).getChildAt(0);
        setSocialButtons(mergedDoc, rootView);

    }
}
