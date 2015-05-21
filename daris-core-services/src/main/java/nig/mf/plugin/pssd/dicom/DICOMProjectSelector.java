package nig.mf.plugin.pssd.dicom;

import java.util.*;

import nig.mf.pssd.CiteableIdUtil;


/**
 * This class takes the string from the DICOM control nig.dicom.project.selector and parses it
 * into a map for consumption by the DICOM server.
 * 
 * The map has key = DICOM proxy user
 *             value = array of Project CIDs that user may access
 * @author nebk
 *
 */

public class DICOMProjectSelector {

	// key=user
	// value = array of CIDs
	private HashMap<String,String[]> _selector = null;
	private Boolean _configured = false;           // True if the dicom ingest control 'nig.dicom.project.selector' was configured in the server

	/**
	 * COnstructor.
	 * 
	 * @param ic
	 * @throws Throwable
	 */
	public DICOMProjectSelector(DicomIngestControls ic)  throws Throwable {
		_selector = new HashMap<String,String[]>();
		if (ic.projectSelector()!=null) {
			_configured = true;
			parse(ic.projectSelector());
		}
	}

	/**
	 * Return the hash map. Will be empty if the control was not configured.
	 * 
	 * @return
	 */
	public HashMap<String,String[]> getMap () {
		return _selector;
	}

	/**
	 * If the dicom ingest controls configured 'nig.dicom.project.selector'  then returns true
	 * 
	 * @return
	 */
	public Boolean wasConfigured () {
		return _configured;
	}

	/**
	 * Return the array of project cids for this DICOM proxy user
	 * 
	 * @param user
	 * @return
	 */
	public String[] getProjects (String user) {
		return  _selector.get(user);
	}

	/**
	 * Establish if the given Project CID was included in the ingest control configuration
	 * for this user.  If the user is not specified in the control, then they can access
	 * all projects.  
	 * 
	 * @param user should hold domain:user
	 * @param projectId
	 * @return
	 * @throws Throwable
	 */
	public Boolean allowAccessToProject (String user, String projectId) throws Throwable {
		// If the dicom control is not configured, allow access to all projects
		if (!_configured) return true;

		//
		if (_selector.containsKey(user)) {
			String[] cids = _selector.get(user);
			for (int i=0; i<cids.length; i++) {
				if (projectId.equals(cids[i])) {
					return true;
				}
			}
			return false;
		} else {

			// User is not contained as a key so can access any project
			return true;
		}
	}

	public void print () {
		String t = toString();
		if (t!=null) {
			System.out.println(t);;
		}
	}

	public String toString () {
		if (!_configured) return null;
		//
		Iterator<String> it = (Iterator<String>)_selector.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String[] cids = _selector.get(key);
			return "User = '" + key + "' may access projects: " + java.util.Arrays.toString(cids);
		}
		return null;
	}


	private void parse (String projectSelector) throws Throwable {

		// The pattern is <domain:user>/<cid>,<cid>,...<cid>;<domain:user>/<cid>,<cid>,...,>cid>  etc

		// Find number of users
		if (projectSelector==null || projectSelector.isEmpty() || projectSelector=="") {
			throw new Exception ("The dicom control 'nig.dicom.project.selector' was mis-configured empty.");

		}
		String[] t1 = projectSelector.split(";");
		if (t1.length==0) {
			// Really can't happen as the input is non-empty
			throw new Exception ("The dicom control 'nig.dicom.project.selector' was mis-configured empty.");
		}
		
		// Itewrate through user/cid groupings
		for (int i=0; i<t1.length; i++) {

			// Split into user and cids
			String[] t2 = t1[i].split("/");
			if (t2.length==2) {

				// FInd number of cids for this user.
				String user = t2[0];
				String[] cids = t2[1].split(",");
				if (cids.length>0) {
					for (int j=0; j<cids.length; j++) {
						int depth = CiteableIdUtil.getIdDepth(cids[j]);
						if (depth==3) {
							// Locate in <user>,<cids>  HashMap
							_selector.put(user,cids);
						} else {
							// Perhaps just a log entry, we don't really want to stop a DICOM upload for this, or do we...
							throw new Exception ("Citeable ID " + cids[j] + ", extracted from dicom control 'nig.dicom.project.selector' does not represent a project");
						}
					}
				} else {
					throw new Exception ("User  " + user + ", extracted from dicom control 'nig.dicom.project.selector' was mis-configured with no project CIDs.");
				}
			} else {
				throw new Exception ("The dicom control 'nig.dicom.project.selector' was mis-configured; user with no CIDs?");
			}
		}
	}

}
