package com.zero211.moviemaestro;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jayway.jsonpath.DocumentContext;
import com.zero211.utils.http.HttpStringResponse;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.zero211.moviemaestro.StringUtils.getFBURLFromID;
import static com.zero211.moviemaestro.StringUtils.getIMDBURLFromID;
import static com.zero211.moviemaestro.StringUtils.getInstaURLFromID;
import static com.zero211.moviemaestro.StringUtils.getTwitterURLFromID;
import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMovieDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MOVIE_DETAILS_URL_PATT_STR = "movie/" + MOVIE_ID_PLACEHOLDER + "?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&append_to_response=videos%2Ccredits%2Cexternal_ids";

    private static final String OVERVIEW_PATH = "$.overview";
    private static final String BUDGET_PATH = "$.budget";
    private static final String REVENUE_PATH = "$.revenue";
    private static final String RUNTIME_PATH = "$.runtime";
    private static final String HOMEPAGE_PATH = "$.homepage";
    private static final String TAGLINE_PATH = "$.tagline";

    private static final String PRODUCTION_COMPANIES_PATH = "$.production_companies";
    private static final String GENRES_PATH = "$.genres";
    private static final String VIDEOS_PATH = "$.videos.results";
    private static final String CAST_PATH = "$.credits.cast";
    private static final String CREW_PATH = "$.credits.crew";

    private static final String EXEC_PRODUCERS_PATH = "$.credits.crew[?(@.job == 'Executive Producer')]";
    private static final String PRODUCERS_PATH = "$.credits.crew[?(@.job == 'Producer')]";
    private static final String CASTINGS_PATH = "$.credits.crew[?(@.job == 'Casting')]";
    private static final String DIRECTORS_PATH = "$.credits.crew[?(@.job == 'Director')]";
    private static final String DOPS_PATH = "$.credits.crew[?(@.job == 'Director of Photography')]";
    private static final String EDITORS_PATH = "$.credits.crew[?(@.job == 'Editor')]";
    private static final String COMPOSERS_PATH = "$.credits.crew[?(@.job == 'Original Music Composer')]";
    private static final String MUSIC_SUPERS_PATH = "$.credits.crew[?(@.job == 'Music Supervisor')]";
    private static final String WRITERS_PATH = "$.credits.crew[?(@.department == 'Writing')]";
    private static final String SCREENPLAY_WRITERS_PATH = "$.credits.crew[?(@.job == 'Screenplay')]";
    private static final String STORY_WRITERS_PATH = "$.credits.crew[?(@.job == 'Story')]";

    private static final String IMDB_ID_PATH = "$.external_ids.imdb_id";
    private static final String FACEBOOK_ID_PATH = "$.external_ids.facebook_id";
    private static final String INSTA_ID_PATH = "$.external_ids.instagram_id";
    private static final String TWITTER_ID_PATH = "$.external_ids.twitter_id";

    private MovieDetailFragment movieDetailFragment;

    public GetMovieDetailsAsyncTask(MovieDetailFragment movieDetailFragment)
    {
        super(movieDetailFragment.getActivity(), MOVIE_DETAILS_URL_PATT_STR.replace(MOVIE_ID_PLACEHOLDER, String.valueOf(movieDetailFragment.getMovieID())));
        this.movieDetailFragment = movieDetailFragment;
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

        // Process the results and update the UI

        Activity activity = movieDetailFragment.getActivity();

        FloatingActionButton fab = activity.findViewById(R.id.run_trailer_fab);

        View fragmentView = movieDetailFragment.getView();

        TextView txtTagline = fragmentView.findViewById(R.id.txtTagline);
        TextView txtOverview = fragmentView.findViewById(R.id.txtMovieOverview);

        TextView txtRuntime = fragmentView.findViewById(R.id.txtRuntime);
        TextView txtBudget = fragmentView.findViewById(R.id.txtBudget);
        TextView txtRevenue = fragmentView.findViewById(R.id.txtRevenue);

        TextView txtProductionCos = fragmentView.findViewById(R.id.txtProductionCos);
        TextView txtDirectors = fragmentView.findViewById(R.id.txtDirector);
        TextView txtProducers = fragmentView.findViewById(R.id.txtProducers);
        TextView txtDOPs = fragmentView.findViewById(R.id.txtDOP);
        TextView txtWriters = fragmentView.findViewById(R.id.txtWriters);

        TextView lblHomepage = fragmentView.findViewById(R.id.lblHomepage);
        TextView txtHomepage = fragmentView.findViewById(R.id.txtHomepage);

        TextView lblFB = fragmentView.findViewById(R.id.lblFB);
        TextView txtFB = fragmentView.findViewById(R.id.txtFB);

        TextView lblInsta = fragmentView.findViewById(R.id.lblInsta);
        TextView txtInsta = fragmentView.findViewById(R.id.txtInsta);

        TextView lblTwitter = fragmentView.findViewById(R.id.lblTwitter);
        TextView txtTwitter = fragmentView.findViewById(R.id.txtTwitter);

        TextView lblIMDB = fragmentView.findViewById(R.id.lblIMDB);
        TextView txtIMDB = fragmentView.findViewById(R.id.txtIMDB);



        // Set the various views with their values

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMaximumFractionDigits(0);

        String tagline = mergedDoc.read(TAGLINE_PATH);

        String overview = mergedDoc.read(OVERVIEW_PATH);
        Integer runtime = mergedDoc.read(RUNTIME_PATH);
        Integer budget = mergedDoc.read(BUDGET_PATH);
        Integer revenue = mergedDoc.read(REVENUE_PATH);

        List<Map<String,Object>> production_companies = mergedDoc.read(PRODUCTION_COMPANIES_PATH);
        List<Map<String,Object>> genres = mergedDoc.read(GENRES_PATH);
        List<Map<String,Object>> videos = mergedDoc.read(VIDEOS_PATH);

        List<Map<String,Object>> cast = mergedDoc.read(CAST_PATH);
        List<Map<String,Object>> crew = mergedDoc.read(CREW_PATH);

        List<Map<String,Object>> exec_producers = mergedDoc.read(EXEC_PRODUCERS_PATH);
        List<Map<String,Object>> producers = mergedDoc.read(PRODUCERS_PATH);
        List<Map<String,Object>> castings = mergedDoc.read(CASTINGS_PATH);
        List<Map<String,Object>> directors = mergedDoc.read(DIRECTORS_PATH);
        List<Map<String,Object>> dops = mergedDoc.read(DOPS_PATH);
        List<Map<String,Object>> editors = mergedDoc.read(EDITORS_PATH);
        List<Map<String,Object>> composers = mergedDoc.read(COMPOSERS_PATH);
        List<Map<String,Object>> music_supers = mergedDoc.read(MUSIC_SUPERS_PATH);
        List<Map<String,Object>> writers = mergedDoc.read(WRITERS_PATH);
        List<Map<String,Object>> screenplay_writers = mergedDoc.read(SCREENPLAY_WRITERS_PATH);
        List<Map<String,Object>> story_writers = mergedDoc.read(STORY_WRITERS_PATH);

        String homepage = mergedDoc.read(HOMEPAGE_PATH);
        String fb_id = mergedDoc.read(FACEBOOK_ID_PATH);
        String insta_id = mergedDoc.read(INSTA_ID_PATH);
        String twitter_id = mergedDoc.read(TWITTER_ID_PATH);
        String imdb_id = mergedDoc.read(IMDB_ID_PATH);

        setTextIfNotNullAndNotEmpty(lblHomepage, txtHomepage, homepage);
        setTextIfNotNullAndNotEmpty(lblFB, txtFB, getFBURLFromID(fb_id));
        setTextIfNotNullAndNotEmpty(lblInsta, txtInsta, getInstaURLFromID(insta_id));
        setTextIfNotNullAndNotEmpty(lblTwitter, txtTwitter, getTwitterURLFromID(twitter_id));
        setTextIfNotNullAndNotEmpty(lblIMDB, txtIMDB, getIMDBURLFromID(imdb_id));

        txtTagline.setText(tagline);
        txtOverview.setText(overview);
        txtHomepage.setText(homepage);

        if ((runtime != null) && (runtime >0))
        {
            StringBuffer sb = new StringBuffer();

            int hours = runtime / 60;
            int minutes = runtime % 60;

            if (hours > 0)
            {
                sb.append(hours);
                sb.append(" ");
                sb.append("hr");
            }

            if (minutes > 0)
            {
                if (sb.length() > 0)
                {
                    sb.append(" ");
                }

                sb.append(minutes);
                sb.append(" ");
                sb.append("min");
            }

            sb.append(" (");
            sb.append(runtime);
            sb.append(" minutes)");

            txtRuntime.setText(sb.toString());
        }

        if ((budget != null) && (budget > 0))
        {
            String budgetStr = currencyFormatter.format(budget);

            txtBudget.setText(budgetStr);
        }

        if ((revenue != null) && (revenue > 0))
        {
            String revenueStr = currencyFormatter.format(revenue);

            txtRevenue.setText(revenueStr);
        }

        String cdlStr;

        cdlStr = getCDLStringFromListWithNames(production_companies);
        txtProductionCos.setText(cdlStr);

        cdlStr = getCDLStringFromListWithNames(directors);
        txtDirectors.setText(cdlStr);

        cdlStr = getCDLStringFromListWithNames(producers);
        txtProducers.setText(cdlStr);

        cdlStr = getCDLStringFromListWithNames(dops);
        txtDOPs.setText(cdlStr);

        cdlStr = getCDLStringFromListWithNames(screenplay_writers);
        txtWriters.setText(cdlStr);

        PersonListAdapter castListAdapter = new PersonListAdapter(PersonListAdapter.PERSON_TYPE.CAST, activity, R.id.rvCastCardList, R.id.lblAsCast, null);
        castListAdapter.clearAndAddList(cast);

        PersonListAdapter crewListAdapter = new PersonListAdapter(PersonListAdapter.PERSON_TYPE.CREW, activity, R.id.rvCrewCardList, R.id.lblAsCrew, null);

        Collections.sort(crew, new PersonUtils.CrewDeptAndJobComparator());
        List<Map<String,Object>> mergedCrew = PersonUtils.CrewListMerge(crew);
        crewListAdapter.clearAndAddList(mergedCrew);

        // process trailers looking for: Final Trailer, Official Trailer 5, Official Trailer 4, etc.
        // default to the first trailer found if none match the desired patterns

        Map<String, Object> trailer_to_play = null;
        for (Map<String,Object> video: videos)
        {

            String type = (String) video.get("type");
            if (type.equals("Trailer"))
            {
                if (trailer_to_play == null)
                {
                    trailer_to_play = video;
                }

                String name = (String) video.get("name");
                name = name.toLowerCase();
                if (name.contains("final trailer"))
                {
                    trailer_to_play = video;
                }
                else
                {
                    for (int i=1; i<=5; i++)
                    {
                        if (name.contains("official trailer " + Integer.toString(i)))
                        {
                            trailer_to_play = video;
                        }
                    }
                }
            }
        }



        if (trailer_to_play != null)
        {
            String site = (String) (trailer_to_play.get("site"));
            String key = (String)(trailer_to_play.get("key"));
            String name = (String)(trailer_to_play.get("name"));

            String youtubeLink = "http://youtube.com/watch?v=" + key;

            //int[] orderediTags = {22, 303, 248, 299, 137, 302, 247, 298, 136};

            // TODO: Convert to ITagOrderedYouTubeURLFabSetter.ITag enums
            int[] orderediTags = {
                    46,
                    37,
                    45,
                    22,
                    18,
                    43
            };

            ITagOrderedYouTubeURLFabSetter iTagOrderedYouTubeURLFabSetter = new ITagOrderedYouTubeURLFabSetter(activity, key, name, orderediTags ,fab);
            iTagOrderedYouTubeURLFabSetter.setFab();

        }
        else
        {
            fab.hide();
        }

        ProgressBar pgLoading = activity.findViewById(R.id.pgLoading);
        pgLoading.setVisibility(View.GONE);

    }

}
