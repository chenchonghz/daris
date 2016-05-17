package daris.client.cli;

import java.io.PrintStream;
import java.util.List;

import arc.mf.client.ServerClient.Connection;
import daris.client.pssd.SubjectUtils;

public class SubjectCreateCLI extends ServerProcedureCLI {

    private String _projectCid;
    private String _methodCid;
    private String _name;
    private String _description;
    private boolean _quiet = false;

    protected SubjectCreateCLI(String[] args) throws Throwable {
        super(args);
    }

    @Override
    protected String name() {
        return "daris-subject-create";
    }

    @Override
    protected void printCommandOptionsUsage(PrintStream s) {
        s.println("  --pid <pid>  The citable id of the parent project.");
        s.println(
                "  --method <method>  The citable id of the method. Must be specified if the parent project has more than one methods.");
        s.println("  --name <name>  The subject name.");
        s.println("  --description <description>  The subject description.");
        s.println(
                "  --quiet <true|false>  Do not print progress information. Defaults to false.");

    }

    @Override
    protected void parseCommandOptions(List<String> args) throws Throwable {
        int n = args.size();
        for (int i = 0; i < n;) {
            if (args.get(i).equals("--pid")) {
                if (_projectCid != null) {
                    throw new IllegalArgumentException(
                            "More than one --pid specified. Expects only one.");
                }
                _projectCid = args.get(i + 1);
                i += 2;
            } else if (args.get(i).equals("--method")) {
                if (_methodCid != null) {
                    throw new IllegalArgumentException(
                            "More than one --method specified. Expects only one.");
                }
                _methodCid = args.get(i + 1);
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
            } else if (args.get(i).equals("--quiet")) {
                _quiet = Boolean.parseBoolean(args.get(i + 1));
                i += 2;
            } else {
                throw new IllegalArgumentException(
                        "Unexpected argument: " + args.get(i));
            }
        }
        if (_projectCid == null) {
            throw new IllegalArgumentException("Missing --pid argument.");
        }

    }

    @Override
    protected void execute(Connection cxn) throws Throwable {
        String subjectCid = SubjectUtils.findOrCreateSubject(cxn, _projectCid,
                _methodCid, _name, _description, _quiet ? null : System.err);
        System.out.println(subjectCid);
    }

    public static void main(String[] args) {
        SubjectCreateCLI cmd = null;
        try {
            cmd = new SubjectCreateCLI(args);
            cmd.execute();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            if (cmd != null && (t instanceof IllegalArgumentException)) {
                cmd.printUsage(System.err);
            }
        }
    }

}
