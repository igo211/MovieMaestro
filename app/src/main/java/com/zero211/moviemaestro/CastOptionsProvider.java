package com.zero211.moviemaestro;

import android.content.Context;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

public class CastOptionsProvider implements OptionsProvider
{
    private static final String CAST_APP_ID = "8D5317FA"; // The real assigned app id

    // App ids from the sample apps
    //private static final String CAST_APP_ID = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
    //private static final String CAST_APP_ID = "CC1AD845";
    //private static final String CAST_APP_ID = "4F8B3483";

    @Override
    public CastOptions getCastOptions(Context context)
    {
        return new CastOptions.Builder()
                .setReceiverApplicationId(CAST_APP_ID)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context)
    {
        return null;
    }
}
