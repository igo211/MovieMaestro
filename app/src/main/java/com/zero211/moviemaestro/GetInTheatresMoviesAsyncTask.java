package com.zero211.moviemaestro;

import android.content.Context;

import com.jayway.jsonpath.DocumentContext;
import com.zero211.utils.http.HttpStringResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetInTheatresMoviesAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MOVIES_URL_PATT_STR = "movie/now_playing?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&region=" + REGION_STR  + "&page=" + PAGE_PLACEHOLDER;

    private TMDBCardListAdapter movieListAdapter;
    private int startPage;

    public GetInTheatresMoviesAsyncTask(Context context, int startPage, int endPage, TMDBCardListAdapter movieListAdapter)
    {
        super(context, startPage, endPage, MOVIES_URL_PATT_STR);
        this.movieListAdapter = movieListAdapter;
        this.startPage = startPage;
    }

    @Override
    protected void onPostExecute(HttpStringResponse mergedResponse)
    {
        DocumentContext mergedDoc = mergedResponse.getDocumentContext();

        String internal_err_msg = mergedDoc.read(INTERNAL_ERROR_PATH);
        List<String> TMDB_err_msgs = mergedDoc.read(ERRORS_PATH);
        Integer TMDB_status_code = mergedDoc.read(STATUS_CODE_PATH);
        String TMDB_status_msg = mergedDoc.read(STATUS_MESSAGE_PATH);
        Integer TMDB_total_results = mergedDoc.read(TOTAL_RESULTS_PATH);
        Integer TMDB_total_pages = mergedDoc.read(TOTAL_PAGES_PATH);

        // TODO: Handle the various error cases... push error handling code up to parent class?

        List<Map<String,Object>> moviesList = mergedDoc.read(RESULTS_PATH);

        Collections.sort(moviesList, new MovieUtils.MovieReleaseDateComparator(true));

        if (startPage == 1)
        {
            movieListAdapter.clearAndAddList(moviesList);
        }
        else
        {
            movieListAdapter.addList(moviesList);
        }
    }
}
