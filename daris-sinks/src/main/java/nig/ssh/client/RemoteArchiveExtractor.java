package nig.ssh.client;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import nig.util.PathUtil;

public class RemoteArchiveExtractor {

    public static final String APPLICATION_ZIP = "application/zip";
    public static final String APPLICATION_X_TAR = "application/x-tar";
    public static final String APPLICATION_X_GZIP = "application/x-gzip";
    public static final String APPLICATION_X_BZIP2 = "application/x-bzip2";
    public static final String APPLICATION_JAR = "application/java-archive";

    private static Set<String> _supportedMimeTypes;

    private static Set<String> supportedMimeTypes() {
        if (_supportedMimeTypes == null) {
            _supportedMimeTypes = new HashSet<String>();
            _supportedMimeTypes.add(APPLICATION_ZIP);
            _supportedMimeTypes.add(APPLICATION_JAR);
            _supportedMimeTypes.add(APPLICATION_X_TAR);
            _supportedMimeTypes.add(APPLICATION_X_GZIP);
            _supportedMimeTypes.add(APPLICATION_X_BZIP2);
        }
        return _supportedMimeTypes;
    }

    private static boolean isMimeTypeSupported(String mimeType) {
        if (mimeType != null) {
            Set<String> types = supportedMimeTypes();
            return types.contains(mimeType);
        } else {
            return false;
        }
    }

    // @formatter:off
    public static final String SCRIPT = new StringBuilder()
            .append("#!/bin/bash\n")
            .append("\n")
            .append("CWD=$(pwd)\n")
            .append("while (( \"$#\" )); do\n")
            .append("    file=$1\n")
            .append("    cd $(dirname $file)\n")
            .append("    ext=${file#*.}\n")
            .append("    type=$(file -b --mime-type \"${file}\")\n")
            .append("    if [[ ${type} == \"application/zip\" ]]; then\n")
            .append("        unzip -o \"$file\"\n")
            .append("        rm \"$file\"\n")
            .append("    elif [[ ${type} == \"application/x-gzip\" ]]; then\n")
            .append("        if [[ ${file} == *.tar.gz || ${file} == *.tgz ]]; then\n")
            .append("            tar zxvf \"$file\"\n")
            .append("            rm \"$file\"\n")
            .append("        else\n")
            .append("            if [[ ${ext} != \"gz\" ]]; then\n")
            .append("                mv \"$file\" \"${file}.gz\"\n")
            .append("                gunzip \"${file}.gz\"\n")
            .append("            else\n")
            .append("                gunzip \"${file}\"\n")
            .append("                file=${file%.*}\n")
            .append("            fi\n")
            .append("            if [[ $(file -b --mime-type \"${file}\") == \"application/x-tar\" ]]; then\n")
            .append("                tar xvf \"${file}\"\n")
            .append("            fi\n")
            .append("            rm \"$file\"\n")
            .append("        fi\n")
            .append("    elif [[ ${type} == \"application/x-bzip2\" ]]; then\n")
            .append("        if [[ ${file} == *.tar.bz2 || ${file} == *.tb2 || ${file} == *.tbz ]]; then\n")
            .append("            tar jxvf \"$file\"\n")
            .append("            rm \"$file\"\n")
            .append("        else\n")
            .append("            if [[ ${ext} != \"bz2\" ]]; then\n")
            .append("                mv $file ${file}.bz2\n")
            .append("                bunzip2 \"${file}.bz2\"\n")
            .append("            else\n")
            .append("                bunzip2 \"${file}\"\n")
            .append("                file=${file%.*}\n")
            .append("            fi\n")
            .append("            if [[ $(file -b --mime-type \"${file}\") == \"application/x-tar\" ]]; then\n")
            .append("                tar xvf \"${file}\"\n")
            .append("            fi\n")
            .append("            rm \"$file\"\n")
            .append("        fi\n")
            .append("    fi\n")
            .append("    shift\n")
            .append("    cd $CWD\n")
            .append("done\n")
            .append("exit 0").toString();
    // @formatter:on

    public static void extract(Session session, String filePath) throws Throwable {
        byte[] b = SCRIPT.getBytes();
        String dir = PathUtil.getParentDirectory(filePath);
        String scriptName = "tmp_extract_" + PathUtil.getRandomFileName(5) + ".sh";
        String scriptPath = PathUtil.join(dir, scriptName);
        OutputStream out = session.scpPut(scriptPath, b.length, "0700");
        try {
            out.write(b);
            out.flush();
        } finally {
            out.close();
        }
        try {
            session.exec(scriptPath + " \"" + filePath + "\"");
        } finally {
            session.execCommand("rm " + scriptPath);
        }
    }

    public static boolean available(Session session) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("[[ ! -z $(which file) ]] && ");
        sb.append("[[ ! -z $(which unzip) ]] && ");
        sb.append("[[ ! -z $(which tar) ]] && ");
        sb.append("[[ ! -z $(which gunzip) ]] && ");
        sb.append("[[ ! -z $(which bunzip2) ]] && exit 0");
        return session.execCommand(sb.toString()).exitStatus == 0;
    }

    public static boolean canExtract(Session session, String mimeType) throws Throwable {
        return isMimeTypeSupported(mimeType) && available(session);
    }
}
