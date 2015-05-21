package daris.client.model.file;

import java.util.List;

import arc.mf.client.dti.DTI;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.object.ObjectResolveHandler;

public class LocalHomeDirectory {

    public static void home(final ObjectResolveHandler<DTIDirectory> rh) {
        DTI.fileSystem().roots(new FileHandler() {

            @Override
            public void process(long start, long end, long total, List<LocalFile> files) {
                if (files != null) {
                    for (LocalFile f : files) {
                        if ("Home".equals(f.name()) && f.isDirectory()) {
                            rh.resolved((DTIDirectory) f);
                            return;
                        }
                    }
                }
                rh.resolved(null);
            }
        });
    }

    public static void downloads(final ObjectResolveHandler<DTIDirectory> rh) {
        home(new ObjectResolveHandler<DTIDirectory>() {
            @Override
            public void resolved(final DTIDirectory home) {
                if (home == null) {
                    rh.resolved(null);
                    return;
                }
                FileUtil.getSubDirectory(home, "Downloads", new ObjectResolveHandler<DTIDirectory>() {

                    @Override
                    public void resolved(DTIDirectory downloads) {
                        if (downloads != null) {
                            rh.resolved(downloads);
                            return;
                        }
                        FileUtil.getSubDirectory(home, "My Documents", new ObjectResolveHandler<DTIDirectory>() {

                            @Override
                            public void resolved(DTIDirectory mydocuments) {
                                if (mydocuments != null) {
                                    FileUtil.getSubDirectory(mydocuments, "Downloads", rh);
                                } else {
                                    rh.resolved(home);
                                }
                            }
                        });
                    }
                });
            }
        });
    }
}
