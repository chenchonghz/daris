package nig.mf.plugin.pssd.servlets;

import java.util.ArrayList;
import java.util.List;

import nig.mf.plugin.pssd.sc.Transcode;
import arc.mf.plugin.http.HttpRequest;

public class TranscodeCodec {

    public static Transcode decodeTranscode(String transcodeToken) {
        if (transcodeToken == null) {
            return null;
        }
        transcodeToken = transcodeToken.trim();
        int idx = transcodeToken.indexOf(ObjectServlet.TRANSCODE_VALUE_SEPARATOR);
        if (idx == -1) {
            // no separator, value is considered to_type
            return new Transcode(null, transcodeToken);
        }
        if (transcodeToken.startsWith(ObjectServlet.TRANSCODE_VALUE_SEPARATOR)) {
            return new Transcode(null, transcodeToken.substring(1));
        }
        if (transcodeToken.endsWith(ObjectServlet.TRANSCODE_VALUE_SEPARATOR)) {
            return null;
        }
        String[] types = transcodeToken.split(ObjectServlet.TRANSCODE_VALUE_SEPARATOR);
        if (types.length == 2) {
            return new Transcode(types[0], types[1]);
        }
        return null;
    }

    public static List<Transcode> decodeTranscodes(String transcodeTokens) {
        String[] tokens = transcodeTokens.split("\\" + ObjectServlet.TRANSCODE_TOKEN_SEPARATOR);
        List<Transcode> transcodes = new ArrayList<Transcode>();
        for (String token : tokens) {
            Transcode transcode = decodeTranscode(token);
            if (transcode != null) {
                transcodes.add(transcode);
            }
        }
        if (transcodes.isEmpty()) {
            return null;
        }
        return transcodes;
    }

    public static List<Transcode> decodeTranscodes(HttpRequest request) {
        String transcodeTokens = request.variableValue(ObjectServlet.ARG_TRANSCODE);
        if (transcodeTokens == null) {
            return null;
        } else {
            return decodeTranscodes(transcodeTokens);
        }
    }

    public static String encodeTranscode(Transcode transcode) {
        if (transcode == null || transcode.to == null) {
            return null;
        }
        if (transcode.from == null) {
            return transcode.to;
        }
        return transcode.from + ObjectServlet.TRANSCODE_VALUE_SEPARATOR + transcode.to;
    }

    public static String encodeTranscodes(List<Transcode> transcodes) {
        if (transcodes == null || transcodes.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Transcode t : transcodes) {
            String token = encodeTranscode(t);
            if (token != null) {
                if (!first) {
                    sb.append(ObjectServlet.TRANSCODE_TOKEN_SEPARATOR);
                } else {
                    first = false;
                }
                sb.append(token);
            }
        }
        String transcodeTokens = sb.toString();
        if (transcodeTokens.isEmpty()) {
            return null;
        } else {
            return transcodeTokens;
        }
    }

}
