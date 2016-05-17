package daris.client.util;

public interface ProgressMonitor {

    void update(long progress, long total, String message);

}
