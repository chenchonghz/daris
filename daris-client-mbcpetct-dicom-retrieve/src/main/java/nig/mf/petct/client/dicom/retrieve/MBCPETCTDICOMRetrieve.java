package nig.mf.petct.client.dicom.retrieve;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Vector;

import nig.mf.client.util.ClientConnection;
import nig.util.DateUtil;

import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class MBCPETCTDICOMRetrieve {

    private static final String MF_NAMESPACE = "MBIC-Archive";

    // This class sets the defaults for arguments that can be passed in
    // to the main program
    private static class Options {

        // mf-dicom-patient
        public String firstName = null;
        public String lastName = null;

        // mf-dicom-series
        public String startDate = null;
        public String endDate = null;
        public String modality = null;
        public String description = null;
        public String protocol = null;

        // find by asset IDs
        public String patientAsset = null;
        public String studyAsset = null;
        public String seriesAsset = null;
        //
        public String callingAET = null;
        public String calledAET = null;
        public String host = null;
        public String port = null;
        public boolean decrypt = true;

        // Juts list result don't do anything
        public boolean list = false;
        //
        public boolean hasAttributes = false;

        public void print() {
            System.out.println("list           = " + list);
            System.out.println("first          = " + firstName);
            System.out.println("last           = " + lastName);
            System.out.println("start          = " + startDate);
            System.out.println("end            = " + endDate);
            System.out.println("desc           = " + description);
            System.out.println("modality       = " + modality);
            System.out.println("protocol       = " + protocol);
            System.out.println("patient asset  = " + patientAsset);
            System.out.println("study   asset  = " + studyAsset);
            System.out.println("series  asset  = " + seriesAsset);
            System.out.println("callingAET     = " + callingAET);
            System.out.println("calledAET      = " + calledAET);
            System.out.println("host           = " + host);
            System.out.println("port           = " + port);
            System.out.println("no-decrypt    = " + !decrypt);
        }
    }

    public static final String HELP_ARG = "-help";
    // Patient
    public static final String FIRSTNAME_ARG = "-first";
    public static final String LASTNAME_ARG = "-last";
    // Series
    public static final String STARTDATE_ARG = "-start";
    public static final String ENDDATE_ARG = "-end";
    public static final String MODALITY_ARG = "-modality";
    public static final String DESCRIPTION_ARG = "-desc";
    public static final String PROTOCOL_ARG = "-protocol";
    // Assets
    public static final String PATIENT_ASSET_ARG = "-patient";
    public static final String STUDY_ASSET_ARG = "-study";
    public static final String SERIES_ASSET_ARG = "-series";
    //
    public static final String CALLING_AET_ARG = "-callingAET"; // CLient
    public static final String CALLED_AET_ARG = "-calledAET"; // Server
    public static final String HOST_ARG = "-host";
    public static final String PORT_ARG = "-port";
    //
    public static final String LIST_ARG = "-list";
    public static final String DECRYPT_ARG = "-no-decrypt";

    /**
     * 
     * the main function of this command line tool.
     * 
     * @param args
     * 
     */
    public static void main(String[] args) throws Throwable {

        Options ops = new Options();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase(HELP_ARG)) {
                printHelp(System.out);
                System.exit(0);
            } else if (args[i].equalsIgnoreCase(FIRSTNAME_ARG)) {
                i++;
                ops.firstName = args[i];
            } else if (args[i].equalsIgnoreCase(LASTNAME_ARG)) {
                i++;
                ops.lastName = args[i];
            } else if (args[i].equalsIgnoreCase(STARTDATE_ARG)) {
                i++;
                ops.startDate = args[i];
            } else if (args[i].equalsIgnoreCase(ENDDATE_ARG)) {
                i++;
                ops.endDate = args[i];
            } else if (args[i].equalsIgnoreCase(MODALITY_ARG)) {
                i++;
                ops.modality = args[i];
            } else if (args[i].equalsIgnoreCase(DESCRIPTION_ARG)) {
                i++;
                ops.description = args[i];
            } else if (args[i].equalsIgnoreCase(PROTOCOL_ARG)) {
                i++;
                ops.protocol = args[i];
            } else if (args[i].equalsIgnoreCase(PATIENT_ASSET_ARG)) {
                i++;
                ops.patientAsset = args[i];
            } else if (args[i].equalsIgnoreCase(STUDY_ASSET_ARG)) {
                i++;
                ops.studyAsset = args[i];
            } else if (args[i].equalsIgnoreCase(SERIES_ASSET_ARG)) {
                i++;
                ops.seriesAsset = args[i];
            } else if (args[i].equalsIgnoreCase(CALLING_AET_ARG)) {
                i++;
                ops.callingAET = args[i];
            } else if (args[i].equalsIgnoreCase(CALLED_AET_ARG)) {
                i++;
                ops.calledAET = args[i];
            } else if (args[i].equalsIgnoreCase(HOST_ARG)) {
                i++;
                ops.host = args[i];
            } else if (args[i].equalsIgnoreCase(PORT_ARG)) {
                i++;
                ops.port = args[i];
            } else if (args[i].equalsIgnoreCase(LIST_ARG)) {
                ops.list = true;
            } else if (args[i].equalsIgnoreCase(DECRYPT_ARG)) {
                ops.decrypt = false;
            } else {
                System.err.println(
                        "MBCPETCTDICOMRetrieve: error: unexpected argument = "
                                + args[i]);
                printHelp(System.err);
                System.exit(1);
            }
        }
        ops.print();

        // Check inputs

        // FOrce a full name if first name given
        if ((ops.firstName != null && ops.lastName == null)) {
            throw new Exception(
                    "*** You must specify the last name if you give the first name.");
        }

        // Validate date formats are in desired Mediaflux syntax
        if (ops.startDate != null) {
            try {
                DateUtil.dateFromString(ops.startDate, "dd-MMM-yyyy");
            } catch (Throwable t) {
                throw new Exception("*** Start date (" + ops.startDate
                        + ") not in correct format: must be of the form dd-MMM-yyyy");
            }
        }
        if (ops.endDate != null) {
            try {
                DateUtil.dateFromString(ops.endDate, "dd-MMM-yyyy");
            } catch (Throwable t) {
                throw new Exception("*** End date (" + ops.endDate
                        + ") not in correct format: must be of the form dd-MMM-yyyy");
            }
        }

        if (ops.modality != null) {
            ops.modality = ops.modality.toUpperCase();
            if (!ops.modality.equals("PT") && !ops.modality.equals("CT")
                    && !ops.modality.equals("NM")) {
                throw new Exception(
                        "*** Modality must be one of 'PT','CT', 'NM'");
            }
        }
        // Can select by attributes or by asset ID
        if (ops.firstName != null || ops.lastName != null
                || ops.startDate != null || ops.endDate != null
                || ops.modality != null || ops.description != null
                || ops.protocol != null)
            ops.hasAttributes = true;
        if (ops.hasAttributes) {
            if (ops.patientAsset != null || ops.studyAsset != null
                    || ops.seriesAsset != null) {
                throw new Exception(
                        "*** You cannot give selection attributes as well as asset IDs");
            }
        } else {
            if (ops.patientAsset == null && ops.studyAsset == null
                    && ops.seriesAsset == null) {
                throw new Exception(
                        "*** You must supply either selection attributes or asset IDs");
            }
        }

        // DICOM AE defaults to console
        if (ops.calledAET == null)
            ops.calledAET = "MBIC-PET-CONSOLE"; // The AET of the PET COnsole
        if (ops.host == null)
            ops.host = "172.30.31.75";
        if (ops.port == null)
            ops.port = "104";
        //
        if (ops.callingAET == null)
            ops.callingAET = "UM-DaRIS-1"; // THe AET of the DaRIS1 system

        System.out.println("");
        System.out.println("");
        System.out.println("");
        try {
            // Push data
            pushData(ops);
        } catch (Throwable t) {
            System.err
                    .println("MBCPETCTDICOMRetrieve: error: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void pushData(Options ops) throws Throwable {

        // Make connection to MF server
        Connection cxn = ClientConnection.createServerConnection();
        if (ops.decrypt) {
            ClientConnection.connect(cxn, true);
        } else {
            ClientConnection.connect(cxn, false);
        }

        //
        if (ops.hasAttributes) {
            findAndPush(cxn, ops);
        } else {
            push(cxn, ops);
        }

        // CLose connection
        cxn.close();
    }

    private static void push(Connection cxn, Options ops) throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        w.push("called-ae"); // Destination
        w.add("title", ops.calledAET);
        w.add("host", ops.host);
        w.add("port", ops.port);
        w.pop();
        //
        w.push("calling-ae");
        w.add("title", ops.callingAET); // Sender
        w.pop();
        if (ops.patientAsset != null) {
            w.add("id", ops.patientAsset);
            if (ops.list)
                System.out.println("Patient asset = " + ops.patientAsset);
        } else if (ops.studyAsset != null) {
            w.add("id", ops.studyAsset);
            if (ops.list)
                System.out.println("Study asset = " + ops.studyAsset);
        } else if (ops.seriesAsset != null) {
            w.add("id", ops.seriesAsset);
            w.add("series", ops.seriesAsset);
            if (ops.list)
                System.out.println("Series asset = " + ops.seriesAsset);
        }
        //
        if (!ops.list) {
            System.out.println("Sending data");
            boolean sent = cxn.execute("daris.dicom.send", w.document())
                    .booleanValue("sent", false);
            if (!sent) {
                throw new Exception(
                        "Failed to sending dicom data to " + ops.calledAET);
            }
        }
    }

    private static void findAndPush(Connection cxn, Options ops)
            throws Throwable {
        Vector<String> seriesAssets = new Vector<String>();
        //
        String query = nameSpaceQuery();

        // If there is a patient name query do that first and then find the
        // Series
        XmlStringWriter w = new XmlStringWriter();
        if (ops.lastName != null) {
            // Find patient DICOM assets
            String lastNameQuery = "xpath(mf-dicom-patient/name[@type='last'])=ignore-case('"
                    + ops.lastName + "')";

            query += " and " + lastNameQuery;
            if (ops.firstName != null) {
                String firstNameQuery = "xpath(mf-dicom-patient/name[@type='first'])=ignore-case('"
                        + ops.firstName + "')";
                query += " and " + firstNameQuery;
            }
            w.add("where", query);
            XmlDoc.Element r = cxn.execute("asset.query", w.document());
            Collection<String> patientAssets = r.values("id");

            // We have patient assets, find their Series
            for (String patientAsset : patientAssets) {
                if (ops.list) {
                    System.out.println("Patient asset id = " + patientAsset);
                }
                addSeries(cxn, ops, patientAsset, seriesAssets);
            }
        } else {
            // No patient just query directly on Series attributes
            query += " and mf-dicom-series has value";
            query = addExtraQueries(query, ops);
            w.add("where", query);
            w.add("size", "infinity");
            XmlDoc.Element r = cxn.execute("asset.query", w.document());
            if (r != null) {
                Collection<String> seriesIDs = r.values("id");
                if (seriesIDs != null && seriesIDs.size() > 0) {
                    seriesAssets.addAll(seriesIDs);
                    if (ops.list) {
                        for (String seriesAsset : seriesAssets) {
                            System.out.println(
                                    "Series asset id = " + seriesAsset);
                        }
                    }
                }
            }
        }
        //
        if (!ops.list) {
            if (seriesAssets.size() == 0) {
                System.out.println("No series assets were found");
                return;
            }

            System.out.println("Sending " + seriesAssets.size() + " series");
            w = new XmlStringWriter();
            w.push("called-ae");
            w.add("title", ops.calledAET);
            w.add("host", ops.host);
            w.add("port", ops.port);
            w.pop();
            w.push("calling-ae");
            w.add("aet", ops.callingAET);
            w.pop();
            for (String seriesAsset : seriesAssets) {
                w.add("id", seriesAsset);
            }
            boolean sent = cxn.execute("daris.dicom.send", w.document())
                    .booleanValue("sent", false);
            if (!sent) {
                throw new Exception(
                        "Failed to sending dicom data to " + ops.calledAET);
            }
        }
    }

    private static void addSeries(Connection cxn, Options ops,
            String patientAsset, Collection<String> seriesAssets)
                    throws Throwable {

        // Find the Studies for this patient
        XmlStringWriter w = new XmlStringWriter();
        String query = nameSpaceQuery()
                + " and (mf-dicom-study has value and (related to{had-by} (id="
                + patientAsset + ")))";
        w.add("where", query);
        w.add("size", "infinity");
        XmlDoc.Element r = cxn.execute("asset.query", w.document());
        if (r == null)
            return;
        //
        Collection<String> studyAssets = r.values("id");

        // Iterate thought Studies and find Series
        if (studyAssets.size() > 0) {
            for (String studyAsset : studyAssets) {
                if (ops.list) {
                    System.out.println("   Study asset id = " + studyAsset);
                }
                w = new XmlStringWriter();
                query = nameSpaceQuery()
                        + " and (mf-dicom-series has value and (related to{container} (id="
                        + studyAsset + ")))";

                // Add the extra series based selection criteria
                query = addExtraQueries(query, ops);
                w.add("where", query);
                w.add("size", "infinity");
                r = cxn.execute("asset.query", w.document());
                if (r != null) {
                    Collection<String> seriesIDs = r.values("id");
                    if (seriesIDs != null && seriesIDs.size() > 0) {
                        seriesAssets.addAll(seriesIDs);
                        if (ops.list) {
                            for (String seriesID : seriesIDs) {
                                System.out.println(
                                        "      Series asset id = " + seriesID);
                            }
                        }
                    }
                }
            }
        }
    }

    private static String nameSpaceQuery() {
        return "namespace='" + MF_NAMESPACE + "'";
    }

    private static String addExtraQueries(String query, Options ops) {

        // Add date query
        if (ops.startDate != null) {
            query += " and xpath(mf-dicom-series/sdate)>='" + ops.startDate
                    + "'";
        }
        if (ops.endDate != null) {
            query += " and xpath(mf-dicom-series/sdate)<='" + ops.endDate + "'";
        }
        if (ops.modality != null) {
            query += " and  xpath(mf-dicom-series/modality)='" + ops.modality
                    + "'";
        }
        if (ops.description != null) {
            query += " and xpath(mf-dicom-series/description) contains literal ('"
                    + ops.description + "')";
        }
        if (ops.protocol != null) {
            query += " and xpath(mf-dicom-series/protocol) contains literal ('"
                    + ops.protocol + "')";
        }
        return query;
    }

    /**
     * 
     * prints the help information for this command line tool.
     * 
     * @param os
     * 
     */
    private static void printHelp(PrintStream os) {
        os.println("MBCPETDICOMRetrieve");
        os.println();
        os.println("Synopsis:");
        os.println(
                "   Finds and pushes DICOM data from Mediaflux to the give AE.  ");
        os.println();
        os.println("Usage:");
        os.println(
                "   " + MBCPETCTDICOMRetrieve.class.getName() + " [options..]");
        os.println();
        os.println();
        os.println("Java Properties:");
        os.println(
                "    -mf.host      [Required]: The name or IP address of the Mediaflux host.");
        os.println("    -mf.port      [Required]: The server port number.");
        os.println("    -mf.domain    [Required]: The logon domain.");
        os.println("    -mf.user      [Required]: The logon user.");
        os.println(
                "    -mf.password  [Required]: The logon user's obfuscated password.");
        os.println(
                "    -mf.transport [Optional]: Required if the port number is non-standard.");
        os.println(
                "                              One of [HTTP, HTTPS, TCPIP]. By default the");
        os.println(
                "                              following transports are inferred from the port:");
        os.println("                                80    = HTTP");
        os.println("                                443   = HTTPS");
        os.println("                                other = TCPIP");
        os.println();
        os.println("Options:");
        os.println("   " + HELP_ARG
                + "          Displays this help. <src-path> not required in when");
        os.println("                    requesting help.");
        os.println("   " + LIST_ARG
                + "          Just lists found assets rather than downloading them");
        os.println("   " + FIRSTNAME_ARG
                + "         Specify patient first name (case insensitive; optional)");
        os.println("   " + LASTNAME_ARG
                + "          Specify patient last name (case insensitive; optional)");
        os.println("   " + STARTDATE_ARG
                + "         Specify start date (dd-MMM-yyyy; inclusive; one date required)");
        os.println("   " + ENDDATE_ARG
                + "           Specify end date (dd-MMM-yyyy; inclusive; one date required)");
        os.println("   " + MODALITY_ARG
                + "      Specify modality ('PT' or 'CT'; optional)");
        os.println("   " + DESCRIPTION_ARG
                + "          Specify description of series (free text; optional; partial matches allowed)");
        os.println("   " + PROTOCOL_ARG
                + "      Specify protocol of series (free text; optional; partial matches allowed)");
        os.println("   " + PATIENT_ASSET_ARG
                + "       Specify Mediaflux Patient asset ID (all children of patient will be pushed)");
        os.println("   " + STUDY_ASSET_ARG
                + "         Specify Mediaflux Study asset ID (all children of patient will be pushed)");
        os.println("   " + SERIES_ASSET_ARG
                + "        Specify Mediaflux Series asset ID");
        os.println("   " + CALLING_AET_ARG
                + "    AE Title of client. Defaults to 'UM-DaRIS-1'");
        os.println("   " + CALLED_AET_ARG
                + "     AE Title of destination DICOM server. Defaults to 'MBIC-PET-CONSOLE'");
        os.println("   " + HOST_ARG
                + "          Host IP of destination DICOM server. Defaults to '172.30.31.75'");
        os.println("   " + PORT_ARG
                + "          Port  of destination DICOM server. Defaults to '104'");
        os.println("   " + DECRYPT_ARG
                + "    Specifies the password should not be decrypted.");
        os.println();
        os.println();
    }
}
