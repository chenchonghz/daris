package daris.client.ui;

import arc.gui.object.register.SystemObjectGUIRegistry;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.dti.file.DTIFile;
import arc.mf.client.dti.task.DTITask;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.model.asset.document.MetadataNamespaceRef;
import arc.mf.model.authentication.UserRef;
import arc.mf.model.dictionary.Term;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.ProjectMember;
import daris.client.model.project.ProjectRoleMember;
import daris.client.model.query.filter.Filter;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.transform.TransformRef;
import daris.client.model.user.RoleUser;
import daris.client.ui.dictionary.TermGUI;
import daris.client.ui.doc.MetadataDocumentGUI;
import daris.client.ui.doc.MetadataNamespaceGUI;
import daris.client.ui.dti.DTITaskGUI;
import daris.client.ui.dti.file.LocalFileGUI;
import daris.client.ui.object.DObjectGUI;
import daris.client.ui.project.ProjectMemberGUI;
import daris.client.ui.project.ProjectRoleMemberGUI;
import daris.client.ui.query.filter.FilterGUI;
import daris.client.ui.sc.ContentItemGUI;
import daris.client.ui.sc.ShoppingCartGUI;
import daris.client.ui.transform.TransformGUI;
import daris.client.ui.user.RoleUserGUI;
import daris.client.ui.user.UserGUI;

public class DObjectGUIRegistry {

	private static boolean _registered = false;

	public static SystemObjectGUIRegistry get() {

		SystemObjectGUIRegistry registry = SystemObjectGUIRegistry.get();
		if (!_registered) {
			registry.add(DObjectRef.class, DObjectGUI.INSTANCE);
			registry.add(UserRef.class, UserGUI.INSTANCE);
			registry.add(RoleUser.class, RoleUserGUI.INSTANCE);
			registry.add(ProjectMember.class, ProjectMemberGUI.INSTANCE);
			registry.add(ProjectRoleMember.class, ProjectRoleMemberGUI.INSTANCE);
			registry.add(DTIFile.class, LocalFileGUI.INSTANCE);
			registry.add(DTIDirectory.class, LocalFileGUI.INSTANCE);
			registry.add(ShoppingCartRef.class, ShoppingCartGUI.INSANCE);
			registry.add(ContentItem.class, ContentItemGUI.INSANCE);
			registry.add(DTITask.class, DTITaskGUI.INSTANCE);
			registry.add(MetadataDocumentRef.class, MetadataDocumentGUI.INSTANCE);
			registry.add(MetadataNamespaceRef.class, MetadataNamespaceGUI.INSTANCE);
			registry.add(Filter.class, FilterGUI.INSTANCE);
			registry.add(Term.class, TermGUI.INSTANCE);
			registry.add(TransformRef.class, TransformGUI.INSTANCE);
			_registered = true;
		}
		return registry;
	}
}
