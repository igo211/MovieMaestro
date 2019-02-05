package com.zero211.tmdbtest;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class ITagOrderedYouTubeURLFabSetter extends YouTubeExtractor
{
    private Context context;
    private String videoID;
    private String title;
    private int[] orderediTags;    // TODO: Convert orderediTags to an array of YouTubeFormat enums
    private FloatingActionButton fab;


    public ITagOrderedYouTubeURLFabSetter(Context context, String videoID, String title, int[] orderediTags, FloatingActionButton fab)
    {
        super(context);
        this.context = context;
        this.videoID = videoID;
        this.title = title;
        this.orderediTags = orderediTags;
        this.fab = fab;
    }

    public void setFab()
    {
        String indirectURL = "http://youtube.com/watch?v=" + this.videoID;
        super.extract(indirectURL, true, true);
    }

    @Override
    public void onExtractionComplete(SparseArray< YtFile > ytFiles, VideoMeta vMeta) {
        if (ytFiles != null) {
            YtFile ytFile = null;
            int itag = -1;
            for (int cItag: orderediTags)
            {
                YtFile cYtFile = ytFiles.get(cItag);
                if (cYtFile != null)
                {
                    itag = cItag;
                    ytFile = cYtFile;
                    break;
                }
            }

            if (ytFile != null)
            {
                String downloadUrl = ytFile.getUrl();
                final int iTag = itag;

                fab.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        //Toast.makeText(activity, "key = '" + key + "'", Toast.LENGTH_LONG).show();
                        //Toast.makeText(activity, "iTag = '" + iTag + "'", Toast.LENGTH_LONG).show();
                        //Toast.makeText(activity, "url = '" + downloadUrl + "'", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(context, MoviePlayerActivity.class);
                        intent.putExtra(MoviePlayerActivity.ARG_VIDEO_URL, downloadUrl);
                        intent.putExtra(MoviePlayerActivity.ARG_MOVIE_TITLE, title);
                        context.startActivity(intent);
                    }
                });
                fab.show();
            }
            else
            {
                fab.hide();
            }

        }
    }
}
