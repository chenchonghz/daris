package daris.client.ui.project;

import java.util.List;
import java.util.Vector;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.FieldSet;
import arc.gui.form.FieldSetListener;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.dnd.DragResponder;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.dnd.DropTarget;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.menu.ActionContextMenu;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.util.Action;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ContextMenuEvent;

import daris.client.model.method.MethodEnum;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.CidRootNameEnumerationDataSource;
import daris.client.model.project.DataUse;
import daris.client.model.project.Project;
import daris.client.model.project.ProjectMember;
import daris.client.model.project.ProjectNamespaceEnumerationDataSource;
import daris.client.model.project.ProjectRole;
import daris.client.model.project.ProjectRoleMember;
import daris.client.model.repository.RepositoryRef;
import daris.client.model.user.RoleUser;
import daris.client.model.user.User;
import daris.client.ui.object.DObjectDetails;
import daris.client.ui.user.RoleUserGrid;
import daris.client.ui.user.UserGrid;

public class ProjectDetails extends DObjectDetails {

    public static final String TAB_NAME_MEMBER = "Members";
    public static final String TAB_DESC_MEMBER = "Members";
    public static final String TAB_NAME_ROLE_MEMBER = "Role Members";
    public static final String TAB_DESC_ROLE_MEMBER = "Role Members";

    private ProjectMemberGrid _memberGrid;

    private ProjectRoleMemberGrid _roleMemberGrid;

    public ProjectDetails(Project o, FormEditMode mode) {

        this(RepositoryRef.INSTANCE, o, mode);
    }

    protected ProjectDetails(DObjectRef po, Project o, FormEditMode mode) {

        super(po, o, mode);

        updateMemberTab();

        updataRoleMemberTab();

        setActiveTab();
    }

    @Override
    protected void insertToInterfaceTab(VerticalPanel vp) {

        /*
         * namespace We want this field to be non-editable once the Project has
         * been created and so the editability is set depending upon the mode of
         * action
         */
        // @formatter:off
//        if (mode() == FormEditMode.CREATE) {
//
//            final Project po = (Project) object();
//
//            HorizontalPanel nsHP = new HorizontalPanel();
//            nsHP.setPaddingRight(20);
//            nsHP.setPaddingLeft(20);
//            nsHP.setHeight(22);
//            Label nsLabel = new Label("namespace:");
//            nsLabel.setFontSize(11);
//            nsLabel.setFontWeight(FontWeight.BOLD);
//            nsLabel.setPaddingTop(8);
//            nsLabel.setHeight100();
//            nsHP.add(nsLabel);
//            nsHP.setSpacing(8);
//
//            TreeSelectComboBox<NamespaceRef> nsCombo = new TreeSelectComboBox<NamespaceRef>(new NamespaceRef(
//                    po.namespace() == null ? Project.DEFAULT_PROJECT_NAMESPACE : po.namespace()), new NamespaceTree(),
//                    true) {
//
//                @Override
//                protected String toString(NamespaceRef ns) {
//                    return ns.path();
//                }
//
//                @Override
//                protected boolean canSelect(Node n) {
//
//                    return n.object() != null && (n.object() instanceof NamespaceRef);
//                }
//
//                @Override
//                protected NamespaceRef transform(Node n) {
//                    return (NamespaceRef) n.object();
//                }
//            };
//            nsCombo.setWidth100();
//            nsCombo.addSelectionHandler(new SelectionHandler<NamespaceRef>() {
//
//                @Override
//                public void selected(NamespaceRef o) {
//                    po.setNamespace(o.path());
//                }
//            });
//            nsHP.add(nsCombo);
//
//            vp.add(nsHP);
//        }
        // @formatter:on
    }

    @Override
    protected void addToInterfaceForm(Form interfaceForm) {

        /*
         * namespace
         */
        final Project po = (Project) object();

        Field<String> namespaceField = new Field<String>(new FieldDefinition(
                "namespace",
                mode() == FormEditMode.CREATE ? new EnumerationType<String>(
                        new ProjectNamespaceEnumerationDataSource())
                        : ConstantType.DEFAULT,
                "The asset namespace of the project.", null, 1, 1));
        namespaceField.setValue(po.namespace(), false);
        if (mode() == FormEditMode.CREATE) {
            // can only set namespace when creating the project
            namespaceField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    po.setNamespace(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        Property property) {

                }
            });
        }
        interfaceForm.add(namespaceField);

        /*
         * cid-root-name
         */
        if (mode() == FormEditMode.CREATE) {
            Field<String> cidRootNameField = new Field<String>(
                    new FieldDefinition(
                            "cid-root-name",
                            new EnumerationType<String>(
                                    new CidRootNameEnumerationDataSource()),
                            "Specify the named citable ID root for the collection. Defaults to 'pssd.project'. Using other named roots allows projects to be created in a CID sandbox, perhaps for testing.",
                            null, 1, 1));
            cidRootNameField.setValue(po.cidRootName(), false);
            cidRootNameField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    po.setCidRootName(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        Property property) {

                }
            });
            interfaceForm.add(cidRootNameField);
        }

        super.addToInterfaceForm(interfaceForm);

        /*
         * methods
         */
        FieldGroup methodsFieldGroup = new FieldGroup(
                new FieldDefinition(
                        "Methods",
                        ConstantType.DEFAULT,
                        "Specifies the list of Methods that can be used with this project. Methods control Subject and Study meta-data.",
                        null, 1, 1));
        if (po.hasMethods()) {
            for (MethodRef m : po.methods()) {
                Field<MethodRef> methodField = new Field<MethodRef>(
                        new FieldDefinition(
                                "method",
                                new EnumerationType<MethodRef>(new MethodEnum()),
                                "method", null, 1, Integer.MAX_VALUE));
                FieldRenderOptions fro = new FieldRenderOptions();
                fro.setWidth(500);
                methodField.setRenderOptions(fro);
                methodField.setValue(m);
                methodsFieldGroup.add(methodField);
            }
        } else {
            if (mode() == FormEditMode.CREATE || mode() == FormEditMode.UPDATE) {
                Field<MethodRef> methodField = new Field<MethodRef>(
                        new FieldDefinition(
                                "method",
                                new EnumerationType<MethodRef>(new MethodEnum()),
                                "method", null, 1, Integer.MAX_VALUE));
                FieldRenderOptions fro = new FieldRenderOptions();
                fro.setWidth(500);
                methodField.setRenderOptions(fro);
                methodsFieldGroup.add(methodField);
            }
        }
        if (mode() != FormEditMode.READ_ONLY) {
            methodsFieldGroup.addListener(new FieldSetListener() {

                @SuppressWarnings("rawtypes")
                private List<MethodRef> getMethods(FieldSet fs) {

                    List<FormItem> items = fs.fields();
                    if (items != null) {
                        if (!items.isEmpty()) {
                            List<MethodRef> methods = new Vector<MethodRef>();
                            for (FormItem item : items) {
                                MethodRef m = (MethodRef) item.value();
                                if (m != null) {
                                    methods.add(m);
                                }
                            }
                            return methods;
                        }
                    }
                    return null;
                }

                @Override
                @SuppressWarnings("rawtypes")
                public void addedField(FieldSet s, FormItem f, int idx,
                        boolean lastUpdate) {

                    po.setMethods(getMethods(s));
                }

                @Override
                @SuppressWarnings("rawtypes")
                public void removedField(FieldSet s, FormItem f, int idx,
                        boolean lastUpdate) {

                    po.setMethods(getMethods(s));
                }

                @Override
                public void updatedFields(FieldSet s) {

                    po.setMethods(getMethods(s));
                }

                @Override
                @SuppressWarnings("rawtypes")
                public void updatedFieldValue(FieldSet s, FormItem f) {

                    po.setMethods(getMethods(s));
                }

                @Override
                @SuppressWarnings("rawtypes")
                public void updatedFieldState(FieldSet s, FormItem f,
                        FormItem.Property p) {

                }
            });
        }
        interfaceForm.add(methodsFieldGroup);

        /*
         * data-use
         */
        Field<DataUse> dataUseField = new Field<DataUse>(
                new FieldDefinition(
                        "data-use",
                        DataUse.asEnumerationType(),
                        "Specifies the type of consent for the use of data for this project: "
                                + " 1) 'specific' means use the data only for the original specific intent, "
                                + " 2) 'extended' means use the data for related projects and "
                                + " 3) 'unspecified' means use the data for any research",
                        null, 1, 1));
        dataUseField.setValue(po.dataUse());
        if (mode() != FormEditMode.READ_ONLY) {
            dataUseField.addListener(new FormItemListener<DataUse>() {

                @Override
                public void itemValueChanged(FormItem<DataUse> f) {

                    po.setDataUse(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<DataUse> f,
                        FormItem.Property p) {

                }
            });
        }
        interfaceForm.add(dataUseField);
    }

    private void updateMemberTab() {

        /*
         * member grid already created, just refresh then return.
         */
        if (_memberGrid != null) {
            _memberGrid.refresh();
            return;
        }

        /*
         * create member grid & tab
         */
        final Project po = (Project) object();
        _memberGrid = new ProjectMemberGrid(po, mode());
        _memberGrid.refresh();
        if (mode().equals(FormEditMode.READ_ONLY)) {
            setTab(TAB_NAME_MEMBER, TAB_DESC_MEMBER, _memberGrid);
        } else {
            addMustBeValid(_memberGrid);
            HorizontalSplitPanel hsp = new HorizontalSplitPanel(5);
            hsp.fitToParent();
            VerticalPanel vpl = new VerticalPanel();
            vpl.setPreferredWidth(0.6);
            vpl.setHeight100();
            hsp.add(vpl);
            VerticalPanel vpr = new VerticalPanel();
            vpr.fitToParent();
            hsp.add(vpr);
            setTab(TAB_NAME_MEMBER, TAB_DESC_MEMBER, hsp);
            CenteringPanel cpl = new CenteringPanel(Axis.HORIZONTAL);
            cpl.setHeight(22);
            cpl.setWidth100();
            Label membersLabel = new Label("Members");
            membersLabel.setPaddingTop(2);
            membersLabel.setFontSize(12);
            membersLabel.setFontWeight(FontWeight.BOLD);
            membersLabel.setHeight100();
            cpl.add(membersLabel);
            vpl.add(cpl);

            _memberGrid.fitToParent();
            vpl.add(_memberGrid);
            final UserGrid userGrid = new UserGrid();
            userGrid.setRowContextMenuHandler(new ListGridRowContextMenuHandler<User>() {

                @Override
                public void show(final User user, ContextMenuEvent event) {

                    final int x = _memberGrid.absoluteRight();
                    final int y = _memberGrid.absoluteTop()
                            + _memberGrid.height() / 2;
                    Menu menu = new Menu("User");
                    menu.setShowTitle(true);
                    menu.add(new ActionEntry("Add to members", new Action() {

                        @Override
                        public void execute() {

                            ProjectMemberRoleSelector
                                    .showAt(x,
                                            y,
                                            new ProjectMemberRoleSelector.RoleSelectionListener() {

                                                @Override
                                                public void roleSelected(
                                                        ProjectRole role,
                                                        DataUse dataUse) {

                                                    if (role != null) {
                                                        po.addMember(new ProjectMember(
                                                                user, role,
                                                                dataUse));
                                                        if (mode()
                                                                .equals(FormEditMode.UPDATE)) {
                                                            po.commitMembers(new ObjectMessageResponse<Boolean>() {

                                                                @Override
                                                                public void responded(
                                                                        Boolean r) {

                                                                    if (r) {
                                                                        _memberGrid
                                                                                .refresh();
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            _memberGrid
                                                                    .refresh();
                                                        }
                                                    }
                                                }
                                            });
                        }
                    }));
                    ActionContextMenu am = new ActionContextMenu(menu);
                    NativeEvent ne = event.getNativeEvent();
                    am.showAt(ne);
                }
            });
            userGrid.addDropTarget(new DropTarget() {

                @Override
                public BaseWidget widget() {

                    return userGrid;
                }

                @Override
                public DragResponder dragResponderAt(int x, int y,
                        EventTarget target) {

                    return this;
                }

                @Override
                public DropHandler dropHandler() {

                    return new DropHandler() {

                        @Override
                        public DropCheck checkCanDrop(Object data) {

                            if (po.members() == null) {
                                return DropCheck.CANNOT;
                            }
                            if (po.members().size() <= 1) {
                                return DropCheck.CANNOT;
                            }
                            if (data instanceof ProjectMember) {
                                return DropCheck.CAN;
                            }
                            return DropCheck.CANNOT;
                        }

                        @Override
                        public void drop(BaseWidget target, List<Object> data,
                                final DropListener dl) {

                            if (data == null) {
                                dl.dropped(DropCheck.CANNOT);
                                return;
                            }
                            boolean changed = false;
                            for (Object o : data) {
                                if (o instanceof ProjectMember) {
                                    po.removeMember((ProjectMember) o);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                if (mode().equals(FormEditMode.UPDATE)) {
                                    po.commitMembers(new ObjectMessageResponse<Boolean>() {

                                        @Override
                                        public void responded(Boolean r) {

                                            if (r) {
                                                _memberGrid.refresh();
                                                dl.dropped(DropCheck.CAN);
                                            }
                                        }
                                    });
                                } else {
                                    _memberGrid.refresh();
                                    dl.dropped(DropCheck.CAN);
                                }
                            } else {
                                dl.dropped(DropCheck.CANNOT);
                            }

                        }
                    };
                }
            });

            CenteringPanel cpr = new CenteringPanel(Axis.HORIZONTAL);
            cpr.setHeight(22);
            cpr.setWidth100();
            Label usersLabel = new Label("Available users");
            usersLabel.setFontSize(12);
            usersLabel.setFontWeight(FontWeight.BOLD);
            usersLabel.setHeight100();
            cpr.add(usersLabel);
            vpr.add(cpr);

            vpr.add(userGrid);
        }

    }

    private void updataRoleMemberTab() {

        /*
         * role-member grid already created, just refresh and return;
         */
        if (_roleMemberGrid != null) {
            _roleMemberGrid.refresh();
            return;
        }

        /*
         * create role-member tab & grid
         */
        final Project po = (Project) object();
        _roleMemberGrid = new ProjectRoleMemberGrid(po, mode());
        _roleMemberGrid.refresh();
        if (mode().equals(FormEditMode.READ_ONLY)) {
            setTab(TAB_NAME_ROLE_MEMBER, TAB_DESC_ROLE_MEMBER, _roleMemberGrid);
        } else {
            addMustBeValid(_roleMemberGrid);
            HorizontalSplitPanel hsp = new HorizontalSplitPanel(5);
            hsp.fitToParent();
            VerticalPanel vpl = new VerticalPanel();
            vpl.setPreferredWidth(0.6);
            vpl.setHeight100();
            hsp.add(vpl);
            VerticalPanel vpr = new VerticalPanel();
            vpr.fitToParent();
            hsp.add(vpr);
            setTab(TAB_NAME_ROLE_MEMBER, TAB_DESC_ROLE_MEMBER, hsp);
            CenteringPanel cpl = new CenteringPanel(Axis.HORIZONTAL);
            cpl.setHeight(22);
            cpl.setWidth100();
            Label roleMembersLabel = new Label("Role Members");
            roleMembersLabel.setPaddingTop(2);
            roleMembersLabel.setFontSize(12);
            roleMembersLabel.setFontWeight(FontWeight.BOLD);
            roleMembersLabel.setHeight100();
            cpl.add(roleMembersLabel);
            vpl.add(cpl);

            _roleMemberGrid.fitToParent();
            vpl.add(_roleMemberGrid);
            final RoleUserGrid roleUserGrid = new RoleUserGrid();
            roleUserGrid
                    .setRowContextMenuHandler(new ListGridRowContextMenuHandler<RoleUser>() {

                        @Override
                        public void show(final RoleUser roleUser,
                                ContextMenuEvent event) {

                            final int x = _roleMemberGrid.absoluteRight();
                            final int y = _roleMemberGrid.absoluteTop()
                                    + _roleMemberGrid.height() / 2;
                            Menu menu = new Menu("Role User");
                            menu.setShowTitle(true);
                            menu.add(new ActionEntry("Add to role members",
                                    new Action() {

                                        @Override
                                        public void execute() {

                                            ProjectMemberRoleSelector
                                                    .showAt(x,
                                                            y,
                                                            new ProjectMemberRoleSelector.RoleSelectionListener() {

                                                                @Override
                                                                public void roleSelected(
                                                                        ProjectRole role,
                                                                        DataUse dataUse) {

                                                                    if (role != null) {
                                                                        po.addRoleMember(new ProjectRoleMember(
                                                                                roleUser,
                                                                                role,
                                                                                dataUse));
                                                                        if (mode()
                                                                                .equals(FormEditMode.UPDATE)) {
                                                                            po.commitMembers(new ObjectMessageResponse<Boolean>() {

                                                                                @Override
                                                                                public void responded(
                                                                                        Boolean r) {

                                                                                    if (r) {
                                                                                        _roleMemberGrid
                                                                                                .refresh();
                                                                                    }
                                                                                }
                                                                            });
                                                                        } else {
                                                                            _roleMemberGrid
                                                                                    .refresh();
                                                                        }
                                                                    }
                                                                }
                                                            });
                                        }
                                    }));
                            ActionContextMenu am = new ActionContextMenu(menu);
                            NativeEvent ne = event.getNativeEvent();
                            am.showAt(ne);
                        }
                    });
            roleUserGrid.addDropTarget(new DropTarget() {

                @Override
                public BaseWidget widget() {

                    return roleUserGrid;
                }

                @Override
                public DragResponder dragResponderAt(int x, int y,
                        EventTarget target) {

                    return this;
                }

                @Override
                public DropHandler dropHandler() {

                    return new DropHandler() {

                        @Override
                        public DropCheck checkCanDrop(Object data) {

                            if (po.members() == null) {
                                return DropCheck.CANNOT;
                            }
                            if (po.members().size() <= 1) {
                                return DropCheck.CANNOT;
                            }
                            if (data instanceof ProjectRoleMember) {
                                return DropCheck.CAN;
                            }
                            return DropCheck.CANNOT;
                        }

                        @Override
                        public void drop(BaseWidget target, List<Object> data,
                                final DropListener dl) {

                            if (data == null) {
                                dl.dropped(DropCheck.CANNOT);
                                return;
                            }
                            boolean changed = false;
                            for (Object o : data) {
                                if (o instanceof ProjectRoleMember) {
                                    po.removeRoleMember((ProjectRoleMember) o);
                                    changed = true;
                                }
                            }
                            if (changed) {
                                if (mode().equals(FormEditMode.UPDATE)) {
                                    po.commitMembers(new ObjectMessageResponse<Boolean>() {

                                        @Override
                                        public void responded(Boolean r) {

                                            if (r) {
                                                _roleMemberGrid.refresh();
                                                dl.dropped(DropCheck.CAN);
                                            }
                                        }
                                    });
                                } else {
                                    _roleMemberGrid.refresh();
                                    dl.dropped(DropCheck.CAN);
                                }
                            } else {
                                dl.dropped(DropCheck.CANNOT);
                            }

                        }
                    };
                }
            });

            CenteringPanel cpr = new CenteringPanel(Axis.HORIZONTAL);
            cpr.setHeight(22);
            cpr.setWidth100();
            Label usersLabel = new Label("Available role users");
            usersLabel.setFontSize(12);
            usersLabel.setFontWeight(FontWeight.BOLD);
            usersLabel.setHeight100();
            cpr.add(usersLabel);
            vpr.add(cpr);

            vpr.add(roleUserGrid);
        }
    }

}
