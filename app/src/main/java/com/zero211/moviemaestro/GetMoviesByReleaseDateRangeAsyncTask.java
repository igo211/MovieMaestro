package com.zero211.moviemaestro;

import com.jayway.jsonpath.DocumentContext;

import java.util.List;
import java.util.Map;

import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMoviesByReleaseDateRangeAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MOVIES_URL_PATT_STR = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY + "&language=" + LOCALE_STR + "&region=" + REGION_STR + "&sort_by=popularity.desc&include_adult=false&include_video=false&release_date.gte=" + START_DATE_PLACEHOLDER + "&release_date.lte=" + END_DATE_PLACEHOLDER + "&with_release_type=2|3&page=" + PAGE_PLACEHOLDER;

    private MovieListAdapter movieListAdapter;
    private String startPageParam;
    private String endPageParam;
    private String startDateStr;
    private String endDateStr;

    public GetMoviesByReleaseDateRangeAsyncTask(MovieListAdapter movieListAdapter, String startDateStr, String endDateStr)
    {
        this.movieListAdapter = movieListAdapter;
        this.startDateStr = startDateStr;
        this.endDateStr = endDateStr;
    }

    @Override
    protected DocumentContext doInBackground(String... params)
    {
        // start and end page default to 0 and Integer.MAX_VALUE respectively
        startPageParam = this.getStartPageFromParams(0, params);
        endPageParam = this.getEndPageFromParams(1, params);

        String urlStr = MOVIES_URL_PATT_STR
                .replace(START_DATE_PLACEHOLDER, startDateStr)
                .replace(END_DATE_PLACEHOLDER, endDateStr);



        DocumentContext mergedDoc = super.doInBackground(urlStr, startPageParam, endPageParam);

        return mergedDoc;
    }

    @Override
    protected void onPostExecute(DocumentContext mergedDoc)
    {
        String internal_err_msg = mergedDoc.read(INTERNAL_ERROR_PATH);
        List<String> TMDB_err_msgs = mergedDoc.read(ERRORS_PATH);
        Integer TMDB_status_code = mergedDoc.read(STATUS_CODE_PATH);
        String TMDB_status_msg = mergedDoc.read(STATUS_MESSAGE_PATH);
        Integer TMDB_total_results = mergedDoc.read(TOTAL_RESULTS_PATH);
        Integer TMDB_total_pages = mergedDoc.read(TOTAL_PAGES_PATH);

        // TODO: Handle the various error cases... push error handling code up to parent class?

        List<Map<String,Object>> moviesList = mergedDoc.read(RESULTS_PATH);

        if ((moviesList != null) && (moviesList.size() > 0))
        {
            movieListAdapter.setTotal_pages(TMDB_total_pages);
            movieListAdapter.setTotal_results(TMDB_total_results);

            if (startPageParam.trim().equals("1"))
            {
                movieListAdapter.clearAndAddMovies(moviesList);
            }
            else
            {
                movieListAdapter.addMovies(moviesList);
            }
        }

    }
}
