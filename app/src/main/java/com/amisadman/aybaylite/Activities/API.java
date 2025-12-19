package com.amisadman.aybaylite.Activities;

import com.amisadman.aybaylite.BuildConfig;

public final class API {
    public static final String MODEL = "gemini-2.5-flash";
    public static final String BASE_URL = "https://generativelanguage.googleapis.com/v1/models/" + MODEL
            + ":generateContent";

    public static String getFullUrl() {
        return BASE_URL + "?key=" + BuildConfig.GEMINI_API_KEY;
    }

    public static String getStreamUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":streamGenerateContent?key="
                + BuildConfig.GEMINI_API_KEY + "&alt=sse";
    }
}
