package daris.client.ui.nifti;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;

import daris.client.model.dataset.DataSet;
import daris.client.model.nifti.NiftiHeader;
import daris.client.model.nifti.NiftiHeaderRef;
import daris.client.ui.form.XmlMetaForm;
import daris.client.ui.image.ImageSeriesViewer;
import daris.client.ui.image.ImageSwitchControl;
import daris.client.ui.image.ImageSwitchControl.ImageSwitchListener;
import daris.client.ui.nifti.papaya.PapayaViewer;
import daris.client.ui.nifti.papaya.params.Params;

public class NiftiSeriesViewer extends ImageSeriesViewer {

    /**
     * the variable to record the slice index of the previous instance of the
     * viewer class.
     */
    private static int _prevSlice = -1;

    private DataSet _data;

    private NiftiHeader _header;

    private boolean _usePapaya;

    private VerticalSplitPanel _vsp;
    private SimplePanel _imageSP;
    private NiftiImagePanel _imagePanel;
    private SimplePanel _switchCtrlSP;
    private ImageSwitchControl _switchCtrl;
    private SimplePanel _metadataSP;

    public NiftiSeriesViewer(DataSet data) {
        this(data, InitialPosition.MIDDLE, false);
    }

    public NiftiSeriesViewer(DataSet data, boolean usePapaya) {
        this(data, InitialPosition.MIDDLE, usePapaya);
    }

    public NiftiSeriesViewer(DataSet data, InitialPosition initialPosition, boolean usePapaya) {

        super(initialPosition);

        _data = data;
        _usePapaya = usePapaya;

        _vsp = new VerticalSplitPanel(5);
        _vsp.fitToParent();

        _imageSP = new SimplePanel();
        _imageSP.setWidth100();
        _imageSP.setPreferredHeight(0.5);
        _vsp.add(_imageSP);

        VerticalPanel vp = new VerticalPanel();
        vp.setWidth100();
        vp.setPreferredHeight(0.5);
        _vsp.add(vp);

        _switchCtrlSP = new SimplePanel();
        _switchCtrlSP.setHeight(28);
        _switchCtrlSP.setWidth100();
        _switchCtrlSP.setBackgroundColour(new RGB(0xdd, 0xdd, 0xdd));
        vp.add(_switchCtrlSP);

        _metadataSP = new SimplePanel();
        _metadataSP.fitToParent();
        vp.add(_metadataSP);

        new NiftiHeaderRef(_data.assetId()).resolve(new ObjectResolveHandler<NiftiHeader>() {

            @Override
            public void resolved(NiftiHeader header) {
                _header = header;
                updateComponents();
            }
        });
    }

    private void updateComponents() {
        if (_header == null || _header.dimZ() <= 0) {
            _imageSP.clear();
            _switchCtrlSP.clear();
            _metadataSP.clear();
            return;
        }

        if (_usePapaya) {

            // TODO: remove
            System.out.println("Papaya viewer: " +_data.contentDownloadUrl());

            final PapayaViewer pv = new PapayaViewer(new Params().addImage(_data.contentDownloadUrl()).setExpandable(
                    true));
            _imageSP.setContent(pv.widget());

            HorizontalPanel hp = new HorizontalPanel();
            hp.fitToParent();

            SimplePanel paddingSP = new SimplePanel();
            paddingSP.fitToParent();
            hp.add(paddingSP);

            final Button button = new Button("Use DaRIS NIFTI viewer");
            button.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    button.disable();
                    _usePapaya = false;
                    updateComponents();
                }
            });
            button.disable();
            new Timer() {
                @Override
                public void run() {
                    button.enable();
                }
            }.schedule(3000);
            button.setWidth(180);
            button.setMarginTop(2);
            button.setMarginRight(10);
            hp.add(button);

            _switchCtrlSP.setContent(hp);

            _metadataSP.setContent(new ScrollPanel(XmlMetaForm
                    .formFor(_header.xml().elements(), FormEditMode.READ_ONLY), ScrollPolicy.AUTO));
        } else {
            _imagePanel = new NiftiImagePanel(_data.assetId());
            _imageSP.setContent(_imagePanel);

            HorizontalPanel hp = new HorizontalPanel();
            hp.fitToParent();

            int nbSlices = _header.dimZ();
            _switchCtrl = new ImageSwitchControl("slice", nbSlices);
            hp.add(_switchCtrl);

            final Button button = new Button("Use Papaya NIFTI viewer");
            button.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    button.disable();
                    _usePapaya = true;
                    updateComponents();
                }
            });
            button.disable();
            new Timer() {
                @Override
                public void run() {
                    button.enable();
                }
            }.schedule(3000);
            button.setWidth(180);
            button.setMarginTop(2);
            button.setMarginRight(10);
            hp.add(button);

            _switchCtrlSP.setContent(hp);

            _metadataSP.setContent(new ScrollPanel(XmlMetaForm
                    .formFor(_header.xml().elements(), FormEditMode.READ_ONLY), ScrollPolicy.AUTO));

            _switchCtrl.addListener(new ImageSwitchListener() {
                @Override
                public void switchTo(final int index, final ActionListener al) {
                    _imagePanel.seek(index, new ActionListener() {

                        @Override
                        public void executed(boolean succeeded) {
                            if (succeeded) {
                                _prevSlice = index;
                            }
                            if (al != null) {
                                al.executed(succeeded);
                            }
                        }
                    });
                }
            });
            if (_prevSlice < 0 || _prevSlice >= nbSlices) {
                switch (initialPosition()) {
                case FIRST:
                    _switchCtrl.first();
                    break;
                case MIDDLE:
                    _switchCtrl.middle();
                    break;
                case LAST:
                    _switchCtrl.last();
                    break;
                default:
                    break;
                }
            } else {
                _switchCtrl.goTo(_prevSlice, null);
            }
        }

    }

    public BaseWidget widget() {
        return _vsp;
    }

}
