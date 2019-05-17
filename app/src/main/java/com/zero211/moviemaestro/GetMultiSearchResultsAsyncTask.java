package com.zero211.moviemaestro;

import android.content.Context;
import android.widget.Toast;

import com.jayway.jsonpath.DocumentContext;
import com.zero211.utils.http.HttpStringResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMultiSearchResultsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MULT_SEARCH_URL_PATT_STR = "search/multi?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&region=" + REGION_STR + "&sort_by=popularity.desc&include_adult=false&include_video=false&query=" + QUERY_PLACEHOLDER + "&page=" + PAGE_PLACEHOLDER;
    public static final String MEDIA_TYPE_KEY = "media_type";
    public static final String MOVIE = "movie";
    public static final String PERSON = "person";
    public static final String TV = "tv";

    private TMDBCardListAdapter movieListAdapter;
    private TMDBCardListAdapter personListAdapter;

    private Context context;

    private int startPage;

    public GetMultiSearchResultsAsyncTask(Context context, int startPage, int endPage, String query, TMDBCardListAdapter movieListAdapter, TMDBCardListAdapter personListAdapter)
    {
        super(context, startPage, endPage, MULT_SEARCH_URL_PATT_STR.replace(QUERY_PLACEHOLDER, query));
        this.context = context;
        this.startPage = startPage;
        this.movieListAdapter = movieListAdapter;
        this.personListAdapter = personListAdapter;
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

        List<Map<String,Object>> resultList = mergedDoc.read(RESULTS_PATH);

        List<Map<String,Object>> movieList = new ArrayList<Map<String, Object>>();
        List<Map<String,Object>> personList = new ArrayList<Map<String, Object>>();

        StringBuffer sb = new StringBuffer();

        for (Map<String,Object> result : resultList)
        {
            String media_type = (String) result.get(MEDIA_TYPE_KEY);
            switch (media_type)
            {
                case MOVIE:
                    movieList.add(result);
                    break;
                case PERSON:
                    personList.add(result);
                    break;
                case TV:
                    // do nothing for now... TV search results are not yet supported
                    break;
                default:
                    if (sb.length() > 0)
                    {
                        sb.append(", ");
                    }
                    sb.append(media_type);
            }
        }

        if (sb.length() > 0)
        {
            Toast.makeText(context, context.getResources().getString(R.string.unhandled_media_type_in_search_results) + sb.toString() + "'", Toast.LENGTH_LONG).show();
        }

        Collections.sort(movieList, new MovieUtils.MovieReleaseDateComparator(true));

        if (startPage == 1)
        {
            movieListAdapter.clearAndAddList(movieList);
        }
        else
        {
            movieListAdapter.addList(movieList);
        }

        if (startPage == 1)
        {
            personListAdapter.clearAndAddList(personList);
        }
        else
        {
            personListAdapter.addList(personList);
        }

    }
}
