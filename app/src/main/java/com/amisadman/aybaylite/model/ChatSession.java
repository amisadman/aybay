package com.amisadman.aybaylite.model;

public class ChatSession
{
    private final String sessionId;
    private String title;
    private final long lastUpdated;

    public ChatSession(String sessionId, String title, long lastUpdated)
    {
        this.sessionId = sessionId;
        this.title = title;
        this.lastUpdated = lastUpdated;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public String getTitle()
    {
        return title;
    }

    public long getLastUpdated()
    {
        return lastUpdated;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
}
