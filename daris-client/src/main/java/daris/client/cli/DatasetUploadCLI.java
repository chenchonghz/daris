package daris.client.cli;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import arc.mf.client.ServerClient.Connection;
import daris.client.pssd.ArchiveType;
import daris.client.pssd.DatasetUtils;

public class DatasetUploadCLI extends ServerProcedureCLI {

    private String _pid;
    private String _studyName;
    private boolean _derived = false;
    private Set<String> _inputCids;
    private String _name;
    private String _description;
    private String _type;
    private ArchiveType _archiveType;
    private boolean _gzip = false;
    private File _input = null;
    private boolean _quiet = false;

    protected DatasetUploadCLI(String[] args) throws Throwable {
        super(args);
    }

    @Override
    protected void parseCommandOptions(List<String> args) throws Throwable {
        int n = args.size();
        for (int i = 0; i < n;) {
            if (args.get(i).equals("--pid")) {
                if (_pid != null) {
                    throw new IllegalArgumentException(
                            "More than one --pid specified. Expects only one.");
                }
                _pid = args.get(i + 1);
                i += 2;
            } else if (args.get(i).equals("--study-name")) {
                if (_studyName != null) {
                    throw new IllegalArgumentException(
                            "More than one --study-name specified. Expects only one.");
                }
                _studyName = args.get(i + 1);
                i += 2;
            } else if (args.get(i).equals("--derived")) {
                _derived = Boolean.parseBoolean(args.get(i + 1));
                i += 2;
            } else if (args.get(i).equals("--input-cid")) {
                if (_inputCids != null) {
                    _inputCids = new LinkedHashSet<String>();
                }
                _inputCids.add(args.get(i + 1));
                i += 2;
            } else if (args.get(i).equals("--name")) {
                if (_name != null) {
                    throw new IllegalArgumentException(
                            "More than one --name specified. Expects only one.");
                }
                _name = args.get(i + 1);
                i += 2;
            } else if (args.get(i).equals("--description")) {
                if (_description != null) {
                    throw new IllegalArgumentException(
                            "More than one --description specified. Expects only one.");
                }
                _description = args.get(i + 1);
                i += 2;
            } else if (args.get(i).equals("--type")) {
                if (_type != null) {
                    throw new IllegalArgumentException(
                            "More than one --type specified. Expects only one.");
                }
                _type = args.get(i + 1);
                i += 2;
            } else if (args.get(i).equals("--archive-type")) {
                if (_archiveType != null) {
                    throw new IllegalArgumentException(
                            "More than one --archive-type specified. Expects only one.");
                }
                _archiveType = ArchiveType.fromString(args.get(i + 1),
                        ArchiveType.AAR);
                i += 2;
            } else if (args.get(i).equals("--gzip")) {
                _gzip = Boolean.parseBoolean(args.get(i + 1));
                i += 2;
            } else if (args.get(i).equals("--input")) {
                if (_input != null) {
                    throw new IllegalArgumentException(
                            "More than one --input specified. Expects only one.");
                }
                _input = new File(args.get(i + 1));
                if (!_input.exists()) {
                    throw new IllegalArgumentException("Input file: '"
                            + _input.getAbsolutePath() + "' does not exist");
                }
                i += 2;
            } else if (args.get(i).equals("--quiet")) {
                _quiet = Boolean.parseBoolean(args.get(i + 1));
                i += 2;
            } else {
                throw new IllegalArgumentException(
                        "Unexpected argument: " + args.get(i));
            }
        }
        if (_pid == null) {
            throw new IllegalArgumentException("Missing --pid argument.");
        }
        if (_input == null) {
            throw new IllegalArgumentException("Missing --input argument.");
        }
    }

    @Override
    protected void execute(Connection cxn) throws Throwable {
        String datasetCid = DatasetUtils.uploadDataset(cxn, _pid, _studyName,
                _inputCids, _derived, _name, _description, _input, _gzip,
                _archiveType, _type,
                _quiet ? null : new PrintStreamProgressMonitor(System.err));
        System.out.println(datasetCid);
    }

    @Override
    protected String name() {
        return "daris-dataset-upload";
    }

    @Override
    protected void printCommandOptionsUsage(PrintStream s) {
        s.println(
                "  --pid <pid>  The citable id of the parent (study/ex-method/subject).");
        s.println(
                "  --study-name <study-name>  The parent study name. If specified, it will be used to find the parent study.");
        s.println(
                "  --derived <true|false>  Indicates if the dataset is primary or derived. Defaults to false.");
        s.println(
                "  --input-cid <input-cid>  The citable id of the input dataset. Only applicable to derived dataset.");
        s.println(
                "  --name <name>  The dataset name. If not given, defaults to the input directory/file name.");
        s.println(
                "  --description <description>  The dataset description. If not given, defaults to the input directory/file name.");
        s.println("  --type <type>  The dataset mime type.");
        s.println(
                "  --gzip <true|false> Gzip the dataset content file. Only applicable if the input is a single file. Defaults to false.");
        s.println(
                "  --archive-type <zip|aar> The dataset content archive type. Only applicable if the input is a directory. Defaults to aar.");
        s.println(
                "  --quiet <true|false>  Do not print uploading progress information. Defaults to false.");
        s.println("  --input <file|directory>  The input file or directory.");
    }

    public static void main(String[] args) {
        DatasetUploadCLI cmd = null;
        try {
            cmd = new DatasetUploadCLI(args);
            cmd.execute();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            if (cmd != null && (t instanceof IllegalArgumentException)) {
                cmd.printUsage(System.err);
            }
        }
    }
}
