package com.bachelor.visualpolygon.info;

import java.util.Date;

public class LogRecord {
    private final Date timestamp;
    private final Level level;
    private final String context;
    private final String message;

    public LogRecord(Level level, String context, String message) {
        this.timestamp = new Date();
        this.level = level;
        this.context = context;
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public String getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }
}

