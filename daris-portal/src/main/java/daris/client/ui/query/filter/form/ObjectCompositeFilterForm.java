package daris.client.ui.query.filter.form;

import java.util.ArrayList;
import java.util.List;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.ObjectUtil;
import arc.mf.dtype.EnumerationType;
import arc.mf.object.CollectionResolveHandler;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObjectCollectionRef;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.ProjectEnumDataSource;
import daris.client.model.query.filter.CompositeFilter.Member;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.FilterTree;
import daris.client.model.query.filter.pssd.ObjectCompositeFilter;
import daris.client.model.query.filter.pssd.ObjectFilterTree;
import daris.client.model.query.filter.pssd.ProjectAware;

public class ObjectCompositeFilterForm extends CompositeFilterForm {

    public static interface ProjectChangeListener {
        void projectChanged(DObjectRef project);
    }

    private VerticalPanel _vp;
    private Form _projectForm;
    private List<ProjectChangeListener> _pcls;

    public ObjectCompositeFilterForm(ObjectCompositeFilter filter, boolean editable) {
        this(filter, editable, new ObjectFilterTree(filter.project()), true);
    }

    public ObjectCompositeFilterForm(ObjectCompositeFilter filter, boolean editable, FilterTree filterTree) {
        this(filter, editable, filterTree, true);
    }

    public ObjectCompositeFilterForm(ObjectCompositeFilter filter, boolean editable, FilterTree filterTree,
            boolean canChangeProject) {
        super(filter, editable, filterTree == null ? new ObjectFilterTree(filter.project()) : filterTree);
        _vp = new VerticalPanel();
        _vp.fitToParent();
        _projectForm = new Form(canChangeProject ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        final Field<DObjectRef> projectField = new Field<DObjectRef>(new FieldDefinition("Search in project",
                new EnumerationType<DObjectRef>(new ProjectEnumDataSource()), null, null, 1, 1));
        if (project() != null) {
            projectField.setInitialValue(project(), false);
        } else {
            new DObjectCollectionRef().resolve(new CollectionResolveHandler<DObjectRef>() {

                @Override
                public void resolved(List<DObjectRef> projects) throws Throwable {
                    if (projects != null && !projects.isEmpty()) {
                        setProject(projects.get(0));
                    }
                    projectField.setValue(project());
                }
            });
        }
        projectField.addListener(new FormItemListener<DObjectRef>() {

            @Override
            public void itemValueChanged(FormItem<DObjectRef> f) {
                setProject(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<DObjectRef> f, Property property) {

            }
        });
        _projectForm.add(projectField);
        _projectForm.setHeight(22);
        _projectForm.render();
        _vp.add(_projectForm);
        addMustBeValid(_projectForm, false);

        _vp.add(super.gui());
    }

    private DObjectRef project() {
        return ((ObjectCompositeFilter) filter()).project();
    }

    private void setProject(DObjectRef project) {
        if (!ObjectUtil.equals(project, project())) {
            ((ObjectCompositeFilter) filter()).setProject(project);
            clearMembers();
            notifyOfProjectChange();
        }
    }

    private void notifyOfProjectChange() {
        if (_pcls != null) {
            for (ProjectChangeListener pcl : _pcls) {
                pcl.projectChanged(project());
            }
        }
    }

    public void addProjectChangeListener(ProjectChangeListener pcl) {
        if (_pcls == null) {
            _pcls = new ArrayList<ProjectChangeListener>();
        }
        _pcls.add(pcl);
    }

    public void removeProjectChangeListener(ProjectChangeListener pcl) {
        if (_pcls != null) {
            _pcls.remove(pcl);
        }
    }

    public void addMember(Member m) {
        Filter f = m.filter();
        if (f instanceof ProjectAware) {
            ((ProjectAware) f).setProject(project());
        }
        super.addMember(m);
    }

    @Override
    public Widget gui() {
        return _vp;
    }

}
