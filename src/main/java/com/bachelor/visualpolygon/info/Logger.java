package com.bachelor.visualpolygon.info;

import lombok.Setter;

import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


public class Logger {
    private static Logger instace;
    @Setter
    private String context;
    private static final int MAX_LOG_ENTRIES = 1_000_000;
    private static final BlockingDeque<LogRecord> logCollection = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);


    private Logger() {
        context = "START: ";
    }

    public static Logger getLogger() {
        if (instace == null) {
            return new Logger();
        }
        return instace;
    }


    public void log(LogRecord logRecord) {
        logCollection.offer(logRecord);
    }

    public void debug(String msg) {
        log(new LogRecord(Level.DEBUG, context, msg));
    }

    public  void info(String msg) {
        log(new LogRecord(Level.INFO, context, msg));
    }

    public  void warn(String msg) {
        log(new LogRecord(Level.WARN, context, msg));
    }

    public  void error(String msg) {
        log(new LogRecord(Level.ERROR, context, msg));
    }


    public  void drainTo(Collection<? super LogRecord> collection) {
        logCollection.drainTo(collection);
    }


}
