package nig.mf.plugin.sink;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import nig.ssh.client.Connection;
import nig.ssh.client.RemoteArchiveExtractor;
import nig.ssh.client.ServerDetails;
import nig.ssh.client.Session;
import nig.ssh.client.Ssh;
import nig.ssh.client.UserDetails;
import nig.util.PathUtil;
import arc.archive.ArchiveInput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.DataSinkImpl;
import arc.mf.plugin.PluginLog;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.DataType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.PasswordType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.sink.ParameterDefinition;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.streams.StreamCopy;
import arc.xml.XmlDoc;

public class ScpSink implements DataSinkImpl {

    public static final String SINK_TYPE = "scp";

    public static final String DEFAULT_FILE_MODE = "0660";

    public static enum ParamDefn {

        // @formatter:off
        HOST("host", StringType.DEFAULT, "The address of the retmote SSH server host."), 
        PORT("port", new IntegerType(1, 65535), "The port of the remote SSH server. Defaults to " + Ssh.DEFAULT_PORT), 
        HOST_KEY("host-key",StringType.DEFAULT, "The public key of the remote SSH server host. If specified, it will be used to verify the remote host. If not specified, accepts any remote host key. Note: RSA and DSA encoded public key are supported. However, ECDSA encoded key is NOT supported."),
        USER("user", StringType.DEFAULT, "The user login for accessing the remote SSH server"),
        PASSWORD("password",PasswordType.DEFAULT, "The user password for accessing the remote SSH server. If not specified, private-key must be specified."),
        PRIVATE_KEY("private-key",PasswordType.DEFAULT, "The user's private key for authenticating with the remote SSH server using pubkey authentication. It assumes the user's public key has been installed on the server, e.g. ~/.ssh/authorized_keys. If not specified, password must be specified."),
        PASSPHRASE("passphrase", PasswordType.DEFAULT, "The passphrase for the private-key. It is only required when the private-key argument is given and it is encrypted."),
        DIRECTORY("directory",StringType.DEFAULT, "The base directory on the remote SSH server. If not set, the user's home direcotry will be used."),
        DECOMPRESS("decompress", BooleanType.DEFAULT, "Indicate whether to decompress the archive. Defaults to false. Note: it can only decompress the recognized archive types, zip, tar, gzip, bzip2 and aar. Also if the calling service e.g. shopping cart services already decompress the archive, turning off the decompress for the sink can do nothing but just transfer the decompressed data."),
        FILE_MODE("file-mode", new StringType(Pattern.compile("\\d{4}")),"The file mode of the remote files. Defaults to 0660." );
        // @formatter:on

        private String _paramName;
        private ParameterDefinition _paramDefn;

        ParamDefn(String name, DataType type, String description) {
            _paramName = name;
            _paramDefn = new ParameterDefinition(type, description);
        }

        @Override
        public final String toString() {
            return paramName();
        }

        public String paramName() {
            return _paramName;
        }

        public ParameterDefinition definition() {
            return _paramDefn;
        }

        private static Map<String, ParameterDefinition> _defns;

        public static Map<String, ParameterDefinition> definitions() {
            if (_defns == null) {
                _defns = new LinkedHashMap<String, ParameterDefinition>();
                ParamDefn[] vs = values();
                for (ParamDefn v : vs) {
                    _defns.put(v.paramName(), v.definition());
                }
            }
            return Collections.unmodifiableMap(_defns);
        }
    }

    private static class Params {
        public final String host;
        public final int port;
        public final String hostKey;
        public final String user;
        public final String password;
        public final String privateKey;
        public final String passphrase;
        public final String directory;
        public final boolean decompress;
        public final String fileMode;

        Params(String host, int port, String hostKey, String user,
                String password, String privateKey, String passphrase,
                String directory, boolean decompress, String fileMode) {
            this.host = host;
            this.port = port;
            this.hostKey = hostKey;
            this.user = user;
            this.password = password;
            this.privateKey = privateKey;
            this.passphrase = passphrase;
            this.directory = directory;
            this.decompress = decompress;
            this.fileMode = fileMode;
        }

        public ServerDetails serverDetails() {
            return new ServerDetails(host, port, hostKey);
        }

        public UserDetails userDetails() {
            return new UserDetails(user, password, privateKey, passphrase,
                    null);
        }

        public static Params parse(Map<String, String> params)
                throws Throwable {
            if (params == null || params.isEmpty()) {
                throw new IllegalArgumentException(
                        "Sink parameters cannot be null or empty.");
            }

            String host = params.get(ParamDefn.HOST.paramName());
            if (host == null) {
                throw new IllegalArgumentException("Missing sink parameter: "
                        + ParamDefn.HOST.paramName());
            }

            String p = params.get(ParamDefn.PORT.paramName());
            int port = p == null ? Ssh.DEFAULT_PORT : Integer.parseInt(p);

            String hostKey = params.get(ParamDefn.HOST_KEY.paramName());
            // if (hostKey == null) {
            // throw new IllegalArgumentException("Missing sink parameter: " +
            // ParamDefn.HOSTKEY.paramName());
            // }

            String user = params.get(ParamDefn.USER.paramName());
            if (user == null) {
                throw new IllegalArgumentException("Missing sink parameter: "
                        + ParamDefn.USER.paramName());
            }

            String password = params.get(ParamDefn.PASSWORD.paramName());
            String privateKey = params.get(ParamDefn.PRIVATE_KEY.paramName());
            if (password == null && privateKey == null) {
                throw new IllegalArgumentException("Missing sink parameter: "
                        + ParamDefn.PASSWORD.paramName() + " or "
                        + ParamDefn.PRIVATE_KEY.paramName()
                        + ". Expecting at least one. Found none.");
            }
            String passphrase = params.get(ParamDefn.PASSPHRASE.paramName());
            if (passphrase != null && privateKey == null) {
                throw new IllegalArgumentException(
                        "passphrase for private-key is given but the private-key is null.");
            }

            String directory = params.get(ParamDefn.DIRECTORY.paramName());

            String d = params.get(ParamDefn.DECOMPRESS.paramName());
            boolean decompress = d == null ? false : Boolean.parseBoolean(d);

            String fileMode = params.get(ParamDefn.FILE_MODE.paramName());
            if (fileMode == null) {
                fileMode = DEFAULT_FILE_MODE;
            }
            return new Params(host, port, hostKey, user, password, privateKey,
                    passphrase, directory, decompress, fileMode);
        }
    }

    private static class MultiTransferContext {

        private static final AtomicLong count = new AtomicLong();

        public final long id;
        public final Connection connection;
        public final Session session;
        public final Set<String> existingDirectories;

        private MultiTransferContext(Connection connection, Session session) {
            this.id = count.incrementAndGet();
            this.connection = connection;
            this.session = session;
            this.existingDirectories = Collections
                    .newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        }

        public int hashCode() {
            return new Long(id).hashCode();
        }

        public boolean equals(Object o) {
            return o != null && (o instanceof MultiTransferContext)
                    && this.id == ((MultiTransferContext) o).id;
        }

    }

    private Set<MultiTransferContext> _mctxs;

    public ScpSink() {
        _mctxs = Collections.newSetFromMap(
                new ConcurrentHashMap<MultiTransferContext, Boolean>());
    }

    @Override
    public String[] acceptedTypes() throws Throwable {
        // all mime types are accepted.
        return null;
    }

    @Override
    public Object beginMultiple(Map<String, String> parameters)
            throws Throwable {

        Params params = Params.parse(parameters);
        ServerDetails serverDetails = params.serverDetails();
        Connection conn = Ssh.get().getConnection(serverDetails);
        Session session = conn.connect(params.userDetails(),
                serverDetails.hostKey() != null);
        MultiTransferContext mctx = new MultiTransferContext(conn, session);
        _mctxs.add(mctx);
        return mctx;
    }

    @Override
    public int compressionLevelRequired() {
        // don't care
        return -1;
    }

    @Override
    public void consume(java.lang.Object multiCtx, java.lang.String path,
            java.util.Map<java.lang.String, java.lang.String> parameters,
            XmlDoc.Element userMeta, XmlDoc.Element meta, LongInputStream in,
            java.lang.String appMimeType, java.lang.String streamMimeType,
            long length) throws Throwable {
        // System.out.println("path: " + path);
        // System.out.println("userMeta: " + userMeta);
        // System.out.println("meta: " + meta);
        // System.out.println("appMimeType: " + appMimeType);
        // System.out.println("streamMimeType: " + streamMimeType);
        Connection conn = multiCtx == null ? null
                : ((MultiTransferContext) multiCtx).connection;
        Session session = multiCtx == null ? null
                : ((MultiTransferContext) multiCtx).session;
        Params params = Params.parse(parameters);
        if (multiCtx == null) {
            conn = Ssh.get().getConnection(params.serverDetails());
            session = conn.connect(params.userDetails(), true);
        }

        String assetId = meta != null ? meta.value("@id") : null;
        String assetName = meta != null ? meta.value("name") : null;
        String ext = meta != null ? meta.value("content/type/@ext") : null;

        try {
            String home = session.getHome();
            String baseDir = params.directory != null ? params.directory : home;
            StringBuilder sb = new StringBuilder(baseDir);
            if (!baseDir.endsWith("/")) {
                sb.append("/");
            }
            if (path != null) {
                path = path.replace("\\\\", "/").replace("\\", "/");
                while (path.startsWith("/")) {
                    path = path.substring(1);
                }
                sb.append(path);
            }

            Set<String> existingDirs = multiCtx == null
                    ? Collections.newSetFromMap(
                            new ConcurrentHashMap<String, Boolean>())
                    : ((MultiTransferContext) multiCtx).existingDirectories;
            if (params.decompress && streamMimeType != null
                    && ArchiveRegistry.isAnArchive(streamMimeType)) {
                // decompress archive
                if (assetName != null) {
                    sb.append("/");
                    sb.append(assetName);                    
                } else if (assetId != null) {
                    sb.append("/");
                    sb.append("asset_");
                    sb.append(assetId);
                }
                if (RemoteArchiveExtractor.canExtract(session,
                        streamMimeType)) {
                    sb.append("/");
                    sb.append(PathUtil.getRandomFileName(8));
                    sb.append(".tmp");
                    String remotePath = sb.toString();
                    transfer(session, in, length, remotePath, params.fileMode,
                            existingDirs);
                    RemoteArchiveExtractor.extract(session, remotePath);
                } else {
                    extractAndTransfer(session, sb.toString(),
                            ArchiveRegistry.createInput(in,
                                    new NamedMimeType(streamMimeType)),
                            params.fileMode, existingDirs);
                }
            } else {
                // single file
                if (assetName != null) {
                    sb.append("/");
                    sb.append(assetName);                    
                } else if (assetId != null) {
                    sb.append("/");
                    sb.append("asset_");
                    sb.append(assetId);
                    if (ext != null) {
                        sb.append(".");
                        sb.append(ext);
                    }
                } else {
                    // meta==null
                    sb.append(System.currentTimeMillis());
                }
                transfer(session, in, length, sb.toString(), params.fileMode,
                        existingDirs);
            }
        } finally {
            if (multiCtx == null) {
                if (session != null) {
                    session.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    private static void extractAndTransfer(Session session, String baseDir,
            ArchiveInput ai, String fileMode, Set<String> existingDirs)
                    throws Throwable {

        ArchiveInput.Entry entry = null;
        try {
            while ((entry = ai.next()) != null) {
                String remotePath = PathUtil.join(baseDir, entry.name());
                if (entry.isDirectory()) {
                    session.mkdir(remotePath, true, "0755");
                    existingDirs.add(remotePath);
                } else {
                    transfer(session, entry.stream(), entry.size(), remotePath,
                            fileMode, existingDirs);
                }
            }
        } finally {
            ai.close();
        }

    }

    private static void transfer(final Session session, InputStream in,
            long length, final String remoteFilePath, String fileMode,
            Set<String> existingDirs) throws Throwable {

        String remoteDirPath = PathUtil.getParentDirectory(remoteFilePath);
        if (existingDirs == null || !existingDirs.contains(remoteDirPath)) {
            session.mkdir(remoteDirPath, true, "0755");
            if (existingDirs != null) {
                existingDirs.add(remoteDirPath);
            }
        }
        if (length < 0 && in instanceof LongInputStream) {
            length = ((LongInputStream) in).length();
        }
        if (length < 0) {
            File tf = PluginTask.createTemporaryFile();
            OutputStream tfos = new BufferedOutputStream(
                    new FileOutputStream(tf));
            try {
                StreamCopy.copy(in, tfos);
            } finally {
                tfos.close();
                in.close();
            }
            in = PluginTask.deleteOnCloseInputStream(tf);
            length = tf.length();
        }

        OutputStream out = null;
        try {
            out = session.scpPut(remoteFilePath, length, fileMode);
            StreamCopy.copy(in, out);
        } finally {
            if (out != null) {
                out.close();
            }
            in.close();
        }
    }

    @Override
    public String description() throws Throwable {
        return "scp sink";
    }

    @Override
    public void endMultiple(Object ctx) throws Throwable {
        MultiTransferContext mctx = (MultiTransferContext) ctx;
        if (mctx.session != null) {
            mctx.session.close();
        }
        if (mctx.connection != null) {
            mctx.connection.disconnect();
        }
        _mctxs.remove(mctx);
    }

    @Override
    public Map<String, ParameterDefinition> parameterDefinitions()
            throws Throwable {
        return ParamDefn.definitions();
    }

    @Override
    public void shutdown() throws Throwable {
        for (MultiTransferContext mctx : _mctxs) {
            try {
                mctx.session.close();
                mctx.connection.disconnect();
            } catch (Throwable e) {
                PluginLog.log().add(e);
            }
        }
        _mctxs.clear();
    }

    @Override
    public String type() throws Throwable {
        return SINK_TYPE;
    }

}
