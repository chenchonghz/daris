package nig.mf.pssd;

/**
 * Generic DaRIS/PSSD Roles
 * 
 * @author nebk
 *
 */
public class Role {
	
	/**
	 * User has access to the model.
	 */
	public static final String MODEL_USER_ROLE_NAME      = "model-user";
	
	/**
	 * User has some extra powers; doc type and dictionary management
	 */
	public static final String POWER_MODEL_USER_ROLE_NAME = "power-user";
	
	/**
	 * User can create projects.
	 */
	public static final String PROJECT_CREATOR_ROLE_NAME = "project-creator";
	
	/**
	 * User can create r-subjects.
	 */
	public static final String SUBJECT_CREATOR_ROLE_NAME = "subject-creator";
	
	/**
	 * User has admin access to all objects
	 * @return
	 */
	public static final String OBJECT_ADMIN_ROLE_NAME = "object-admin";
	
	
	
	public static String modelUserRoleName() {
		return "daris:pssd.model.user";
	}
	
	public static String powerModelUserRoleName() {
		return "daris:pssd.model.power.user";
	}
	
	public static String projectCreatorRoleName() {
		return "daris:pssd.project.create";
	}
	
	public static String subjectCreatorRoleName() {
		return "daris:pssd.subject.create";
	}
	
	public static String objectAdminRoleName() {
		return "daris:pssd.object.admin";
	}    
	
	
	public static  String objectGuestRoleName () {
		return  "daris:pssd.object.guest";
	}
	
	public static String rSubjectAdminRoleName () {
		return  "daris:pssd.r-subject.admin";
	}
	
	public static String rSubjectGuestRoleName() {
		return "daris:pssd.r-subject.guest";
	}
}
