package daris.client.model.user.messages;

import arc.mf.object.ObjectMessageResponse;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;

public class Self {

    public static void canRead(DObjectRef o, ObjectMessageResponse<Boolean> rh) {
        isProjectGuest(o.id(), rh);
    }

    public static void canWrite(DObjectRef o, ObjectMessageResponse<Boolean> rh) {
        if (o.isRepository()) {
            rh.responded(false);
        } else if (o.isProject()) {
            isProjectAdmin(o.id(), rh);
        } else {
            isProjectSubjectAdmin(o.id(), rh);
        }
    }

    public static void haveRole(String role, ObjectMessageResponse<Boolean> rh) {
        new ActorSelfHaveRole(role).send(rh);
    }

    public static void isProjectGuest(String id, ObjectMessageResponse<Boolean> rh) {
        haveRole(Project.guestRoleFromeId(id), rh);
    }

    public static void isProjectMember(String id, ObjectMessageResponse<Boolean> rh) {
        haveRole(Project.memberRoleFromeId(id), rh);
    }

    public static void isProjectSubjectAdmin(String id, ObjectMessageResponse<Boolean> rh) {
        haveRole(Project.subjectAdminRoleFromId(id), rh);
    }

    public static void isProjectAdmin(String id, ObjectMessageResponse<Boolean> rh) {
        haveRole(Project.adminRoleFromId(id), rh);
    }
}
