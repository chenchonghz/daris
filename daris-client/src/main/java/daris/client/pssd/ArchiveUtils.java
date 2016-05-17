package daris.client.pssd;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import arc.archive.ArchiveOutput;
import arc.exception.AbortedException;
import arc.streams.StreamCopy.AbortCheck;
import daris.client.util.ProgressMonitor;

public class ArchiveUtils {

    public static void addToArchive(File dir, ArchiveOutput output,
            ProgressMonitor pm, AbortCheck ac) throws Throwable {
        addToArchive(dir, new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !".DS_Store".equals(name);
            }
        }, output, pm, ac);
    }

    public static void addToArchive(File dir, FilenameFilter filter,
            ArchiveOutput output, ProgressMonitor pm, AbortCheck ac)
                    throws Throwable {
        addToArchive(dir.listFiles(filter), filter, dir.getAbsolutePath(),
                output, pm, ac);
    }

    public static void addToArchive(File[] files, FilenameFilter filter,
            String baseDir, ArchiveOutput output, ProgressMonitor pm,
            AbortCheck ac) throws Throwable {
        Arrays.sort(files);
        for (File f : files) {
            String name = f.getAbsolutePath();
            if (name.startsWith(baseDir)) {
                name = name.substring(baseDir.length());
            }
            if (name.startsWith(System.getProperty("file.separator"))) {
                name = name.substring(1);
            }
            if (f.isDirectory()) {
                addToArchive(f.listFiles(), filter, baseDir, output, pm, ac);
            } else {
                if (pm != null) {
                    pm.update(0, 1,
                            "Uploading \"" + f.getAbsolutePath() + "\"...");
                }
                if (ac != null && ac.hasBeenAborted()) {
                    throw new AbortedException();
                }
                output.add(null, name, f);
                if (pm != null) {
                    pm.update(1, 1,
                            "Uploaded \"" + f.getAbsolutePath() + "\".");
                }
            }
        }
    }
}
