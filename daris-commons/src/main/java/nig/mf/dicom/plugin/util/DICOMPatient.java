package nig.mf.dicom.plugin.util;

import java.util.Collection;
import java.util.Date;

import nig.mf.pssd.CiteableIdUtil;
import nig.util.DateUtil;
import arc.mf.plugin.dicom.DicomPersonName;
import arc.xml.XmlDoc;

/**
 * Class to hold a few patient related details from the mf-dicom-patient
 * document
 * 
 * Leading and trailing spaces are pulled off strings (e.g. name fields) before
 * being stored here
 * 
 * @author nebk
 *
 */
public class DICOMPatient {

    private String firstName;
    private String lastName;
    private String fullName;
    private String sex;
    private Date dob;
    private String id;

    public DICOMPatient(XmlDoc.Element r) throws Throwable {
        if (r != null)
            create(r);
    }

    public String getFirstName() {
        return firstName;
    };

    public String getLastName() {
        return lastName;
    };

    public String getFullName() {
        return fullName;
    };

    public String getSex() {
        return sex;
    };

    public Date getDOB() {
        return dob;
    };

    public String getID() {
        return id;
    };

    /**
     * Constructs last^first as best it can
     * 
     * @return
     */
    public String nameForDICOMFile() {
        if (firstName != null && lastName != null) {
            return lastName + "^" + firstName;
        } else if (lastName != null) {
            return lastName;
        } else if (firstName != null) {
            return "^" + firstName;
        } else {
            return null;
        }
    }

    public String toString() {
        String t = "Name = " + firstName + " " + lastName + "\n" + "Sex  = "
                + sex + "\n" + "DOB  = " + dob + "\n" + "ID   = " + id;
        return t;
    }

    public Boolean hasBothNames() {
        return (firstName != null && lastName != null);
    }

    /**
     * Need at least lastName to match First name is optional (can be null in
     * both)
     * 
     * @param findSubectMethod
     *            'id', 'name', 'name+' (name + sex + dob), or 'all' (id, name,
     *            dob, sex)
     * @param oldPatientMeta
     * @param newFirstName
     * @param newLastName
     * @param newSex
     * @param newDateOfBirth
     * @param newID
     * @return
     * @throws Throwable
     */
    public static boolean matchDICOMDetail(String findSubjectMethod,
            XmlDoc.Element oldPatientMeta, DicomPersonName newName,
            String newSex, Date newDOB, String newID) throws Throwable {
        if (oldPatientMeta == null)
            return false;

        if (findSubjectMethod.equalsIgnoreCase("id")) {
            return stringsMatch(oldPatientMeta.value("id"), newID, true);
        } else if (findSubjectMethod.equalsIgnoreCase("name")) {
            return dicomNamesMatch(newName, oldPatientMeta);
        } else if (findSubjectMethod.equalsIgnoreCase("name+")) {
            if (!dicomNamesMatch(newName, oldPatientMeta))
                return false;
            if (!stringsMatch(oldPatientMeta.value("sex"), newSex, true))
                return false;
            if (!dicomDOBMatch(oldPatientMeta.dateValue("dob"), newDOB))
                return false;
            return true;
        } else if (findSubjectMethod.equalsIgnoreCase("all")) {
            if (!stringsMatch(oldPatientMeta.value("id"), newID, true))
                return false;
            if (!dicomNamesMatch(newName, oldPatientMeta))
                return false;
            if (!stringsMatch(oldPatientMeta.value("sex"), newSex, true))
                return false;
            if (!dicomDOBMatch(oldPatientMeta.dateValue("dob"), newDOB))
                return false;
            return true;
        }
        return false;
    }

    public static Boolean dicomDOBMatch(Date oldDOB, Date newDOB)
            throws Throwable {
        if (oldDOB == null && newDOB == null) {
            // Both null is considered a match
            return true;
        } else {
            if (oldDOB != null && newDOB != null) {
                return DateUtil.areDatesOfBirthEqual(oldDOB, newDOB);
            }
        }
        return false;
    }

    public static boolean stringsMatch(String oldStr, String newStr,
            boolean ignoreCase) {
        if (oldStr == null && newStr == null) {
            // If they are both null, then we can't say whether the
            // value is the same or not and we consider this a match.
            return true;
        } else {
            if (oldStr != null && newStr != null) {
                if (ignoreCase) {
                    if (oldStr.equalsIgnoreCase(newStr)) {
                        return true;
                    }
                } else {
                    if (oldStr.equals(newStr)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Boolean dicomNamesMatch(DicomPersonName newName,
            XmlDoc.Element oldPatientMeta) throws Throwable {
        // COnstruct fullnames and compare.
        // If there are CIDs in the name, treat them as null as we don't
        // save/compare CIDs.
        String newFullName = null;
        if (newName != null) {
            newFullName = makeFullName(newName.first(), newName.last());
            if (CiteableIdUtil.isCiteableId(newFullName))
                newFullName = null;
        }

        //
        String oldFullName = null;
        if (oldPatientMeta != null) {
            String oldLastName = oldPatientMeta.value("name[@type='last']");
            String oldFirstName = oldPatientMeta.value("name[@type='first']");
            oldFullName = makeFullName(oldFirstName, oldLastName);
            if (CiteableIdUtil.isCiteableId(oldFullName))
                oldFullName = null;

        }

        //
        return stringsMatch(newFullName, oldFullName, true);
    }

    private static String makeFullName(String first, String last) {
        String full = null;
        if (first != null && last != null) {
            full = first + " " + last;
        } else if (last != null) {
            full = last;
        } else if (first != null) {
            full = first;
        }
        return full;
    }

    private void create(XmlDoc.Element r) throws Throwable {
        Collection<XmlDoc.Element> names = r.elements("name");
        if (names != null && names.size() > 0) {
            for (XmlDoc.Element name : names) {
                String type = name.value("@type");
                if (type.equals("first")) {
                    firstName = name.value().trim();
                } else if (type.equals("last")) {
                    lastName = name.value().trim();
                } else if (type.equals("full")) {
                    fullName = name.value().trim();
                }
            }

            // Construct a full name if not native
            if (fullName == null) {
                if (firstName != null)
                    fullName = firstName;
                if (lastName != null) {
                    if (fullName == null) {
                        fullName = lastName;
                    } else {
                        fullName += " " + lastName;
                    }
                }
            }
            //
            dob = r.dateValue("dob");
            String sex = r.value("sex");
            if (sex != null) {
                sex = sex.trim();
            }
            id = r.value("id");
        }
    }

}