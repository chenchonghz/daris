package daris.util;

import java.io.OutputStream;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class LoggingUtils {

    public static final int MB = 1000000;
    public static final int DEFAULT_FILE_SIZE_LIMIT = MB * 10;
    public static final int DEFAULT_NUM_FILES_TO_USE = 2;

    public static Formatter DEFAULT_FORMATTER = new Formatter() {
        @Override
        public String format(LogRecord record) {
            StringBuilder sb = new StringBuilder();
            sb.append(new Date(record.getMillis())).append(" ");
            sb.append("[thread: ").append(record.getThreadID()).append("] ");
            sb.append(record.getLevel().getName().toUpperCase()).append(" ");
            sb.append(record.getMessage());
            sb.append("\n");
            Throwable error = record.getThrown();
            if (error != null) {
                sb.append(ThrowableUtils.getStackTrace(error));
                sb.append("\n");
            }
            return sb.toString();
        }
    };

    private static String getDefaultLogFileNamePattern(String name) {
        return System.getProperty("user.dir") + "/" + name + "." + "%g.log";
    }

    public static Logger createLogger(String name, Level level, boolean useParentHandlers) {
        Logger logger = Logger.getLogger(name);
        logger.setLevel(level);
        logger.setUseParentHandlers(useParentHandlers);
        return logger;
    }

    public static FileHandler createFileHandler(String logFileNamePattern, int sizeLimitInBytes, int numLogFilesToUse,
            Level level, Formatter formatter) throws Throwable {
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler(logFileNamePattern, sizeLimitInBytes, numLogFilesToUse);
        } catch (Throwable e) {
            throw new Exception("failed to create log file from pattern: '" + logFileNamePattern + "'.", e);
        }
        if (fileHandler != null) {
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(level);
        }
        return fileHandler;
    }

    public static FileHandler createFileHandler(String logFileNamePattern, int sizeLimitInBytes, int numLogFilesToUse,
            Level level) throws Throwable {
        return createFileHandler(logFileNamePattern, sizeLimitInBytes, numLogFilesToUse, level, DEFAULT_FORMATTER);
    }

    public static FileHandler createFileHandler(String name, Level level) throws Throwable {
        return createFileHandler(getDefaultLogFileNamePattern(name), DEFAULT_FILE_SIZE_LIMIT, DEFAULT_NUM_FILES_TO_USE,
                level, DEFAULT_FORMATTER);
    }

    public static FileHandler createFileHandler(String name) throws Throwable {
        return createFileHandler(getDefaultLogFileNamePattern(name), DEFAULT_FILE_SIZE_LIMIT, DEFAULT_NUM_FILES_TO_USE,
                Level.ALL, DEFAULT_FORMATTER);
    }

    public static StreamHandler createStreamHandler(OutputStream stream, Level level, Formatter formatter) {
        StreamHandler streamHandler = new StreamHandler(stream, formatter) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                super.flush();
            }
        };
        streamHandler.setLevel(level);
        return streamHandler;
    }

    public static StreamHandler createStreamHandler(OutputStream stream, Level level) {
        return createStreamHandler(stream, level, DEFAULT_FORMATTER);
    }

    public static StreamHandler createStreamHandler(OutputStream stream) {
        return createStreamHandler(stream, Level.ALL);
    }
}
