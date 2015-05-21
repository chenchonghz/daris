package nig.mf.plugin.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;

import arc.mf.plugin.PluginTask;

public class TempFileImageInputStream extends FileImageInputStream {

    private File _tf;

    public TempFileImageInputStream(File tf) throws FileNotFoundException, IOException {
        super(tf);
        _tf = tf;
    }

    @Override
    public void close() throws IOException {
        super.close();
        try {
            PluginTask.deleteTemporaryFile(_tf);
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            e.printStackTrace(System.out);
        }
    }
}