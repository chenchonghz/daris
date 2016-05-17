package daris.client.cli;

import java.io.PrintStream;

import daris.client.util.ProgressMonitor;

public class PrintStreamProgressMonitor implements ProgressMonitor {

    private PrintStream _ps;

    public PrintStreamProgressMonitor(PrintStream ps) {
        _ps = ps;
    }

    @Override
    public void update(long progress, long total, String message) {
        _ps.println(message);
    }

}
