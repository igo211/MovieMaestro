package com.zero211.moviemaestro;

import com.jayway.jsonpath.DocumentContext;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMoviesByReleaseDateRangeAsyncTask extends AbstractTMDBJSONResultFromURLTask
{

    private static final String UPCOMING_MOVIES_URL_PATT_STR = "https://api.themoviedb.org/3/discover/movie?api_key=" + API_KEY + "&language=" + LOCALE_STR + "&region=" + REGION_STR + "&sort_by=popularity.desc&include_adult=false&include_video=false&primary_release_date.gte=" + START_DATE_PLACEHOLDER + "&release_date.lte=" + END_DATE_PLACEHOLDER + "&page=" + PAGE_PLACEHOLDER;

    private MoviesAdapter moviesAdapter;
    private String startPageParam;
    private String endPageParam;

    public GetMoviesByReleaseDateRangeAsyncTask(MoviesAdapter moviesAdapter)
    {
        this.moviesAdapter = moviesAdapter;
    }

    @Override
    protected DocumentContext doInBackground(String... params)
    {
        // start and end page default to 0 and Integer.MAX_VALUE respectively
        startPageParam = this.getStartDateFromParams(0, params);
        endPageParam = this.getEndDateFromParams(1, params);

        Calendar cal = Calendar.getInstance();

        // start with tomorrow's date
        cal.add(Calendar.DAY_OF_MONTH, 1);
        String startDateStr = DATE_FORMAT.format(cal.getTime());

        // end with a date six months from tomorrow
        cal.add(Calendar.MONTH, 6);
        String endDateStr = DATE_FORMAT.format(cal.getTime());

        String urlStr = UPCOMING_MOVIES_URL_PATT_STR
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

        // TODO: Handle the various error cases

        List<Map<String,Object>> moviesList = mergedDoc.read(RESULTS_PATH);

        if ((moviesList != null) && (moviesList.size() > 0))
        {
            moviesAdapter.setTotal_pages(TMDB_total_pages);
            moviesAdapter.setTotal_results(TMDB_total_results);

            if (startPageParam.trim().equals("1"))
            {
                moviesAdapter.clearAndAddMovies(moviesList);
            }
            else
            {
                moviesAdapter.addMovies(moviesList);
            }
        }

    }
}
