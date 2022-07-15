package com.bachelor.visualpolygon.info;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Logger {

    private static String context;
    private static final int MAX_LOG_ENTRIES = 1_000_000;
    private static final BlockingDeque<LogRecord> logCollection = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);


    public Logger(String context) {
        this.context = context;
    }

    public static void log(LogRecord record) {
        logCollection.offer(record);
    }

    public static void debug(String msg) {
        log(new LogRecord(Level.DEBUG, context, msg));
    }

    public static void info(String msg) {
        log(new LogRecord(Level.INFO, context, msg));
    }

    public static void warn(String msg) {
        log(new LogRecord(Level.WARN, context, msg));
    }

    public static void error(String msg) {
        log(new LogRecord(Level.ERROR, context, msg));
    }


    public static void drainTo(Collection<? super LogRecord> collection) {
        logCollection.drainTo(collection);
    }


}
