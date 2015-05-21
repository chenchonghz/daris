package daris.client.model.file;

import java.util.List;

import arc.mf.client.dti.DTI;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

public class FileUtil {

    /**
     * Get the file extension for a simple filename. Note that this may give
     * incorrect results if called in a pathname with directory components.
     * 
     * @param fileName
     *            the filename.
     * @return the file extension, or null if the filename is null, or does not
     *         have a file extension in the conventional sense.
     */

    public static String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int idx = fileName.lastIndexOf('.');
        if (idx <= 0) {
            return null;
        }
        String ext = fileName.substring(idx + 1).trim();
        if (ext.length() > 0) {
            return ext;
        } else {
            return null;
        }
    }

    public static String getExtension(LocalFile file) {
        return getExtension(file.name());
    }

    public static void subDirectoryExists(DTIDirectory parentDir, final String subDirName, final ObjectMessageResponse<Boolean> rh) {
        DTI.fileSystem().files(LocalFile.Filter.DIRECTORIES, parentDir.path(), 0, Integer.MAX_VALUE, new FileHandler() {

            @Override
            public void process(long start, long end, long total, List<LocalFile> files) {
                if (files != null) {
                    for (LocalFile f : files) {
                        if (f.name().equals(subDirName) && f.isDirectory()) {
                            rh.responded(true);
                            return;
                        }
                    }
                }
                rh.responded(false);
            }
        });
    }

    public static void getSubDirectory(DTIDirectory parentDir, final String subDirName, final ObjectResolveHandler<DTIDirectory> rh) {
        DTI.fileSystem().files(LocalFile.Filter.DIRECTORIES, parentDir.path(), 0, Integer.MAX_VALUE, new FileHandler() {

            @Override
            public void process(long start, long end, long total, List<LocalFile> files) {
                if (files != null) {
                    for (LocalFile f : files) {
                        if (f.name().equals(subDirName) && f.isDirectory()) {
                            rh.resolved((DTIDirectory) f);
                            return;
                        }
                    }
                }
                rh.resolved(null);
            }
        });
    }

}
