package com.zero211.moviemaestro;

import android.graphics.drawable.Animatable;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import androidx.exifinterface.media.ExifInterface;

public class ExifDateExtractorControllerListener extends BaseControllerListener<ImageInfo>
{
    private TextView txtDate;
    private ImageRequest imageRequest;

    public ExifDateExtractorControllerListener(ImageRequest imageRequest, TextView txtDate)
    {
        this.imageRequest = imageRequest;
        this.txtDate = txtDate;
    }

    @Override
    public void onFinalImageSet (String id, ImageInfo imageInfo, Animatable animatable)
    {
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(this.imageRequest, null);
        BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);

        File file = ((FileBinaryResource)resource).getFile();
        try
        {
            ExifInterface exifInterface = new ExifInterface(file);
            String dateTimeOrigStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            String dateTimeStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String dateTimeDigitizedStr = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
            String gpsDateTimeStampStr = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);

            String bestDateTimeStr;
            if (!StringUtils.isNullOrEmpty(dateTimeOrigStr))
            {
                bestDateTimeStr = dateTimeOrigStr;
            }
            else if (!StringUtils.isNullOrEmpty(dateTimeDigitizedStr))
            {
                bestDateTimeStr = dateTimeDigitizedStr;
            }
            else if (!StringUtils.isNullOrEmpty(gpsDateTimeStampStr))
            {
                bestDateTimeStr = gpsDateTimeStampStr;
            }
            else
            {
                bestDateTimeStr = dateTimeStr;
            }

            if (!StringUtils.isNullOrEmpty(bestDateTimeStr))
            {
                Date date = DateFormatUtils.getDateFromExifDateTimeOriginal(bestDateTimeStr);
                if (date != null)
                {
                    String formattedDateStr = DateFormatUtils.getJustYearDateStrFromTMDBDate(date);
                    UIUtils.setTextIfNotNullAndNotEmpty(txtDate, formattedDateStr);
                }
            }


        }
        catch (IOException ioe)
        {
            // TODO: What todo?
        }

    }
}
