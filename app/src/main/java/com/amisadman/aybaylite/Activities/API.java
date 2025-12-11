package com.amisadman.aybaylite.Activities;

public final class API {
    public static final String MODEL = "gemini-2.5-flash";
    public static final String BASE_URL = "https://generativelanguage.googleapis.com/v1/models/" + MODEL
            + ":generateContent";
    private static final String API_KEY = "AIzaSyDZrXWR-gGwsHQ1lqA__esE0VHmYPrcJLM";

    public static String getFullUrl() {
        return BASE_URL + "?key=" + API_KEY;
    }

    public static String getStreamUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":streamGenerateContent?key="
                + API_KEY + "&alt=sse";
    }
}
