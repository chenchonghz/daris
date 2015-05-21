package daris.client.ui.dti.file;

import java.util.List;

import arc.gui.file.FileFilter;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.file.LocalFile;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.ui.util.ButtonUtil;
import daris.client.util.StringUtil;

public class LocalFileSelectDialog {
	public static final double WIDTH = 0.5;
	public static final double HEIGHT = 0.5;

	public static interface FileSelectionHandler {
		void fileSelected(LocalFile file);
	}

	private FileSelectionHandler _sh;

	private Window _win;
	private VerticalPanel _vp;
	private LocalFileNavigatorPanel _nav;
	private Button _cancelButton;
	private Button _selectButton;
	private LocalFileSelectTarget _target;

	public LocalFileSelectDialog(LocalFileSelectTarget target, LocalFile root, FileFilter fileFilter,
			FileSelectionHandler sh) {

		_target = target;
		_sh = sh;

		_vp = new VerticalPanel();
		_vp.fitToParent();

		_nav = new LocalFileNavigatorPanel(root, LocalFile.Filter.ANY, fileFilter, false) {

			protected void selectedDirectory(LocalFile dir) {
				if (_nav.files() == null) {
					_selectButton.disable();
				}
			}

			protected void selectedFiles(List<LocalFile> files) {
				if (files != null && !files.isEmpty()) {
					LocalFile file = files.get(0);
					if (_target == LocalFileSelectTarget.ANY
							|| (_target == LocalFileSelectTarget.FILE && file.isFile())
							|| (_target == LocalFileSelectTarget.DIRECTORY && file.isDirectory())) {
						_selectButton.enable();
						return;
					}
				}
				_selectButton.disable();
			}
		};
		_vp.add(_nav);

		ButtonBar bb = ButtonUtil.createButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT, 32);
		_cancelButton = bb.addButton("Cancel");
		_cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				_win.close();
			}
		});
		_selectButton = bb.addButton("Select");
		_selectButton.setMarginRight(20);
		_selectButton.disable();
		_selectButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				_win.close();
				if (_sh != null) {
					if (_nav.files() != null & !_nav.files().isEmpty()) {
						_sh.fileSelected(_nav.files().get(0));
					}
				}
			}
		});
		_vp.add(bb);
	}

	public void show(Window owner) {

		WindowProperties wp = new WindowProperties();
		wp.setModal(true);
		wp.setTitle("Select Local " + StringUtil.upperCaseFirst(_target.toString()));
		wp.setCanBeResized(true);
		wp.setCanBeClosed(false);
		wp.setCanBeMoved(true);
		wp.setOwnerWindow(owner);
		wp.setSize(WIDTH, HEIGHT);
		wp.setCenterInPage(true);
		_win = Window.create(wp);
		_win.setContent(_vp);
		_win.centerInPage();
		_win.show();
	}
}
