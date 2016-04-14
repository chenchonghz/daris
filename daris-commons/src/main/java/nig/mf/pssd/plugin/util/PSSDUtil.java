package nig.mf.pssd.plugin.util;

import java.util.Collection;
import java.util.Iterator;

import nig.iio.metadata.SubjectMethodMetadata;
import nig.mf.Executor;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.plugin.util.PluginExecutor;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class PSSDUtil {

	public static final String PROJECT_MODEL = "om.pssd.project";
	public static final String SUBJECT_MODEL = "om.pssd.subject";
	public static final String EX_METHOD_MODEL = "om.pssd.ex-method";
	public static final String STUDY_MODEL = "om.pssd.study";
	public static final String DATASET_MODEL = "om.pssd.dataset";
	public static final String DATA_OBJECT_MODEL = "om.pssd.dataobject";
	public static final String R_SUBJECT_MODEL = "om.pssd.r-subject";
	public static final String METHOD_MODEL = "om.pssd.method";
	public static final String MODEL_PREFIX = "om.pssd";



	public static boolean isValidPSSDObject(ServiceExecutor executor, String cid) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (model == null) return false;
		if (model.startsWith(MODEL_PREFIX)) return true;
		return false;
	}

	public static boolean isValidPSSDObject(ServiceExecutor executor, String cid, String model, Boolean throwIt) throws Throwable {

		Boolean ok = true;
		if (model == null) {
			ok = false;
		} else {
			if (!model.startsWith(MODEL_PREFIX)) ok = false;
		}
		if (ok) {
			return true;
		} else {
			if (throwIt) {
				throw new Exception("No or wrong asset/model found. Asset(cid=" + cid + ") is not a valid PSSD object.");
			} else {
				return false;
			}
		}
	}

	public static boolean isValidProject(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(PROJECT_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD Project asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static boolean isValidSubject(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(SUBJECT_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD Subject asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	
	
	public static boolean isValidExMethod(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(EX_METHOD_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD ExMethod asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	
	public static boolean isValidStudy(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(STUDY_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD Study asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static boolean isValidDataSet(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(DATASET_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD DataSet asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	
	public static boolean isValidDataObject(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(DATA_OBJECT_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD DataObject asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static boolean isValidMethod (ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(METHOD_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD Method asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public static boolean isValidRSubject(ServiceExecutor executor, String cid, boolean throwIt) throws Throwable {

		String model = AssetUtil.getModel(executor, cid, true);
		if (!isValidPSSDObject (executor, cid, model, throwIt)) return false;
		if (!model.equals(R_SUBJECT_MODEL)) {
			if (throwIt) {
				throw new Exception("Wrong asset/model. Asset(cid=" + cid + ", model=" + model
						+ ") is not a valid PSSD RSubject asset.");
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	


	/**
	 * Is the asset associated with this CID a replica ?
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */	
	public static Boolean isReplica (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", cid);
		XmlDoc.Element r = executor.execute("om.pssd.object.is.replica", doc.root());
		return r.booleanValue("replica");
	}

	/**
	 * Does this CID have children (primary or replica) on remote peers.
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static Boolean hasRemoteChildren (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", cid);
		doc.add("asset-type", "all");
		XmlDoc.Element r = executor.execute("om.pssd.object.has.remote.children", doc.root());
		return r.booleanValue("remote-children");
	}


	/**
	 * Check the given Document Type exists. Exception if not.
	 * 
	 * @param role - the role
	 * @param throwIt - if true throw an exception if it does not exist
	 * @return - true if exists, false if does not
	 * @throws Throwable
	 */
	public static boolean checkDocTypeExists(ServiceExecutor executor, String docType) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", docType);

		XmlDoc.Element r = executor.execute("asset.doc.type.exists", dm.root());
		return r.booleanValue("exists");
	}

	/**
	 * Returns the object type for the given object.
	 * 
	 * @param executor
	 * @param dCID DIstributed CID
	 * @return  Returns null if the asset (primary or replica) does not exist.
	 * @throws Throwable
	 */
	public static String typeOf(ServiceExecutor executor, DistributedAsset dCID)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", dCID.getCiteableID());

		ServerRoute route = dCID.getServerRouteObject();
		XmlDoc.Element r = executor.execute(route, "asset.exists", dm.root());
		if (!r.booleanValue("exists")) return null;

		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		r = executor.execute(route, "asset.get", dm.root());
		return r.stringValue("asset/meta/daris:pssd-object/type");
	}

	/**
	 * Returns the object type for the given local object.
	 * 
	 * @param executor
	 * @param cid  Distributed citeable ID
	 * @return  Returns null if the asset does not exist.
	 * @throws Throwable
	 */

	public static String typeOf(ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);

		XmlDoc.Element r = executor.execute("asset.exists", dm.root());
		if (!r.booleanValue("exists")) return null;

		dm.add("pdist",0);                 // Force local 		
		r = executor.execute("asset.get", dm.root());
		return r.stringValue("asset/meta/daris:pssd-object/type");
	}

	/**
	 * Establish if the caller holds the system-administrator role on the local server
	 * Exception if not
	 * 
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static void  isSystemAdministrator (ServiceExecutor executor) throws Throwable {
		if (!hasRole (null, executor, "system-administrator")) {
			throw new Exception ("You do not hold the system-administrator role.");
		}
	}

	/**
	 * Establish if the caller holds the system-administrator or the given role
	 * on the local server
	 * 
	 * Exception if not
	 * 
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static void  isSystemAdminORGiven (ServiceExecutor executor, String role) throws Throwable {
		if (!hasRole (null, executor, "system-administrator") &&
				!hasRole (null, executor, role)) {
			throw new Exception ("You do not hold the system-administrator " +
					" or the " + role + " role.");
		}
	}


	/**
	 * Establish if the calling user has the given role
	 * 
	 * @param route
	 * @param executor
	 * @param role
	 * @return
	 * @throws Throwable
	 */
	public static boolean hasRole(ServerRoute route, ServiceExecutor executor, String role) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, role);

		// When executed on a remote server, the authority that is used will be
		// numbered
		// explicitly by the peer. E.g. If this is called from 101 an executed
		// on
		// peer 1005 (peer 1005 actor.self.have) the authority used on peer 1005
		// will be 101
		// So it must be explicitly created there

		XmlDoc.Element r = executor.execute(route, "actor.self.have", dm.root());
		return r.booleanValue("role");
	}


	/**
	 * Function to try to auto-create a Subject. Subject CID can be supplied.  Method meta-data is set 
	 * on the Subject
	 * 
	 * @param pid  The citable ID of the parent Project
	 * @param sid  The desired citable ID of the SUbject. If null, the next one is allocated
	 * @param fillin If sid not specified, fillin allocator space
	 * @param mid   The citable ID of the Method to use. If null,  looks for first.
	 * @return  Subject and ExMethod CIDs
	 * @throws Throwable
	 */
	public static String[] createSubject (ServiceExecutor executor, String pid, String sid, 
			String mid, String name, Boolean fillIn) throws Throwable {

		// Get Methods
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", pid);
		if (mid==null) {
			XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
			Collection<XmlDoc.Element> methods = r.elements("object/method");

			// We can't proceed if there are no Methods or more than one (how could we choose ?)
			if (methods==null) {
				// There is nothing we can do but throw an exception
				throw new Exception ("There is no Method registered with the Project - cannot auto-create Subject");
			}
			if (methods.size()>1) {
				// There is nothing we can do but throw an exception
				throw new Exception ("There is more than 1  Method registered with the Project; cannot select for Subject auto-creation");
			}

			// Get the only Method CID
			Iterator<XmlDoc.Element> it = methods.iterator();
			XmlDoc.Element method = it.next();
			mid = method.value("id");
			if (mid==null) {
				// There is nothing we can do but throw an exception
				throw new Exception ("There is no Method registered with the Project - cannot auto-create Subject");
			}
		}


		// If the Subject CID has been supplied but not allocated, import it.  
		String subjectNumber = null;
		if (sid!=null) {
			DistributedAsset dID = new DistributedAsset (null, sid);
			if (!nig.mf.pssd.plugin.util.CiteableIdUtil.cidExists(dID.getServerRoute(),
					executor, dID.getCiteableID())) {
				nig.mf.pssd.plugin.util.CiteableIdUtil.importCid(executor, sid, 1);
			}
			subjectNumber = nig.mf.pssd.CiteableIdUtil.getLastSection(sid);
		}

		// We already know the asset does not exist so now we can try to create it
		dm = new XmlDocMaker("args");
		dm.add("pid",pid);
		if (subjectNumber!=null) {
			dm.add("subject-number", subjectNumber);
		} else {
			if (fillIn) dm.add("fillin", true);
		}
		if (name!=null) dm.add("name", name);
		dm.add("method", mid);
		dm.add("allow-incomplete-meta", true);     // The Method may not supply all meta-data

		// Set the meta-data pre-specified by the Method. Use the executor framework that combines 
		// Plugins and CLients as this code is shared with the Bruker client
		Executor pExecutor = new PluginExecutor(executor);
		SubjectMethodMetadata.addSubjectMethodMeta (pExecutor, mid, dm);

		// If it fails we will catch (outside this function) and send a message to the admin
		XmlDoc.Element r = executor.execute("om.pssd.subject.create", dm.root());
		String[] ids = new String[2];
		ids[0] = r.value("id");
		ids[1] = r.value("id/@mid");
		return ids;
	}
}
