package com.zero211.moviemaestro;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.zero211.moviemaestro.DateFormatUtils.*;

public class MovieUtils
{
    public static class MovieReleaseDateComparator implements Comparator<Map<String,Object>>
    {
        private static final String RELEASE_DATE_KEY = "release_date";

        private boolean newerFirst;

        public MovieReleaseDateComparator(boolean newerFirst)
        {
            this.newerFirst = newerFirst;
        }

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2)
        {
            String releaseDate1Str = (String)(o1.get(RELEASE_DATE_KEY));
            Date releaseDate1 = getDateFromTMDBDateStr(releaseDate1Str);
            long releaseDate1EpochMillis = Long.MAX_VALUE;
            if (releaseDate1 != null)
            {
                releaseDate1EpochMillis = releaseDate1.getTime();
            }

            String releaseDate2Str = (String)(o2.get(RELEASE_DATE_KEY));
            Date releaseDate2 = getDateFromTMDBDateStr(releaseDate2Str);
            long releaseDate2EpochMillis = Long.MAX_VALUE;
            if (releaseDate2 != null)
            {
                releaseDate2EpochMillis = releaseDate2.getTime();
            }

            long dateDiff;

            if (newerFirst)
            {
                dateDiff = releaseDate2EpochMillis - releaseDate1EpochMillis;
            }
            else
            {
                dateDiff = releaseDate1EpochMillis - releaseDate2EpochMillis;
            }


            return Long.signum(dateDiff);
        }
    }
}
