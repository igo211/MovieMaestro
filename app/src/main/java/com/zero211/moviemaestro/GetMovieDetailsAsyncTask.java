package com.zero211.moviemaestro;

import android.app.Activity;
import android.content.Context;
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

import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMovieDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MOVIE_DETAILS_URL_PATT_STR = "movie/" + MOVIE_ID_PLACEHOLDER + "?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LANGUAGE_STR + "&append_to_response=images%2Cvideos%2Ccredits%2Crelease_dates%2Cexternal_ids";

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
    private static final String POSTERS_PATH = "$.images.posters";
    private static final String BACKDROPS_PATH = "$.images.backdrops";
    private static final String RELEASE_DATES_FOR_REGION_PATH = "$.release_dates.results[?(@.iso_3166_1 == '" + REGION_STR + "')].release_dates";
    private static final String RELEASE_DATES_PATH = "$.release_dates";

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

    public enum RELEASE_TYPE
    {
        PREMIERE(1, R.string.premiere),
        LIMITED_THEATRICAL(2, R.string.limited_theatrical),
        THEATRICAL(3, R.string.theatrical),
        DIGITAL(4, R.string.digital),
        PHYSICAL(5, R.string.physical),
        TV(6, R.string.physical)
        ;

        int dbVal;
        int displayStringID;

        RELEASE_TYPE(int dbVal, int displayStringID)
        {
            this.dbVal = dbVal;
            this.displayStringID = displayStringID;
        }

        public static RELEASE_TYPE fromDbVal(int dbVal)
        {
            for (RELEASE_TYPE releaseType : RELEASE_TYPE.values())
            {
                if (releaseType.dbVal == dbVal)
                {
                    return releaseType;
                }
            }

            return null;
        }

        public static String getReleaseTypeDisplayNameFromDbVal(Context context, int dbval)
        {
            RELEASE_TYPE releaseType = RELEASE_TYPE.fromDbVal(dbval);
            int resID = releaseType.getDisplayStringID();
            String displayString = context.getResources().getString(resID);
            return displayString;
        }

        public int getDisplayStringID()
        {
            return this.displayStringID;
        }

        public int getDbVal()
        {
            return this.dbVal;
        }
    }

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

        TextView lblTagline = fragmentView.findViewById(R.id.lblTagline);
        TextView txtTagline = fragmentView.findViewById(R.id.txtTagline);

        TextView lblOverview = fragmentView.findViewById(R.id.lblOverview);
        TextView txtOverview = fragmentView.findViewById(R.id.txtMovieOverview);

        TextView lblReleaseDates = fragmentView.findViewById(R.id.lblReleaseDate);
        TextView txtReleaseDates = fragmentView.findViewById(R.id.txtReleaseDate);

        TextView lblRuntime = fragmentView.findViewById(R.id.lblRuntime);
        TextView txtRuntime = fragmentView.findViewById(R.id.txtRuntime);

        TextView lblRevenueSlashBudget = fragmentView.findViewById(R.id.lblRevenueSlashBudget);
        TextView txtRevenueSlashBudget = fragmentView.findViewById(R.id.txtRevenueSlashBudget);

        TextView lblProductionCos = fragmentView.findViewById(R.id.lblProductionCos);
        TextView txtProductionCos = fragmentView.findViewById(R.id.txtProductionCos);

        // Set the various views with their values

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMaximumFractionDigits(0);

        String tagline = mergedDoc.read(TAGLINE_PATH);

        String overview = mergedDoc.read(OVERVIEW_PATH);

        Integer runtime = mergedDoc.read(RUNTIME_PATH);
        Number budgetNum = mergedDoc.read(BUDGET_PATH);
        Number revenueNum = mergedDoc.read(REVENUE_PATH);

        List<List<Map<String,Object>>> regionsReleaseDates = mergedDoc.read(RELEASE_DATES_FOR_REGION_PATH);
        List<Map<String,Object>> production_companies = mergedDoc.read(PRODUCTION_COMPANIES_PATH);
        List<Map<String,Object>> genres = mergedDoc.read(GENRES_PATH);
        List<Map<String,Object>> videos = mergedDoc.read(VIDEOS_PATH);

        List<Map<String,Object>> cast = mergedDoc.read(CAST_PATH);
        List<Map<String,Object>> crew = mergedDoc.read(CREW_PATH);
        List<Map<String,Object>> posters = mergedDoc.read(POSTERS_PATH);
        List<Map<String,Object>> backdrops = mergedDoc.read(BACKDROPS_PATH);

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

        UIUtils.setTextIfNotNullAndNotEmpty(lblTagline, txtTagline, tagline);
        UIUtils.setTextIfNotNullAndNotEmpty(lblOverview, txtOverview, overview);

        StringBuilder sb = new StringBuilder();

        for (List<Map<String,Object>> regions : regionsReleaseDates)
        {
            for(Map<String,Object> regionReleaseDate : regions)
            {
                Integer type = (Integer) (regionReleaseDate.get("type"));
                String typeDisplayName = RELEASE_TYPE.getReleaseTypeDisplayNameFromDbVal(this.movieDetailFragment.getContext(), type);
                String note = (String) (regionReleaseDate.get("note"));
                String releaseDateStr = (String) (regionReleaseDate.get("release_date"));
                String shortReleaseDateStr = DateFormatUtils.getShortDateStrFromTMDBDateStr(releaseDateStr);

                if (sb.length() > 0)
                {
                    sb.append("\n");
                }

                sb.append(typeDisplayName);
                sb.append(":\n    ");
                sb.append(shortReleaseDateStr);

                if (StringUtils.isNotNullOrEmpty(note))
                {
                    sb.append(" - ");
                    sb.append(note);
                }
            }

        }

        UIUtils.setTextIfNotNullAndNotEmpty(lblReleaseDates, txtReleaseDates, sb.toString());


        if ((runtime != null) && (runtime >0))
        {
            int hours = runtime / 60;
            int minutes = runtime % 60;

            String runtimeStr;

            if (hours > 0)
            {
                if (minutes > 0)
                {
                    runtimeStr = activity.getString(R.string.runtime_in_hours_minutes_total_minutes, hours, minutes, runtime);
                }
                else
                {
                    runtimeStr = activity.getString(R.string.runtime_in_hours_total_minutes, hours, runtime);
                }
            }
            else
            {
                runtimeStr = activity.getString(R.string.runtime_in_total_minutes, runtime);
            }

            UIUtils.setTextIfNotNullAndNotEmpty(lblRuntime, txtRuntime, runtimeStr);
        }

        long revenue = revenueNum.longValue();
        long budget = budgetNum.longValue();

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

        String revenueSlashBudgetStr = revenueStr + " / " + budgetStr;

        if ((revenue != 0) && (budget != 0))
        {

            long profitOrLoss = revenue - budget;
            long profitOrLossAbs = Math.abs(profitOrLoss);
            String profitOrLossAbsStr = currencyFormatter.format(profitOrLossAbs);


            revenueSlashBudgetStr = revenueSlashBudgetStr + "\n( ";

            switch (Long.signum(profitOrLoss))
            {
                case -1:
                    revenueSlashBudgetStr = revenueSlashBudgetStr + activity.getString(R.string.currently_an_x_loss, profitOrLossAbsStr);
                    break;
                case 1:
                    revenueSlashBudgetStr = revenueSlashBudgetStr  + activity.getString(R.string.currently_an_x_profit, profitOrLossAbsStr);
                    break;
                case 0:
                default:
                    revenueSlashBudgetStr = revenueSlashBudgetStr  +  activity.getString(R.string.currently_break_even);
            }

            revenueSlashBudgetStr = revenueSlashBudgetStr + " )";
        }

        UIUtils.setTextIfNotNullAndNotEmpty(lblRevenueSlashBudget, txtRevenueSlashBudget, revenueSlashBudgetStr);

        String cdlStr;

        cdlStr = UIUtils.getNDLStringFromListWithNames(production_companies);
        UIUtils.setTextIfNotNullAndNotEmpty(lblProductionCos, txtProductionCos, cdlStr);

        TMDBCardListAdapter castListAdapter = new TMDBCardListAdapter(activity, TMDBCardListAdapter.CARDTYPE.CAST, R.id.rvCastCardList, R.id.lblAsCast, null);
        castListAdapter.clearAndAddList(cast);

        TMDBCardListAdapter crewListAdapter = new TMDBCardListAdapter(activity, TMDBCardListAdapter.CARDTYPE.CREW,  R.id.rvCrewCardList, R.id.lblAsCrew, null);
        Collections.sort(crew, new PersonUtils.CrewDeptAndJobComparator());
        List<Map<String,Object>> mergedCrew = PersonUtils.CrewListMerge(crew);
        crewListAdapter.clearAndAddList(mergedCrew);

        TMDBCardListAdapter postersListAdapter = new TMDBCardListAdapter(activity, TMDBCardListAdapter.CARDTYPE.DATED_MOVIE_POSTER, R.id.rvPostersCardList, R.id.lblPosters, null);
        postersListAdapter.clearAndAddList(posters);

        TMDBCardListAdapter backdropsListAdapter = new TMDBCardListAdapter(activity, TMDBCardListAdapter.CARDTYPE.DATED_MOVIE_BACKDROP, R.id.rvBackdropsCardList, R.id.lblBackdrops, null);
        backdropsListAdapter.clearAndAddList(backdrops);

        TMDBCardListAdapter videosListAdapter = new TMDBCardListAdapter(activity, TMDBCardListAdapter.CARDTYPE.MOVIE_TRAILER_LAUNCHER, R.id.rvVideosCardList, R.id.lblVideos, null);
        videosListAdapter.clearAndAddList(videos);

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

            // TODO: Convert to ITagOrderedYouTubeURLImageAndButtonSetter.ITag enums
            int[] orderediTags = {
                    46,
                    37,
                    45,
                    22,
                    18,
                    43
            };

            ITagOrderedYouTubeURLImageAndButtonSetter iTagOrderedYouTubeURLImageAndButtonSetter = new ITagOrderedYouTubeURLImageAndButtonSetter(activity, key, name, orderediTags ,fab);
            iTagOrderedYouTubeURLImageAndButtonSetter.setPlayButtonAndStillImage();
        }
        else
        {
            fab.hide();
        }

        UIUtils.setSocialButtons(mergedDoc, fragmentView);

        ProgressBar pgLoading = activity.findViewById(R.id.pgLoading);
        pgLoading.setVisibility(View.GONE);

    }

}
