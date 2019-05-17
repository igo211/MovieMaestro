package com.zero211.moviemaestro;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class ITagOrderedYouTubeURLImageAndButtonSetter extends YouTubeExtractor
{
    private Context context;
    private String videoID;
    private String title;
    private int[] orderediTags;    // TODO: Convert orderediTags to an array of YouTubeFormat enums
    private View playButtonView;
    private SimpleDraweeView stillImageView;

    public ITagOrderedYouTubeURLImageAndButtonSetter(Context context, String videoID, String title, int[] orderediTags, View playButtonView)
    {
        this(context, videoID, title, orderediTags, playButtonView, null);
    }

    public ITagOrderedYouTubeURLImageAndButtonSetter(Context context, String videoID, String title, int[] orderediTags, View playButtonView, SimpleDraweeView stillImageView)
    {
        super(context);
        this.context = context;
        this.videoID = videoID;
        this.title = title;
        this.orderediTags = orderediTags;
        this.playButtonView = playButtonView;
        this.stillImageView = stillImageView;
    }

    public void setPlayButtonAndStillImage()
    {
        String indirectURL = "http://youtube.com/watch?v=" + this.videoID;
        super.extract(indirectURL, true, true);
    }

    @Override
    public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta)
    {
        if (ytFiles != null)
        {
            YtFile ytFile = null;
            int itag = -1;
            for (int cItag : orderediTags)
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
                if (this.stillImageView != null)
                {
                    String stillImageURL = vMeta.getHqImageUrl();
                    if (stillImageURL == null)
                    {
                        stillImageURL = vMeta.getMqImageUrl();
                    }
                    if (stillImageURL == null)
                    {
                        stillImageURL = vMeta.getSdImageUrl();
                    }

                    if (stillImageURL != null)
                    {
                        this.stillImageView.setImageURI(stillImageURL);
                    }
                }

                String downloadUrl = ytFile.getUrl();
                final int iTag = itag;


                playButtonView.setOnClickListener(new View.OnClickListener()
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
                if (playButtonView instanceof FloatingActionButton)
                {
                    FloatingActionButton fab = (FloatingActionButton)(playButtonView);
                    fab.show();
                }
                else
                {
                    playButtonView.setVisibility(View.VISIBLE);
                }
            }
            else
            {
                if (playButtonView instanceof FloatingActionButton)
                {
                    FloatingActionButton fab = (FloatingActionButton) (playButtonView);
                    fab.hide();
                }
                else
                {
                    playButtonView.setVisibility(View.INVISIBLE);
                }
            }

        }
    }
}
