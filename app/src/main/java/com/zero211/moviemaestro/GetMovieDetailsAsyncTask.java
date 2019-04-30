package com.zero211.moviemaestro;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
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

import static com.zero211.moviemaestro.StringUtils.getFBURIFromID;
import static com.zero211.moviemaestro.StringUtils.getIMDBURIFromID;
import static com.zero211.moviemaestro.StringUtils.getInstaURIFromID;
import static com.zero211.moviemaestro.StringUtils.getTwitterURIFromID;
import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMovieDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MOVIE_DETAILS_URL_PATT_STR = "movie/" + MOVIE_ID_PLACEHOLDER + "?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&append_to_response=images%2Cvideos%2Ccredits%2Cexternal_ids";

    private static final String OVERVIEW_PATH = "$.overview";
    private static final String BUDGET_PATH = "$.budget";
    private static final String REVENUE_PATH = "$.revenue";
    private static final String RUNTIME_PATH = "$.runtime";
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
        TextView txtRevenueSlashBudget = fragmentView.findViewById(R.id.txtRevenueSlashBudget);

        TextView txtProductionCos = fragmentView.findViewById(R.id.txtProductionCos);

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

//        List<Map<String,Object>> exec_producers = mergedDoc.read(EXEC_PRODUCERS_PATH);
//        List<Map<String,Object>> producers = mergedDoc.read(PRODUCERS_PATH);
//        List<Map<String,Object>> castings = mergedDoc.read(CASTINGS_PATH);
//        List<Map<String,Object>> directors = mergedDoc.read(DIRECTORS_PATH);
//        List<Map<String,Object>> dops = mergedDoc.read(DOPS_PATH);
//        List<Map<String,Object>> editors = mergedDoc.read(EDITORS_PATH);
//        List<Map<String,Object>> composers = mergedDoc.read(COMPOSERS_PATH);
//        List<Map<String,Object>> music_supers = mergedDoc.read(MUSIC_SUPERS_PATH);
//        List<Map<String,Object>> writers = mergedDoc.read(WRITERS_PATH);
//        List<Map<String,Object>> screenplay_writers = mergedDoc.read(SCREENPLAY_WRITERS_PATH);
//        List<Map<String,Object>> story_writers = mergedDoc.read(STORY_WRITERS_PATH);


        txtTagline.setText(tagline);
        txtOverview.setText(overview);

        if ((runtime != null) && (runtime >0))
        {
            int hours = runtime / 60;
            int minutes = runtime % 60;

            String runtimeStr;
            if (hours > 0)
            {
                runtimeStr = activity.getString(R.string.runtime_in_hours_minutes_total_minutes, hours, minutes, runtime);
            }
            else
            {
                runtimeStr = activity.getString(R.string.runtime_in_total_minutes, runtime);
            }

            txtRuntime.setText(runtimeStr);
        }

        String revenueStr = activity.getString(R.string.unknown);
        if (revenue != 0)
        {
            revenueStr = currencyFormatter.format(revenue);
        }

        String budgetStr = activity.getString(R.string.unknown);
        if (budget != 0)
        {
            budgetStr = currencyFormatter.format(budget);
        }

        String revenueSlashBudgetStr = revenueStr + "/" + budgetStr;

        if ((revenue != 0) && (budget != 0))
        {

            int profitOrLoss = revenue - budget;
            int profitOrLossAbs = Math.abs(profitOrLoss);
            String profitOrLossAbsStr = currencyFormatter.format(profitOrLossAbs);


            revenueSlashBudgetStr = revenueSlashBudgetStr + " ( ";

            switch (Integer.signum(profitOrLoss))
            {
                case -1:
                    revenueSlashBudgetStr = revenueSlashBudgetStr + activity.getString(R.string.currently_an_x_loss, profitOrLossAbsStr);
                    break;
                case 1:
                    revenueSlashBudgetStr = revenueSlashBudgetStr + activity.getString(R.string.currently_an_x_profit, profitOrLossAbsStr);
                    break;
                case 0:
                default:
                    revenueSlashBudgetStr = revenueSlashBudgetStr + activity.getString(R.string.currently_break_even);
            }

            revenueSlashBudgetStr = revenueSlashBudgetStr + " )";
        }

        txtRevenueSlashBudget.setText(revenueSlashBudgetStr);

        String cdlStr;

        cdlStr = getCDLStringFromListWithNames(production_companies);
        txtProductionCos.setText(cdlStr);

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

        setSocialButtons(mergedDoc, fragmentView);

        ProgressBar pgLoading = activity.findViewById(R.id.pgLoading);
        pgLoading.setVisibility(View.GONE);

    }

}
