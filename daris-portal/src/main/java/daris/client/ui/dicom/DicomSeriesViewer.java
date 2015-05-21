package daris.client.ui.dicom;

import java.util.Map;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.dicom.DicomElement;
import daris.client.model.dicom.messages.DicomMetadataGet;
import daris.client.ui.image.ImageSeriesViewer;
import daris.client.ui.image.ImageSwitchControl;
import daris.client.ui.image.ImageSwitchControl.ImageSwitchListener;

public class DicomSeriesViewer extends ImageSeriesViewer {

    /**
     * the variable to record the slice index of the previous instance of the
     * viewer class.
     */
    private static int _prevSlice = -1;
    /**
     * the variable to record the frame index of the previous instance of the
     * viewer class.
     */
    private static int _prevFrame = -1;

    private String _assetId;
    private int _nbSlices;

    private VerticalSplitPanel _vsp;
    private SimplePanel _imageSP;
    private DicomImagePanel _imagePanel;
    private VerticalPanel _switchCtrlVP;
    private ImageSwitchControl _sliceSwitchCtrl;
    private ImageSwitchControl _frameSwitchCtrl;
    private DicomMetadataGrid _metadataGrid;

    public DicomSeriesViewer(String assetId, int nbSlices, InitialPosition initialPosition) {

        super(initialPosition);

        _assetId = assetId;
        _nbSlices = nbSlices;
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

        _switchCtrlVP = new VerticalPanel();
        _switchCtrlVP.setHeight(28);
        _switchCtrlVP.setWidth100();
        vp.add(_switchCtrlVP);

        _metadataGrid = new DicomMetadataGrid(_assetId);
        _metadataGrid.fitToParent();
        vp.add(_metadataGrid);

        if (_nbSlices > 0) {
            new DicomMetadataGet(_assetId).send(new ObjectMessageResponse<Map<String, DicomElement>>() {

                @Override
                public void responded(Map<String, DicomElement> r) {
                    int nbFrames = 0;
                    if (r != null) {
                        DicomElement fse = r.get(DicomElement.FRAME_SIZE_TAG);
                        if (fse != null) {
                            try {
                                nbFrames = Integer.parseInt(fse.values().get(0));
                            } catch (Throwable e) {
                                ThrowableUtil.rethrowAsUnchecked(e);
                            }
                        }
                        _imagePanel = new DicomImagePanel(_assetId, nbFrames);
                        _imageSP.setContent(_imagePanel);
                        _sliceSwitchCtrl = new ImageSwitchControl("slice", _nbSlices);
                        _sliceSwitchCtrl.addListener(new ImageSwitchListener() {

                            @Override
                            public void switchTo(final int index, final ActionListener al) {
                                final boolean[] executed = { false, false };
                                final boolean[] result = { false, false };
                                _imagePanel.seek(index, 0, new ActionListener() {

                                    @Override
                                    public void executed(boolean succeeded) {
                                        executed[0] = true;
                                        result[0] = succeeded;
                                        if (executed[0] && executed[1] && al != null) {
                                            if (result[0] && result[1]) {
                                                _prevSlice = index;
                                            }
                                            al.executed(result[0] && result[1]);
                                        }
                                    }
                                });
                                _metadataGrid.seek(index, new ActionListener() {

                                    @Override
                                    public void executed(boolean succeeded) {
                                        executed[1] = true;
                                        result[1] = succeeded;
                                        if (executed[0] && executed[1] && al != null) {
                                            if (result[0] && result[1]) {
                                                _prevSlice = index;
                                            }
                                            al.executed(result[0] && result[1]);
                                        }
                                    }
                                });
                            }
                        });
                        _switchCtrlVP.removeAll();
                        _switchCtrlVP.add(_sliceSwitchCtrl);
                        if (nbFrames > 0) {
                            _frameSwitchCtrl = new ImageSwitchControl("frame", nbFrames);
                            _frameSwitchCtrl.addListener(new ImageSwitchListener() {

                                @Override
                                public void switchTo(final int frame, final ActionListener al) {
                                    _imagePanel.seek(_sliceSwitchCtrl.index(), frame, new ActionListener() {

                                        @Override
                                        public void executed(boolean succeeded) {
                                            if (succeeded) {
                                                _prevFrame = frame;
                                            }
                                            if (al != null) {
                                                al.executed(succeeded);
                                            }
                                        }
                                    });
                                }
                            });
                            _switchCtrlVP.add(_frameSwitchCtrl);
                            _switchCtrlVP.setHeight(56);
                        } else {
                            _frameSwitchCtrl = null;
                            _switchCtrlVP.setHeight(28);
                        }
                        if (_prevSlice < 0 || _prevSlice >= _nbSlices) {
                            /*
                             * no slice index was recorded for the last instance
                             * of the viewer or the index is out of bound.
                             */
                            switch (initialPosition()) {
                            case FIRST:
                                _sliceSwitchCtrl.first();
                                break;
                            case MIDDLE:
                                _sliceSwitchCtrl.middle();
                                break;
                            case LAST:
                                _sliceSwitchCtrl.last();
                                break;
                            default:
                                break;
                            }
                        } else {
                            /*
                             * try to jump to the slice index that was recorded
                             * for the last instance of the viewer.
                             */
                            _sliceSwitchCtrl.goTo(_prevSlice, new ActionListener() {

                                @Override
                                public void executed(boolean succeeded) {
                                    if (_frameSwitchCtrl != null && _prevFrame >= 0
                                            && _prevFrame < _frameSwitchCtrl.total()) {
                                        /*
                                         * try to jump to the frame index that
                                         * was recorded for the last instance of
                                         * the viewer.
                                         */
                                        _frameSwitchCtrl.goTo(_prevFrame, null);
                                    }
                                }
                            });

                        }
                    }
                }
            });

        }

    }

    @Override
    public BaseWidget widget() {
        return _vsp;
    }

}
