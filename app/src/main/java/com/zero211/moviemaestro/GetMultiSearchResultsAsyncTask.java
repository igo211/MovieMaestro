package com.zero211.moviemaestro;

import android.content.Context;
import android.widget.Toast;

import com.jayway.jsonpath.DocumentContext;
import com.zero211.utils.http.HttpStringResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.zero211.utils.http.HttpUtils.INTERNAL_ERROR_PATH;

public class GetMultiSearchResultsAsyncTask extends AbstractTMDBJSONResultFromURLTask
{
    private static final String MULT_SEARCH_URL_PATT_STR = "search/multi?api_key=" + API_KEY_PLACEHOLDER + "&language=" + LOCALE_STR + "&region=" + REGION_STR + "&sort_by=popularity.desc&include_adult=false&include_video=false&query=" + QUERY_PLACEHOLDER + "&page=" + PAGE_PLACEHOLDER;

    private MovieListAdapter movieListAdapter;
    private PersonListAdapter personListAdapter;

    private Context context;

    private int startPage;

    public GetMultiSearchResultsAsyncTask(Context context, int startPage, int endPage, String query, MovieListAdapter movieListAdapter, PersonListAdapter personListAdapter)
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
            String media_type = (String) result.get("media_type");
            switch (media_type)
            {
                case "movie":
                    movieList.add(result);
                    break;
                case "person":
                    personList.add(result);
                    break;
                case "tv":
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
            Toast.makeText(context, "Unhandled media_types in search results: '" + sb.toString() + "'", Toast.LENGTH_LONG).show();
        }

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
