package com.zero211.moviemaestro;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

public class PersonUtils
{
    public static final String ID_KEY = "id";
    public static final String DEPARTMENT_KEY = "department";
    public static final String JOB_KEY = "job";

    public static List<Map<String, Object>> CrewListMerge(List<Map<String, Object>> origList)
    {
        HashMap<Integer, Map<String, Object>> processedItemsByID = new HashMap<>();

        for (Map<String,Object> origItem : origList)
        {
            int id = (Integer)(origItem.get(ID_KEY));

            Map<String, Object> processedItem = processedItemsByID.get(id);
            if (processedItem == null)
            {
                processedItemsByID.put(id, origItem);
            }
            else
            {
                String origItemJob = (String)(origItem.get(JOB_KEY));
                if (!StringUtils.isNullOrEmpty(origItemJob))
                {
                    String newProcessedItemJob;
                    String processedItemJob = (String)(processedItem.get(JOB_KEY));
                    if (!StringUtils.isNullOrEmpty(processedItemJob))
                    {
                        newProcessedItemJob = processedItemJob + ", " + origItemJob;
                    }
                    else
                    {
                        newProcessedItemJob = origItemJob;
                    }

                    processedItem.put(JOB_KEY, newProcessedItemJob);
                    processedItemsByID.put(id, processedItem);
                }
            }
        }

        HashSet<Integer> processedToOrderedResults = new HashSet<>();
        ArrayList<Map<String, Object>> results = new ArrayList<>();

        for (Map<String,Object> origItem : origList)
        {
            int id = (Integer)(origItem.get(ID_KEY));
            Boolean wasProcessedAlready = processedToOrderedResults.contains(id);

            if (!wasProcessedAlready)
            {
                Map<String,Object> processedItem = processedItemsByID.get(id);
                results.add(processedItem);
                processedToOrderedResults.add(id);
            }
        }

        return results;
    }

    // Note, these are in reverse order.
    public static class CrewDeptAndJobComparator implements Comparator<Map<String,Object>>
    {
        private static final String DEFAULT_DEPT = "Crew";

        private static final String[] ORDERED_DEPTS = {
                DEFAULT_DEPT,
                "Actors",
                "Visual Effects",
                "Editing",
                "Costume & Make-Up",
                "Sound",
                "Art",
                "Lighting",
                "Camera",
                "Production",
                "Writing",
                "Directing"
        };

        private static final ArrayList<String> ORDERED_DEPTS_LIST = new ArrayList<>(Arrays.asList(ORDERED_DEPTS));


        // Note, these specific jobs come before all others, after that, we fall back to department order
        // Also note, these are in reverse order.
        private static final String[] ORDERED_JOBS = {
                "Casting Director",
                "Original Music Composer",
                "Music Composer",
                "Costume Designer",
                "Associate Producer",
                "Editor",
                "Production Designer",
                "Director of Photography",
                "Executive Producer",
                "Co-Producer",
                "Producer",
                "Author",
                "Original Story",
                "Screenplay",
                "Director"
        };

        private static final ArrayList<String> ORDERED_JOBS_LIST = new ArrayList<>(Arrays.asList(ORDERED_JOBS));

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2)
        {
            String dept1Str = (String)(o1.get(DEPARTMENT_KEY));
            int dept1Val = ORDERED_DEPTS_LIST.indexOf(dept1Str);
            if (dept1Val == -1)
            {
                dept1Val = ORDERED_DEPTS_LIST.indexOf(DEFAULT_DEPT);
            }

            String job1Str = (String)(o1.get(JOB_KEY));
            int job1Val = ORDERED_JOBS_LIST.indexOf(job1Str);

            String dept2Str = (String)(o2.get(DEPARTMENT_KEY));
            int dept2Val = ORDERED_DEPTS_LIST.indexOf(dept2Str);
            if (dept2Val == -1)
            {
                dept2Val = ORDERED_DEPTS_LIST.indexOf(DEFAULT_DEPT);
            }

            String job2Str = (String)(o2.get(JOB_KEY));
            int job2Val = ORDERED_JOBS_LIST.indexOf(job2Str);

            // TODO: Figure out relative sort order of depts + jobs...

            // If a hit on the job, we go with that, if not, go with department.


            if ((job1Val == -1) && (job2Val == -1))
            {
                return Integer.signum(dept2Val - dept1Val);
            }
            else
            {
                return Integer.signum(job2Val - job1Val);
            }

        }
    }
}
