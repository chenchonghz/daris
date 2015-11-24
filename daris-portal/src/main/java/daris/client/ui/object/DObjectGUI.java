package daris.client.ui.object;

import java.util.List;
import java.util.Vector;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.image.Image;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.action.ActionInterfaceEntry;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.menu.ObjectMenu;
import arc.gui.object.register.ObjectGUI;
import arc.gui.object.register.ObjectUpdateHandle;
import arc.gui.object.register.ObjectUpdateListener;
import arc.gui.window.Window;
import arc.mf.client.dti.file.DTIDirectory;
import arc.mf.client.dti.file.DTIFile;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.Resource;
import daris.client.model.IDUtil;
import daris.client.model.dataset.DataSet;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.CanDestroy;
import daris.client.model.object.messages.DObjectDestroy;
import daris.client.model.object.messages.DataSetProcessedDestroy;
import daris.client.model.object.messages.DataSetProcessedDestroyableExists;
import daris.client.model.query.filter.pssd.ObjectQuery;
import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.ui.DObjectBrowser;
import daris.client.ui.object.action.DObjectCreateAction;
import daris.client.ui.object.action.DObjectEditAction;
import daris.client.ui.object.action.DerivedDataSetCreateAction;
import daris.client.ui.object.action.DicomIngestAction;
import daris.client.ui.object.action.DicomSendAction;
import daris.client.ui.object.action.ObjectMemberExportForm;
import daris.client.ui.object.action.PrimaryDataSetCreateAction;
import daris.client.ui.object.action.StudyPreCreateAction;
import daris.client.ui.query.action.SearchForm;
import daris.client.ui.study.StudySendForm;
import daris.client.ui.widget.MessageBox;
import daris.client.util.DownloadUtil;

public class DObjectGUI implements ObjectGUI {
    public static final arc.gui.image.Image ICON_RELOAD_GREEN = new arc.gui.image.Image(
            Resource.INSTANCE.refreshGreen16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_RELOAD_BLUE = new arc.gui.image.Image(
            Resource.INSTANCE.refreshBlue16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_CREATE = new Image(
            Resource.INSTANCE.add16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_DOWNLOAD = new Image(
            Resource.INSTANCE.download16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_SHARE = new Image(
            Resource.INSTANCE.share16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_EDIT = new Image(
            Resource.INSTANCE.edit16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_SEND = new Image(
            Resource.INSTANCE.forward16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_DICOM_SEND = new Image(
            Resource.INSTANCE.send16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_DICOM_INGEST = new Image(
            Resource.INSTANCE.upload16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_ADD_TO_SHOPPINGCART = new Image(
            Resource.INSTANCE.shoppingcart24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SEARCH = new arc.gui.image.Image(
            Resource.INSTANCE.search16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_DESTROY = new arc.gui.image.Image(
            Resource.INSTANCE.delete16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_DESTROY_PROCESSED = new arc.gui.image.Image(
            Resource.INSTANCE.clear16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_TAG = new arc.gui.image.Image(
            Resource.INSTANCE.tag16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_EXPORT = new arc.gui.image.Image(
            Resource.INSTANCE.export16().getSafeUri().asString(), 16, 16);

    public static final DObjectGUI INSTANCE = new DObjectGUI();

    private DObjectGUI() {

    }

    @Override
    public String idToString(Object o) {

        if (o instanceof DObjectRef) {
            String id = ((DObjectRef) o).id();
            if (id == null) {
                id = "repository";
            } else {
                id = IDUtil.typeFromId(id) + " " + id;
            }
            return id;
        }
        return null;
    }

    @Override
    public String icon(Object o, int size) {

        return null;
    }

    @Override
    public Object reference(Object o) {

        return null;
    }

    @Override
    public boolean needToResolve(Object o) {

        return ((DObjectRef) o).needToResolve();
    }

    @Override
    public void displayDetails(Object o, final ObjectDetailsDisplay dd,
            final boolean forEdit) {

        final FormEditMode mode = forEdit ? FormEditMode.UPDATE
                : FormEditMode.READ_ONLY;
        final DObjectRef ro = ((DObjectRef) o);
        ro.setForEdit(forEdit);
        ro.reset();
        if (ro.resolved()) {
            dd.display(ro,
                    DObjectDetails.detailsFor(ro.referent(), mode).gui());
        } else {
            ro.reset();
            ObjectResolveHandler<DObject> rh = new ObjectResolveHandler<DObject>() {
                @Override
                public void resolved(DObject oo) {

                    if (oo != null) {
                        dd.display(ro,
                                DObjectDetails.detailsFor(oo, mode).gui());
                    }
                }
            };
            if (mode == FormEditMode.UPDATE) {
                ro.resolveAndLock(rh);
            } else {
                ro.resolve(rh);
            }
        }
    }

    @Override
    public void open(Window w, Object o) {

    }

    @Override
    public DropHandler dropHandler(final Object o) {
        if (o != null) {
            if (o instanceof DObjectRef) {
                final DObjectRef oo = (DObjectRef) o;
                final DObject.Type type = oo.referentType();
                return new DropHandler() {

                    @Override
                    public DropCheck checkCanDrop(Object data) {
                        if (type == DObject.Type.repository) {
                            return DropCheck.CANNOT;
                        }
                        if (data != null) {
                            if (data instanceof DTIFile
                                    || data instanceof DTIDirectory) {
                                return DropCheck.CAN;
                            }
                        }
                        return DropCheck.CANNOT;
                    }

                    @Override
                    public void drop(BaseWidget target, List<Object> data,
                            DropListener dl) {
                        List<LocalFile> fs = new Vector<LocalFile>(data.size());
                        for (Object d : data) {
                            fs.add((LocalFile) d);
                        }
                        ObjectMenu<DObject> m = new ObjectMenu<DObject>(
                                (DObjectRef) o);
                        // TODO: review & enable
                        // m.add(new AttachmentAddAction(fs, oo,
                        // target.window()));
                        if (DObject.Type.subject == type
                                || DObject.Type.ex_method == type
                                || DObject.Type.study == type) {
                            m.add(new DicomIngestAction(fs, oo,
                                    target.window()));
                        }
                        if (type == DObject.Type.study) {
                            m.add(new PrimaryDataSetCreateAction(fs, oo,
                                    target.window()));
                            m.add(new DerivedDataSetCreateAction(fs, oo, null,
                                    target.window()));
                        } else if (type == DObject.Type.dataset) {
                            m.add(new DerivedDataSetCreateAction(fs,
                                    new DObjectRef(IDUtil.getParentId(oo.id()),
                                            oo.proute(), false, false, -1),
                                    oo, target.window()));
                        }
                        if (m.entries() != null && !m.entries().isEmpty()) {
                            new ActionMenu(m).showAt(target.absoluteLeft(),
                                    target.absoluteBottom());
                        }
                        dl.dropped(DropCheck.CAN);
                    }
                };
            }
        }
        return null;
    }

    @Override
    public DragWidget dragWidget(Object o) {

        return null;
    }

    @Override
    public Menu actionMenu(final Window w, final Object o,
            SelectedObjectSet selected, boolean readOnly) {

        if (o == null) {
            return null;
        }

        final DObjectRef ro = (DObjectRef) o;
        final String title = idToString(o);

        ObjectMenu<DObject> menu = new ObjectMenu<DObject>(title);
        menu.setShowTitle(false);

        /*
         * edit
         */
        if (ro.id() != null) {
            menu.add(new ActionInterfaceEntry(ICON_EDIT,
                    new DObjectEditAction(ro, w)));
        }

        /*
         * create child/sibling
         */
        if (ro.isStudy()) {
            menu.add(new ActionInterfaceEntry(ICON_CREATE,
                    new PrimaryDataSetCreateAction(null, ro, w)));
            menu.add(new ActionInterfaceEntry(ICON_CREATE,
                    new DerivedDataSetCreateAction(null, ro, null, w)));
        } else if (ro.isDataSet()) {
            menu.add(
                    new ActionInterfaceEntry(ICON_CREATE,
                            new DerivedDataSetCreateAction(null,
                                    new DObjectRef(IDUtil.getParentId(ro.id()),
                                            ro.proute(), false, false, -1),
                                    ro, w)));
        } else {
            menu.add(new ActionInterfaceEntry(ICON_CREATE,
                    new DObjectCreateAction(ro, w)));
        }

        /*
         * study pre-create
         */
        if (ro.isExMethod()) {
            menu.add(new ActionInterfaceEntry(ICON_CREATE,
                    new StudyPreCreateAction(ro, w)));
        }

        /*
         * export member list
         */
        menu.add(new ActionEntry(ICON_EXPORT, "Export member list...",
                "Exports member list as .xml or .csv file.", new Action() {

                    @Override
                    public void execute() {
                        new ObjectMemberExportForm(ro).show(w,
                                new ActionListener() {

                            @Override
                            public void executed(boolean succeeded) {
                                // DO NOTHING
                            }
                        });
                    }
                }));

        if (ro.id() != null) {
            /*
             * download data-set directly using the servlet api
             */
            if (ro.isDataSet()) {
                final ActionEntry aeDownloadContent = new ActionEntry(
                        ICON_DOWNLOAD, "Download", new Action() {

                            @Override
                            public void execute() {
                                ro.resolve(new ObjectResolveHandler<DObject>() {

                                    @Override
                                    public void resolved(DObject o) {
                                        String contentUrl = ((DataSet) o)
                                                .contentDownloadUrl();
                                        DownloadUtil.download(contentUrl);
                                    }
                                });

                            }
                        });
                aeDownloadContent.disable();
                ro.reset();
                ro.resolve(new ObjectResolveHandler<DObject>() {

                    @Override
                    public void resolved(DObject o) {
                        if (o == null) {
                            return;
                        }
                        String contentUrl = ((DataSet) o).contentDownloadUrl();
                        if (contentUrl != null) {
                            aeDownloadContent.enable();
                        }
                    }
                });
                menu.add(aeDownloadContent);
            }
            /*
             * Share URL
             */
            if (!ro.isProject()) {
                menu.add(new ActionEntry(ICON_SHARE, "Generate Sharable Link",
                        new Action() {

                            @Override
                            public void execute() {
                                new UrlShareForm(ro).showDialog(w);
                            }
                        }));
            }
            /*
             * add to shopping cart
             */
            menu.add(new ActionInterfaceEntry(ICON_TAG, new TagAction(ro, w)));
            menu.add(new ActionEntry(ICON_ADD_TO_SHOPPINGCART,
                    "Add to shopping cart", new Action() {

                        @Override
                        public void execute() {
                            ActiveShoppingCart.addContents(ro, true,
                                    new Action() {

                                @Override
                                public void execute() {
                                    ActiveShoppingCart.get(
                                            new ObjectResolveHandler<ShoppingCartRef>() {

                                        @Override
                                        public void resolved(
                                                ShoppingCartRef asc) {
                                            MessageBox.display(
                                                    MessageBox.Type.info,
                                                    "Shopping cart " + asc.id(),
                                                    ro.referentTypeName() + " "
                                                            + ro.id()
                                                            + " has been added to shopping cart "
                                                            + asc.id() + ".",
                                                    3);
                                        }
                                    });
                                }
                            });
                        }
                    }));

            /*
             * search project
             */
            final DObjectRef project = ro.isProject() ? ro
                    : new DObjectRef(IDUtil.getProjectId(ro.id()));
            menu.add(new ActionEntry(ICON_SEARCH, "Search...",
                    "Search for DaRIS/PSSD objects.", new Action() {

                        @Override
                        public void execute() {
                            new SearchForm(new ObjectQuery(project)).show(w,
                                    "Search for DaRIS/PSSD objects...");
                        }
                    }, true));

            /*
             * destroy
             */
            if (ro.isProject() || ro.isSubject() || ro.isExMethod()
                    || ro.isStudy() || ro.isDataSet()) {
                String label = "Delete " + ro.referentTypeName() + " " + ro.id()
                        + "...";
                String description = "Delete " + ro.referentTypeName() + " "
                        + ro.id();
                final ActionEntry aeDestroy = new ActionEntry(ICON_DESTROY,
                        label, description, new Action() {

                            @Override
                            public void execute() {
                                Dialog.confirm(
                                        "Deleting " + ro.referentTypeName()
                                                + " " + ro.id(),
                                        "Are you sure you want to delete "
                                                + ro.referentTypeName() + " "
                                                + ro.id()
                                                + " and all its descendants?",
                                        new ActionListener() {

                                    @Override
                                    public void executed(boolean succeeded) {
                                        if (succeeded) {
                                            new DObjectDestroy(ro).send(
                                                    new ObjectMessageResponse<Null>() {

                                                @Override
                                                public void responded(Null r) {
                                                    MessageBox.info("Deleted",
                                                            ro.referentTypeName()
                                                                    + " "
                                                                    + ro.id()
                                                                    + " has been deleted.",
                                                            4);
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                        });
                aeDestroy.disable();
                new CanDestroy(ro).send(new ObjectMessageResponse<Boolean>() {

                    @Override
                    public void responded(Boolean can) {
                        aeDestroy.setEnabled(can);
                    }
                });
                menu.add(aeDestroy);
            }

            /*
             * destroy processed
             */
            if (ro.isProject() || ro.isSubject() || ro.isExMethod()
                    || ro.isStudy()) {
                String label = "Delete processed datasets in "
                        + ro.referentTypeName() + " " + ro.id() + "...";
                String description = "Delete processed datasets in "
                        + ro.referentTypeName() + " " + ro.id();
                final ActionEntry aeDestroyProcessed = new ActionEntry(
                        ICON_DESTROY_PROCESSED, label, description,
                        new Action() {

                            @Override
                            public void execute() {
                                Dialog.confirm("Delete processed datasets in "
                                        + ro.referentTypeName() + " " + ro.id(),
                                        "Are you sure you want to delete all the processed datasets in "
                                                + ro.referentTypeName() + " "
                                                + ro.id() + "?",
                                        new ActionListener() {

                                    @Override
                                    public void executed(boolean succeeded) {
                                        if (succeeded) {
                                            new DataSetProcessedDestroy(ro)
                                                    .send(new ObjectMessageResponse<XmlElement>() {

                                                @Override
                                                public void responded(
                                                        XmlElement re) {
                                                    int destroyed = 0;
                                                    int failed = 0;
                                                    try {
                                                        destroyed = re
                                                                .intValue();
                                                        failed = re.intValue(
                                                                "@failed", 0);
                                                    } catch (Throwable e) {
                                                        e.printStackTrace(
                                                                System.out);
                                                    }
                                                    StringBuilder sb = new StringBuilder();
                                                    sb.append(destroyed);
                                                    sb.append(
                                                            " processed datasets have been deleted.");
                                                    if (failed > 0) {
                                                        sb.append(
                                                                " There are still "
                                                                        + failed
                                                                        + " processed data sets in ");
                                                        sb.append(ro
                                                                .referentTypeName()
                                                                + " "
                                                                + ro.id());
                                                        sb.append(
                                                                " that you do not have privilege to delete.");
                                                    }
                                                    MessageBox.info("Deleted",
                                                            sb.toString(), 4);
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                aeDestroyProcessed.disable();
                new DataSetProcessedDestroyableExists(ro)
                        .send(new ObjectMessageResponse<Boolean>() {

                            @Override
                            public void responded(Boolean exists) {
                                aeDestroyProcessed.setEnabled(exists);
                            }
                        });
                menu.add(aeDestroyProcessed);
            }

            if (((DObjectRef) o).isStudy()) {
                menu.add(new ActionEntry(ICON_SEND, "Send/Copy study to...",
                        "Send/Copy study to another project/subject.",
                        new Action() {

                            @Override
                            public void execute() {
                                new StudySendForm((DObjectRef) o).show(w,
                                        new ActionListener() {

                                    @Override
                                    public void executed(boolean succeeded) {
                                        if (succeeded) {
                                            MessageBox.info("Sending study",
                                                    "Started sending study "
                                                            + ((DObjectRef) o)
                                                                    .id(),
                                                    3);
                                        }
                                    }
                                });
                                ;
                            }
                        }));
            }

            /*
             * dicom send
             */
            menu.add(new ActionInterfaceEntry(ICON_DICOM_SEND,
                    new DicomSendAction((DObjectRef) o, w)));

            /*
             * refresh (current object)
             */
            menu.add(new ActionEntry(ICON_RELOAD_GREEN,
                    "Refresh " + ro.referentTypeName() + " " + ro.id(),
                    new Action() {

                        @Override
                        public void execute() {
                            DObjectBrowser.get(false).reloadSelected();
                        }
                    }));
        }

        menu.addSeparator();

        /*
         * refresh repository
         */
        menu.add(new ActionEntry(ICON_RELOAD_BLUE, "Refresh repository",
                new Action() {

                    @Override
                    public void execute() {
                        DObjectBrowser.get(false).reloadAll();
                    }
                }));

        return menu;
    }

    @Override
    public Menu memberActionMenu(Window w, Object o, SelectedObjectSet selected,
            boolean readOnly) {
        return null;
    }

    @Override
    public ObjectUpdateHandle createUpdateMonitor(Object o,
            ObjectUpdateListener ul) {
        return null;
    }

}
