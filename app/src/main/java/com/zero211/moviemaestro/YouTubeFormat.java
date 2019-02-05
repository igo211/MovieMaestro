package com.zero211.moviemaestro;

// TODO: flesh out names and member values
public enum YouTubeFormat
{
    FOO(17, "3GP", 144, "MPEG-4 Visual", "Simple", .05f, "AAC", 24)
    ;

    // TODO: Move all members except the bit rates into other new enums (containers, encodings, profiles, resolutions)
    private int iTag;
    private String container;
    private int videoResolution;
    private String videoEncoding;
    private String videoProfile;
    private float videoMbitsPerSec;
    private String audioEncoding;
    private float audioKbitsPerSec;

    YouTubeFormat(int iTag, String container, int videoResolution, String videoEncoding, String videoProfile, float videoMbitsPerSec, String audioEncoding, float audioKbitsPerSec)
    {
        this.iTag = iTag;
        this.container = container;
        this.videoResolution = videoResolution;
        this.videoEncoding = videoEncoding;
        this.videoProfile = videoProfile;
        this.videoMbitsPerSec = videoMbitsPerSec;
        this.audioEncoding = audioEncoding;
        this.audioKbitsPerSec = audioKbitsPerSec;
    }

    public int getiTag()
    {
        return iTag;
    }

    public String getContainer()
    {
        return container;
    }

    public int getVideoResolution()
    {
        return videoResolution;
    }

    public String getVideoEncoding()
    {
        return videoEncoding;
    }

    public String getVideoProfile()
    {
        return videoProfile;
    }

    public float getVideoMbitsPerSec()
    {
        return videoMbitsPerSec;
    }

    public String getAudioEncoding()
    {
        return audioEncoding;
    }

    public float getAudioKbitsPerSec()
    {
        return audioKbitsPerSec;
    }
}
