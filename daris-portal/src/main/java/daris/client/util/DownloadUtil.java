package daris.client.util;

import arc.mf.client.Output;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Frame;

public class DownloadUtil {

    private static Frame _downloadFrame;

    public static void download(Output output, String fileName) {
        String url = output.url() + "&filename=" + fileName;
        download(url);
    }

    public static void download(String url) {
        if (_downloadFrame == null) {
            _downloadFrame = Frame.wrap(Document.get().getElementById(
                    "__gwt_downloadFrame"));
        }
        _downloadFrame.setUrl(url);
    }

}
