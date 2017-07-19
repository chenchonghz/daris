package nig.mf.plugin.pssd.dicom;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import arc.mf.plugin.dicom.DicomAssetEngine;
import arc.mf.plugin.dicom.DicomAssetEngineFactory;

/**
 * This class is used by the Mediaflux DICOM engine framework
 * 
 * @author jason
 *
 */
public class NIGDicomAssetEngineFactory implements DicomAssetEngineFactory {

    public static final String DESCRIPTION = "Handles PSSD data - stores data in an ex-method and study for a project and subject. Calls 'pss' engine (if configured) to handle PSS and non-citeable DICOM data.";

    public Map<String, String> arguments() {
        Map<String, String> args = new TreeMap<String, String>();

        // The control nig.dicom.id.citable; however, it's purpose is to specify
        // the destination CID and so
        // it's not appropriate to put it in the DICOM server controls. It can
        // be supplied directly to the dicom.ingest
        // service which in turn calls the StudyProxyFactory

        args.put("nig.dicom.id.by",
                "The method of identifying studies using P.S[.EM[.S]] (project, subject, ex-method, study) notation. If specified, one of [patient.id, patient.name, patient.name.first, patient.name.last, study.id, performing.physician.name, referring.physician.name, referring.physician.phone, requesting.physician].");
        args.put("nig.dicom.id.ignore-non-digits",
                "Specifies whether non-digits in part of an element should be ignored when constructing a P.S.EM.S identifier. One of [false,true]. Defaults to false.");
        args.put("nig.dicom.id.ignore-before-last-delim",
                "Specifies whether all characters before (and including) the last occurrence of the given delimiter should be ignored  when constructing a P.S.EM.S identifier. Invoked before ignore-non-digits if both supplied. Invoked after nig.dicom.id.ignore-after-last-delim");
        args.put("nig.dicom.id.ignore-after-last-delim",
                "Specifies whether all characters after (and including) the last occurrence of the given delimiter should be ignored  when constructing a P.S.EM.S identifier. Invoked before ignore-non-digits if both supplied. Invoked before nig.dicom.id.ignore-before-last-delim");
        args.put("nig.dicom.id.prefix",
                "If specified, the value to be prepended to any P.S.EM.S identifier.");
        args.put("nig.dicom.subject.find.method",
                "Specifies how the server tries to find Subjects if the CID is for a Project. Allowed values are 'id',name', 'name+', 'all'. 'id' means mf-dicom-patient/id, 'name' means the full name from mf-dicom-patient/name (first and last), 'name+' means match on name,sex and DOB, 'all' means match on id,name,sex and DOB.  The default if not specified is 'name'.");
        args.put("nig.dicom.subject.create",
                "If true, will auto-create Subjects if the identifier is of the form P.S and the Subject does not exist.");
        args.put("nig.dicom.subject.clone_first",
                "If auto-creating subjects, make new ones by cloning the first one if it exists. If it does not exist, just generate new subject as per normal");
        args.put("nig.dicom.subject.name.from.last",
                "When auto-creating Subjects, set the Subject name to the DICOM patient last name");
        args.put("nig.dicom.subject.name.from.first",
                "When auto-creating Subjects, set the Subject name to the DICOM patient first name");
        args.put("nig.dicom.subject.name.from.full",
                "When auto-creating Subjects, set the Subject name to the DICOM patient full name");
        args.put("nig.dicom.subject.name.from.id",
                "When auto-creating Subjects, name the Subject from the DICOM PatientID.");
        args.put("nig.dicom.subject.name.from.index.range",
                "When auto-creating Subjects, and utilising a control nig.dicom.subject.name.from.{full,last,full,id} to set the Subject name, select characters from the given index range pair. E.g. 0,11 would select the String starting at index 0 and finishing at index 11.  This control is applied before control nig.dicom.subject.name.from.ignore-after-last-delim");
        args.put("nig.dicom.subject.name.from.ignore-after-last-delim",
                "When auto-creating Subjects, and utilising a control nig.dicom.subject.name.from.{full,last,full,id} to set the SUbject name, ignore all characters after and including the last occurrence of the specified delimiter.");
        args.put("nig.dicom.subject.meta.set-service",
                "Service to populate domain-specific meta-data on Subject objects.");
        args.put("nig.dicom.write.mf-dicom-patient",
                "Instructs the server to populate document mf-dicom-patient (or possibly mf-dicom-patient-encrypted - see nig.dicom.use.encrypted.patient)  on all Subjects (sets in private meta-data)");
        args.put("nig.dicom.encrypt.patient.metadata",
                "Instructs the server to populate document mf-dicom-patient-encrypted rather than mf-dicom-patient (if it is being written according to nig.dicom.write.mf-dicom-patient");
        args.put("nig.dicom.modality.ignore",
                "Ignore the DICOM modality in the data and Method and just create the next Study under the next available empty Method step");
        args.put("nig.dicom.dose-reports.drop",
                "Drop Structured dose reports (modality SR) if the Method specifies is not for humans (see service om.pssd.method.for.subject.create and document daris:pssd-method-subject). Repors will not be dropped for human or unspecified Methods.");
        args.put("nig.dicom.project.selector",
                "Specifies Projects that specific proxy DICOM users are allowed to write to.");
        return args;
    }

    public Object createConfiguration(Map<String, String> args)
            throws Throwable {
        DicomIngestControls ic = new DicomIngestControls();
        if (args == null) {
            args = new HashMap<String, String>();
        }

        ic.configure(args);
        return ic;
    }

    public String description() {
        return DESCRIPTION;
    }

    public DicomAssetEngine instantate() {
        return new NIGDicomAssetEngine();
    }

    public String type() {
        return NIGDicomAssetEngine.TYPE_NAME;
    }
}
