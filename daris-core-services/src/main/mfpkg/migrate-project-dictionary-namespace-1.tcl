foreach project_cid [xvalues cid [asset.query :where "model='om.pssd.project'" :action get-cid :size infinity]] {
    dictionary.namespace.create :ifexists ignore :namespace "daris-project-${project_cid}" :description "Project specific dictionary namespace for DaRIS project ${project_cid}."
    actor.grant :type role :name daris:pssd.project.admin.${project_cid} :perm < :resource -type dictionary:namespace daris-project-${project_cid} :access ADMINISTER >
    actor.grant :type role :name daris:pssd.project.member.${project_cid} :perm < :resource -type dictionary:namespace daris-project-${project_cid} :access MODIFY >
    actor.grant :type role :name daris:pssd.project.guest.${project_cid} :perm < :resource -type dictionary:namespace daris-project-${project_cid} :access ACCESS >
}