package com.zero211.tmdbtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jayway.jsonpath.DocumentContext;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMovieDetailsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MOVIE_DETAILS_URL_PATT_STR = "https://api.themoviedb.org/3/movie/" + MOVIE_ID_PLACEHOLDER + "?api_key=" + API_KEY + "&language=" + LOCALE_STR + "&append_to_response=videos%2Ccredits%2Cexternal_ids";

    private static final String OVERVIEW_PATH = "$.overview";
    private static final String RELEASE_DATE_PATH = "$.release_date";
    private static final String BUDGET_PATH = "$.budget";
    private static final String REVENUE_PATH = "$.revenue";
    private static final String RUNTIME_PATH = "$.runtime";
    private static final String HOMEPAGE_PATH = "$.homepage";
    private static final String IMDB_ID_PATH = "$.imdb_id";
    private static final String TAGLINE_PATH = "$.tagline";
    private static final String CAST_PATH = "$.credits.cast";
    private static final String CREW_PATH = "$.credits.crew";
    private static final String GENRES_PATH = "$.genres";
    private static final String VIDEOS_PATH = "$.videos.results";

    private MovieDetailFragment movieDetailFragment;
    private String urlStr;

    public GetMovieDetailsAsyncTask(MovieDetailFragment movieDetailFragment)
    {
        this.movieDetailFragment = movieDetailFragment;
        int movieID = movieDetailFragment.getMovieID();
        String movieIDStr = String.valueOf(movieID);
        urlStr = MOVIE_DETAILS_URL_PATT_STR.replace(MOVIE_ID_PLACEHOLDER, movieIDStr);

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
        // Process the results and update the UI

        Activity activity = movieDetailFragment.getActivity();

        FloatingActionButton fab = activity.findViewById(R.id.run_trailer_fab);

        View fragmentView = movieDetailFragment.getView();

        TextView txtTagline = fragmentView.findViewById(R.id.txtTagline);
        TextView txtOverview = fragmentView.findViewById(R.id.txtMovieOverview);
        TextView txtReleaseDate = fragmentView.findViewById(R.id.txtReleaseDate);
        TextView txtHomepage = fragmentView.findViewById(R.id.txtHomepage);
        TextView txtRuntime = fragmentView.findViewById(R.id.txtRuntime);
        TextView txtBudget = fragmentView.findViewById(R.id.txtBudget);
        TextView txtRevenue = fragmentView.findViewById(R.id.txtRevenue);

        String internal_err_msg = mergedDoc.read(INTERNAL_ERROR_PATH);
        List<String> TMDB_err_msgs = mergedDoc.read(ERRORS_PATH);
        Integer TMDB_status_code = mergedDoc.read(STATUS_CODE_PATH);
        String TMDB_status_msg = mergedDoc.read(STATUS_MESSAGE_PATH);

        // TODO: Handle the various error cases

        // Set the various views with their values

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        currencyFormatter.setCurrency(Currency.getInstance("USD"));
        currencyFormatter.setMaximumFractionDigits(0);

        String tagline = mergedDoc.read(TAGLINE_PATH);
        String homepage = mergedDoc.read(HOMEPAGE_PATH);
        String overview = mergedDoc.read(OVERVIEW_PATH);
        String release_date = mergedDoc.read(RELEASE_DATE_PATH);
        Integer runtime = mergedDoc.read(RUNTIME_PATH);
        Integer budget = mergedDoc.read(BUDGET_PATH);
        Integer revenue = mergedDoc.read(REVENUE_PATH);
        String imdb_id = mergedDoc.read(IMDB_ID_PATH);
        List<Map<String,Object>> genres = mergedDoc.read(GENRES_PATH);
        List<Map<String,Object>> cast = mergedDoc.read(CAST_PATH);
        List<Map<String,Object>> crew = mergedDoc.read(CREW_PATH);
        List<Map<String,Object>> videos = mergedDoc.read(VIDEOS_PATH);

        txtTagline.setText(tagline);
        txtOverview.setText(overview);
        txtReleaseDate.setText(release_date);
        txtHomepage.setText(homepage);

        if ((runtime != null) && (runtime >0))
        {
            String runtimeStr = runtime.toString() + " minutes";

            txtRuntime.setText(runtimeStr);
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



    }
}
