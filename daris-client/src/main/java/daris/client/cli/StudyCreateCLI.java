package daris.client.cli;

import java.io.PrintStream;
import java.util.List;

import arc.mf.client.ServerClient.Connection;
import daris.client.pssd.StudyUtils;

public class StudyCreateCLI extends ServerProcedureCLI {

    private String _pid;
    private String _step;
    private String _name;
    private String _description;
    private boolean _quiet = false;

    protected StudyCreateCLI(String[] args) throws Throwable {
        super(args);
    }

    @Override
    protected String name() {
        return "daris-study-create";
    }

    @Override
    protected void printCommandOptionsUsage(PrintStream s) {
        s.println(
                "  --pid <pid>  The citable id of the parent subject/ex-method.");
        s.println("  --step <step>  The step path of the method.");
        s.println("  --name <name>  The study name.");
        s.println("  --description <description>  The study description.");
        s.println(
                "  --quiet <true|false>  Do not print progress information. Defaults to false.");

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
            } else if (args.get(i).equals("--step")) {
                if (_step != null) {
                    throw new IllegalArgumentException(
                            "More than one --step specified. Expects only one.");
                }
                _step = args.get(i + 1);
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
        if (_pid == null) {
            throw new IllegalArgumentException("Missing --pid argument.");
        }
    }

    @Override
    protected void execute(Connection cxn) throws Throwable {
        String studyCid = StudyUtils.findOrCreateStudy(cxn, _pid, _step, _name,
                _description, _quiet ? null : System.err);
        System.out.println(studyCid);
    }

    public static void main(String[] args) {
        StudyCreateCLI cmd = null;
        try {
            cmd = new StudyCreateCLI(args);
            cmd.execute();
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            if (cmd != null && (t instanceof IllegalArgumentException)) {
                cmd.printUsage(System.err);
            }
        }
    }

}
